package com.eurobuddha.terminalide;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;

import org.json.JSONObject;
import org.minimarex.minimaapi.MinimaAPI;
import org.minimarex.minimaapi.MinimaAPIMessages;

public class MainActivity extends AppCompatActivity {

    ViewPager mMainPager;
    TerminalAdapter mAdapter;
    NodeApi mNode;

    Toolbar mToolbar;
    TextView mPairBanner;

    /** Live NEWBLOCK listener so the toolbar block height updates without polling. */
    private final BroadcastReceiver mNotifyReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (!MinimaAPI.checkMinimaID(context, intent)) return;
            String message = intent.getStringExtra(MinimaAPIMessages.MINIMA_API_NOTIFY_DATA);
            if (message == null) return;
            try {
                JSONObject json = new JSONObject(message);
                if ("NEWBLOCK".equals(json.optString("event"))) {
                    String block = json.getJSONObject("data").getJSONObject("txpow")
                            .getJSONObject("header").optString("block", "");
                    if (!block.isEmpty()) setBlock(block);
                }
            } catch (Exception ignored) {}
        }
    };
    private boolean mReceiverRegistered = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        // NO EdgeToEdge.enable() — matching the proven sibling apps (utxo/mail/wallet).
        // On Android 14 the window then genuinely resizes for the keyboard (adjustResize);
        // on Android 15 edge-to-edge is enforced and this listener pads for bars + IME.
        View root = findViewById(R.id.main);
        ViewCompat.setOnApplyWindowInsetsListener(root, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            Insets ime = insets.getInsets(WindowInsetsCompat.Type.ime());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right,
                    Math.max(systemBars.bottom, ime.bottom));
            return insets;
        });
        ViewCompat.requestApplyInsets(root);

        mToolbar = findViewById(R.id.toolbar);
        mToolbar.setTitle("Terminal IDE");
        setSupportActionBar(mToolbar);

        mPairBanner = findViewById(R.id.pair_banner);
        mPairBanner.setOnClickListener(v -> {
            Intent launch = getPackageManager().getLaunchIntentForPackage("org.minimarex.minimacore");
            if (launch != null) startActivity(launch);
        });

        mAdapter = new TerminalAdapter(this);
        mMainPager = findViewById(R.id.main_pager);
        mMainPager.setOffscreenPageLimit(3);
        mMainPager.setAdapter(mAdapter);

        TabLayout tabs = findViewById(R.id.tabs);
        tabs.setupWithViewPager(mMainPager);
        String[] titles = {"Terminal", "Scripts", "Txn", "Logs"};
        for (int i = 0; i < titles.length; i++) {
            TabLayout.Tab t = tabs.getTabAt(i);
            if (t != null) t.setText(titles[i]);
        }
        tabs.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override public void onTabSelected(TabLayout.Tab tab) { mAdapter.refreshPagerView(tab.getPosition()); }
            @Override public void onTabUnselected(TabLayout.Tab tab) {}
            @Override public void onTabReselected(TabLayout.Tab tab) { mAdapter.refreshPagerView(tab.getPosition()); }
        });

        // One NodeApi for the whole activity; pairing state drives the banner.
        mNode = new NodeApi(this, enabled -> {
            mPairBanner.setVisibility(enabled ? View.GONE : View.VISIBLE);
            if (enabled) fetchBlock();
        });
        mAdapter.getTerminalView().setNodeApi(mNode);
        mAdapter.getTxnView().setNodeApi(mNode);
        mAdapter.getScriptsView().setNodeApi(mNode);

        IntentFilter filter = new IntentFilter(MinimaAPIMessages.MINIMA_API_NOTIFY);
        if (android.os.Build.VERSION.SDK_INT >= 33) {
            registerReceiver(mNotifyReceiver, filter, Context.RECEIVER_EXPORTED);
        } else {
            registerReceiver(mNotifyReceiver, filter);
        }
        mReceiverRegistered = true;

        fetchBlock();
    }

    private void fetchBlock() {
        mNode.cmd("block", new NodeApi.Cb() {
            @Override
            public void onResult(JSONObject json) {
                JSONObject resp = json.optJSONObject("response");
                if (resp != null) setBlock(resp.optString("block", ""));
            }
            @Override
            public void onError(String message) {
                if (!NodeApi.ERR_NOT_ENABLED.equals(message)) {
                    mToolbar.setSubtitle("node offline?");
                }
            }
        });
    }

    private void setBlock(String block) {
        if (!block.isEmpty()) mToolbar.setSubtitle("block " + block);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(0, 1, 0, "Clear terminal");
        menu.add(0, 3, 1, "Share session…");
        menu.add(0, 4, 2, "Minima docs");
        menu.add(0, 2, 3, "About");
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == 1) {
            mAdapter.getTerminalView().clearTerminal();
            return true;
        }
        if (item.getItemId() == 3) {
            Intent share = new Intent(Intent.ACTION_SEND);
            share.setType("text/plain");
            share.putExtra(Intent.EXTRA_TEXT, mAdapter.getTerminalView().exportText());
            startActivity(Intent.createChooser(share, "Share terminal session"));
            return true;
        }
        if (item.getItemId() == 4) {
            startActivity(new Intent(Intent.ACTION_VIEW,
                    android.net.Uri.parse("https://docs.minima.global")));
            return true;
        }
        if (item.getItemId() == 2) {
            new AlertDialog.Builder(this)
                    .setTitle("Terminal IDE")
                    .setMessage("Professional tooling for the Minima protocol:\n\n"
                            + "• Terminal — full node command line with history, autocomplete "
                            + "and colorized output\n"
                            + "• Scripts — KISS VM editor with lint, offline testing (the node's "
                            + "own VM) and deploy\n"
                            + "• Txn — guided manual-UTXO transaction workbench\n\n"
                            + "Requires the Minima Core app: enable Terminal IDE in "
                            + "Minima Core → Apps.")
                    .setPositiveButton("OK", null)
                    .show();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        fetchBlock();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mReceiverRegistered) unregisterReceiver(mNotifyReceiver);
        if (mNode != null) mNode.onDestroy();
    }
}
