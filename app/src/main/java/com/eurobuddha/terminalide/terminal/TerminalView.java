package com.eurobuddha.terminalide.terminal;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.graphics.Typeface;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

import com.eurobuddha.terminalide.BaseView;
import com.eurobuddha.terminalide.NodeApi;
import com.eurobuddha.terminalide.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

/**
 * Professional terminal: persistent history, autocomplete suggestion chips, colorized
 * output entries with long-press copy, favorites, per-command timing, and guards
 * against known node-killer commands (unbounded coins/history).
 */
public class TerminalView extends BaseView {

    private static final int MAX_ENTRIES = 300;

    ScrollView mScroller;
    LinearLayout mOutput;
    EditText mInput;
    LinearLayout mChipRow;
    HorizontalScrollView mChipScroller;
    TextView mParamHint;

    NodeApi mNode;
    HistoryDB mHistory;

    // History navigation state: -1 = live input.
    private int mHistPos = -1;
    private String mLiveDraft = "";

    public TerminalView(Activity zActivity) {
        super(zActivity, R.layout.view_terminal);

        mHistory = new HistoryDB(zActivity);

        mScroller = mMainView.findViewById(R.id.terminal_scroller);
        mOutput = mMainView.findViewById(R.id.terminal_output);
        mInput = mMainView.findViewById(R.id.terminal_input);
        mChipRow = mMainView.findViewById(R.id.terminal_chiprow);
        mChipScroller = mMainView.findViewById(R.id.terminal_chipscroller);
        mParamHint = mMainView.findViewById(R.id.terminal_paramhint);

        banner();

        mInput.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE
                    || (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER
                        && event.getAction() == KeyEvent.ACTION_DOWN)) {
                submit();
                return true;
            }
            return false;
        });

        mInput.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int a, int b, int c) {}
            @Override public void onTextChanged(CharSequence s, int a, int b, int c) {}
            @Override
            public void afterTextChanged(Editable s) {
                updateSuggestions(s.toString());
            }
        });

        Button run = mMainView.findViewById(R.id.terminal_send);
        run.setOnClickListener(v -> submit());

        Button up = mMainView.findViewById(R.id.terminal_hist_up);
        Button down = mMainView.findViewById(R.id.terminal_hist_down);
        up.setOnClickListener(v -> histNav(1));
        down.setOnClickListener(v -> histNav(-1));
        up.setOnLongClickListener(v -> { showHistoryDialog(); return true; });

        Button fav = mMainView.findViewById(R.id.terminal_fav);
        fav.setOnClickListener(v -> showFavoritesDialog());
        fav.setOnLongClickListener(v -> { saveFavorite(); return true; });

        updateSuggestions("");
    }

    public void setNodeApi(NodeApi zNode) {
        mNode = zNode;
    }

    private void banner() {
        appendEntry(OutputFormatter.colored(
                "╔══════════════════════════════╗\n"
              + "║   MINIMA  TERMINAL  IDE      ║\n"
              + "╚══════════════════════════════╝\n"
              + "Type a command, or tap a suggestion chip.\n"
              + "▲/▼ = history (long-press ▲ for list) · ★ = favorites (long-press to save)\n"
              + "Long-press any output block to copy it.", OutputFormatter.COL_DIM));
    }

    public void clearTerminal() {
        mOutput.removeAllViews();
        banner();
    }

    // ---------------- input / history ----------------

    private void submit() {
        String cmd = mInput.getText().toString().trim();
        if (cmd.isEmpty()) return;
        mInput.setText("");
        mHistPos = -1;
        mLiveDraft = "";
        runCMD(cmd);
    }

    private void histNav(int direction) {
        List<String> hist = mHistory.getHistory(200);
        if (hist.isEmpty()) return;
        if (mHistPos == -1) mLiveDraft = mInput.getText().toString();
        int next = mHistPos + direction;
        if (next < -1) next = -1;
        if (next >= hist.size()) next = hist.size() - 1;
        mHistPos = next;
        mInput.setText(mHistPos == -1 ? mLiveDraft : hist.get(mHistPos));
        mInput.setSelection(mInput.getText().length());
    }

    private void showHistoryDialog() {
        List<String> hist = mHistory.getHistory(100);
        if (hist.isEmpty()) {
            Toast.makeText(mActivity, "No history yet", Toast.LENGTH_SHORT).show();
            return;
        }
        String[] items = hist.toArray(new String[0]);
        new AlertDialog.Builder(mActivity)
                .setTitle("Command history")
                .setItems(items, (d, which) -> {
                    mInput.setText(items[which]);
                    mInput.setSelection(mInput.getText().length());
                })
                .setNegativeButton("Close", null)
                .show();
    }

    private void saveFavorite() {
        String cmd = mInput.getText().toString().trim();
        if (cmd.isEmpty()) {
            Toast.makeText(mActivity, "Type a command first, then long-press ★ to save it", Toast.LENGTH_SHORT).show();
            return;
        }
        EditText name = new EditText(mActivity);
        name.setHint("Name this favorite");
        new AlertDialog.Builder(mActivity)
                .setTitle("Save favorite")
                .setMessage(cmd)
                .setView(name)
                .setPositiveButton("Save", (d, w) -> {
                    String n = name.getText().toString().trim();
                    mHistory.addFavorite(n.isEmpty() ? cmd : n, cmd);
                    Toast.makeText(mActivity, "Saved", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showFavoritesDialog() {
        List<String[]> favs = mHistory.getFavorites();
        if (favs.isEmpty()) {
            Toast.makeText(mActivity, "No favorites — type a command and long-press ★", Toast.LENGTH_LONG).show();
            return;
        }
        String[] labels = new String[favs.size()];
        for (int i = 0; i < favs.size(); i++) labels[i] = favs.get(i)[1];
        new AlertDialog.Builder(mActivity)
                .setTitle("Favorites (long-press ★ on a command to add)")
                .setItems(labels, (d, which) -> {
                    mInput.setText(favs.get(which)[2]);
                    mInput.setSelection(mInput.getText().length());
                })
                .setNeutralButton("Delete…", (d, w) -> showDeleteFavoriteDialog(favs, labels))
                .setNegativeButton("Close", null)
                .show();
    }

    private void showDeleteFavoriteDialog(List<String[]> favs, String[] labels) {
        new AlertDialog.Builder(mActivity)
                .setTitle("Delete favorite")
                .setItems(labels, (d, which) -> {
                    mHistory.deleteFavorite(Long.parseLong(favs.get(which)[0]));
                    Toast.makeText(mActivity, "Deleted", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    // ---------------- suggestions ----------------

    private void updateSuggestions(String text) {
        mChipRow.removeAllViews();
        String trimmed = text.trim();
        boolean firstToken = !text.contains(" ");

        if (firstToken) {
            mParamHint.setVisibility(View.GONE);
            for (String name : CommandRegistry.commandNames()) {
                if (trimmed.isEmpty() || name.startsWith(trimmed)) {
                    if (!name.equals(trimmed)) addChip(name, name + " ");
                }
            }
        } else {
            String cmdName = text.split(" ")[0];
            String hint = CommandRegistry.paramsFor(cmdName);
            if (hint != null) {
                mParamHint.setText(hint);
                mParamHint.setVisibility(View.VISIBLE);
                // Suggest param keys not already present.
                String params = hint.substring(cmdName.length()).trim();
                for (String p : params.split("\\s+")) {
                    if (!p.endsWith(":")) continue;
                    if (!text.contains(p)) addChip(p, null);
                }
            } else {
                mParamHint.setVisibility(View.GONE);
            }
        }
        mChipScroller.setVisibility(mChipRow.getChildCount() > 0 ? View.VISIBLE : View.GONE);
        mChipScroller.scrollTo(0, 0);
    }

    private void addChip(String label, String replaceAll) {
        if (mChipRow.getChildCount() >= 12) return;
        TextView chip = new TextView(mActivity);
        chip.setText(label);
        chip.setTypeface(Typeface.MONOSPACE);
        chip.setTextSize(13);
        chip.setTextColor(OutputFormatter.COL_CMD);
        chip.setBackgroundResource(R.drawable.chip_bg);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.setMargins(0, 0, 12, 0);
        chip.setLayoutParams(lp);
        int pad = (int) (mActivity.getResources().getDisplayMetrics().density * 6);
        chip.setPadding(pad * 2, pad, pad * 2, pad);
        chip.setOnClickListener(v -> {
            if (replaceAll != null) {
                mInput.setText(replaceAll);
            } else {
                String cur = mInput.getText().toString();
                if (!cur.endsWith(" ")) cur += " ";
                mInput.setText(cur + label);
            }
            mInput.setSelection(mInput.getText().length());
        });
        mChipRow.addView(chip);
    }

    // ---------------- execution ----------------

    public void runCMD(String zCommand) {
        if (mNode == null) return;

        String warning = CommandRegistry.dangerWarning(zCommand);
        if (warning != null) {
            new AlertDialog.Builder(mActivity)
                    .setTitle("⚠ Risky command")
                    .setMessage(warning)
                    .setPositiveButton("Run anyway", (d, w) -> execute(zCommand))
                    .setNegativeButton("Cancel", null)
                    .show();
            return;
        }
        execute(zCommand);
    }

    private void execute(String zCommand) {
        mHistory.addHistory(zCommand);
        appendEntry(OutputFormatter.colored("❯ " + zCommand, OutputFormatter.COL_CMD));

        final TextView result = appendEntry(OutputFormatter.colored("… running", OutputFormatter.COL_DIM));
        final long start = System.currentTimeMillis();

        mNode.cmd(zCommand, new NodeApi.Cb() {
            @Override
            public void onResult(JSONObject json) {
                long ms = System.currentTimeMillis() - start;
                boolean failed = !json.optBoolean("status", true);
                String pretty;
                try {
                    pretty = json.toString(2);
                } catch (JSONException e) {
                    pretty = json.toString();
                }
                result.setText(OutputFormatter.json(pretty, false));
                if (failed) {
                    result.append(OutputFormatter.colored("\n✖ command failed", OutputFormatter.COL_ERROR));
                }
                result.append(OutputFormatter.colored("\n⏱ " + ms + " ms", OutputFormatter.COL_DIM));
                scrollToBottom();
            }

            @Override
            public void onError(String message) {
                if (NodeApi.ERR_NOT_ENABLED.equals(message)) {
                    result.setText(OutputFormatter.colored(
                            "✖ This app is not enabled yet.\nOpen Minima Core → Apps and enable Terminal IDE.",
                            OutputFormatter.COL_ERROR));
                } else {
                    result.setText(OutputFormatter.colored("✖ " + message, OutputFormatter.COL_ERROR));
                }
                scrollToBottom();
            }
        });
    }

    // ---------------- output ----------------

    private TextView appendEntry(CharSequence text) {
        if (mOutput.getChildCount() >= MAX_ENTRIES) {
            mOutput.removeViewAt(0);
        }
        TextView tv = new TextView(mActivity);
        tv.setTypeface(Typeface.MONOSPACE);
        tv.setTextSize(13);
        tv.setText(text);
        tv.setPadding(0, 8, 0, 8);
        tv.setOnLongClickListener(v -> {
            ClipboardManager cb = (ClipboardManager) mActivity.getSystemService(Context.CLIPBOARD_SERVICE);
            cb.setPrimaryClip(ClipData.newPlainText("terminal", tv.getText().toString()));
            Toast.makeText(mActivity, "Copied", Toast.LENGTH_SHORT).show();
            return true;
        });
        mOutput.addView(tv);
        scrollToBottom();
        return tv;
    }

    private void scrollToBottom() {
        mScroller.post(() -> mScroller.fullScroll(View.FOCUS_DOWN));
    }
}
