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
 * Professional terminal: persistent history, tab-driven completion dropdown (only
 * the chosen command's params/values, with descriptions mined from the node help),
 * colorized output entries with long-press copy, favorites, per-command timing, and
 * guards against known node-killer commands (unbounded coins/history).
 */
public class TerminalView extends BaseView {

    private static final int MAX_ENTRIES = 300;

    ScrollView mScroller;
    LinearLayout mOutput;
    CaretEditText mInput;
    LinearLayout mDropdown;
    ScrollView mDropdownScroller;
    TextView mParamHint;
    TextView mTabKey;

    // Live completion state: the current dropdown items + highlighted row.
    private List<Suggest.Item> mItems = new java.util.ArrayList<>();
    private int mSel = 0;

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
        mDropdown = mMainView.findViewById(R.id.terminal_dropdown);
        mDropdownScroller = mMainView.findViewById(R.id.terminal_dropdown_scroller);
        mParamHint = mMainView.findViewById(R.id.terminal_paramhint);
        mTabKey = mMainView.findViewById(R.id.terminal_tab);

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
                updateSuggestions();
            }
        });

        // The dropdown must track the caret, not just the text (tap to move the cursor).
        mInput.setOnCaretMoved(this::updateSuggestions);

        // Hardware keyboard: Tab accepts the highlighted completion, ↑/↓ move the
        // highlight while the dropdown is open.
        mInput.setOnKeyListener((v, keyCode, event) -> {
            if (event.getAction() != KeyEvent.ACTION_DOWN) return false;
            if (keyCode == KeyEvent.KEYCODE_TAB) {
                return acceptSelected();
            }
            if (!mItems.isEmpty()) {
                if (keyCode == KeyEvent.KEYCODE_DPAD_DOWN) { moveSelection(1); return true; }
                if (keyCode == KeyEvent.KEYCODE_DPAD_UP)   { moveSelection(-1); return true; }
            }
            return false;
        });

        // On-screen Tab key for the soft keyboard.
        mTabKey.setOnClickListener(v -> acceptSelected());

        Button run = mMainView.findViewById(R.id.terminal_send);
        run.setOnClickListener(v -> submit());

        TextView up = mMainView.findViewById(R.id.terminal_hist_up);
        TextView down = mMainView.findViewById(R.id.terminal_hist_down);
        up.setOnClickListener(v -> histNav(1));
        down.setOnClickListener(v -> histNav(-1));
        up.setOnLongClickListener(v -> { showHistoryDialog(); return true; });

        TextView fav = mMainView.findViewById(R.id.terminal_fav);
        fav.setOnClickListener(v -> showFavoritesDialog());
        fav.setOnLongClickListener(v -> { saveFavorite(); return true; });

        updateSuggestions();
    }

    public void setNodeApi(NodeApi zNode) {
        mNode = zNode;
    }

    private void banner() {
        appendEntry(OutputFormatter.colored(
                "╔══════════════════════════════╗\n"
              + "║   MINIMA  TERMINAL  IDE      ║\n"
              + "╚══════════════════════════════╝\n"
              + "Type a command — completions drop down as you type; ⇥ accepts.\n"
              + "▲/▼ = history (long-press ▲ for list) · ★ = favorites (long-press to save)\n"
              + "Long-press any output block to copy it.", OutputFormatter.COL_DIM));
    }

    public void clearTerminal() {
        mOutput.removeAllViews();
        banner();
    }

    @Override
    public void onDestroy() {
        if (mHistory != null) mHistory.close();
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

    // ---------------- completion dropdown ----------------

    private static final int MAX_ROWS = 40;      // hard cap on dropdown entries
    private static final int VISIBLE_DP = 234;   // ~6.5 rows before it scrolls

    private void updateSuggestions() {
        String text = mInput.getText().toString();
        int caret = mInput.getSelectionStart();
        Suggest.Result res = Suggest.suggest(mActivity, text, caret);

        if (res.paramHint != null) {
            mParamHint.setText(res.paramHint + "   (tap for help)");
            mParamHint.setVisibility(View.VISIBLE);
            final String helpCmd = res.paramHint.split(" ")[0];
            mParamHint.setOnClickListener(v -> showHelpDialog(helpCmd));
        } else {
            mParamHint.setVisibility(View.GONE);
        }

        mItems = res.items.size() > MAX_ROWS ? res.items.subList(0, MAX_ROWS) : res.items;
        mSel = 0;
        renderDropdown();
    }

    private void renderDropdown() {
        mDropdown.removeAllViews();
        if (mItems.isEmpty()) {
            mDropdownScroller.setVisibility(View.GONE);
            mTabKey.setVisibility(View.GONE);
            return;
        }
        float density = mActivity.getResources().getDisplayMetrics().density;
        int padH = (int) (density * 12);
        int padV = (int) (density * 8);

        for (int i = 0; i < mItems.size(); i++) {
            final Suggest.Item item = mItems.get(i);
            TextView row = new TextView(mActivity);
            row.setTypeface(Typeface.MONOSPACE);
            row.setTextSize(13);
            row.setSingleLine(true);
            row.setEllipsize(android.text.TextUtils.TruncateAt.END);
            row.setPadding(padH, padV, padH, padV);
            row.setText(rowText(item));
            row.setBackgroundColor(i == mSel ? 0x3382AAFF : 0x00000000);
            row.setOnClickListener(v -> accept(item));
            // Long-press a command row: its full help page, offline.
            if (item.kind == Suggest.KIND_COMMAND || HelpStore.has(mActivity, item.label)) {
                row.setOnLongClickListener(v -> {
                    showHelpDialog(item.label);
                    return true;
                });
            }
            mDropdown.addView(row);
        }

        // Cap the dropdown height; past ~6.5 rows it scrolls.
        ViewGroup.LayoutParams lp = mDropdownScroller.getLayoutParams();
        int cap = (int) (density * VISIBLE_DP);
        lp.height = mItems.size() > 6 ? cap : ViewGroup.LayoutParams.WRAP_CONTENT;
        mDropdownScroller.setLayoutParams(lp);
        mDropdownScroller.setVisibility(View.VISIBLE);
        mTabKey.setVisibility(View.VISIBLE);
        scrollSelectedIntoView();
    }

    /** One dropdown line: completion in blue, required marker, dim description. */
    private CharSequence rowText(Suggest.Item item) {
        android.text.SpannableStringBuilder sb = new android.text.SpannableStringBuilder();
        sb.append(OutputFormatter.colored(item.label, OutputFormatter.COL_CMD));
        if (item.required) {
            sb.append(OutputFormatter.colored(" required", OutputFormatter.COL_NUMBER));
        }
        if (!item.desc.isEmpty()) {
            sb.append(OutputFormatter.colored("  " + item.desc, OutputFormatter.COL_DIM));
        }
        return sb;
    }

    private void moveSelection(int direction) {
        if (mItems.isEmpty()) return;
        mSel = (mSel + direction + mItems.size()) % mItems.size();
        renderDropdown();
    }

    private void scrollSelectedIntoView() {
        if (mSel < 0 || mSel >= mDropdown.getChildCount()) return;
        final View row = mDropdown.getChildAt(mSel);
        mDropdownScroller.post(() -> {
            int top = row.getTop();
            int bottom = row.getBottom();
            int visTop = mDropdownScroller.getScrollY();
            int visBottom = visTop + mDropdownScroller.getHeight();
            if (top < visTop) mDropdownScroller.smoothScrollTo(0, top);
            else if (bottom > visBottom) mDropdownScroller.smoothScrollTo(0, bottom - mDropdownScroller.getHeight());
        });
    }

    /** Tab: accept the highlighted completion. Returns true if one was applied. */
    private boolean acceptSelected() {
        if (mItems.isEmpty()) return false;
        accept(mItems.get(Math.min(mSel, mItems.size() - 1)));
        return true;
    }

    private void accept(Suggest.Item item) {
        // Re-derive against the CURRENT text + caret (the field may have changed
        // since the row was built), splicing the token rather than replacing all.
        String cur = mInput.getText().toString();
        int caret = mInput.getSelectionStart();
        mInput.setText(Suggest.apply(cur, caret, item));
        mInput.setSelection(Suggest.applyCaret(cur, caret, item));
    }

    private void showHelpDialog(String command) {
        String full = HelpStore.full(mActivity, command);
        if (full == null) return;
        TextView tv = new TextView(mActivity);
        tv.setTypeface(Typeface.MONOSPACE);
        tv.setTextSize(12);
        tv.setTextColor(OutputFormatter.COL_PLAIN);
        tv.setTextIsSelectable(true);
        tv.setText(full);
        int pad = (int) (mActivity.getResources().getDisplayMetrics().density * 16);
        ScrollView scroller = new ScrollView(mActivity);
        scroller.setPadding(pad, pad / 2, pad, pad / 2);
        scroller.addView(tv);
        new AlertDialog.Builder(mActivity)
                .setTitle(command)
                .setView(scroller)
                .setPositiveButton("Close", null)
                .show();
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

                CharSequence rendered = renderHelp(zCommand, json);
                if (rendered == null) {
                    String pretty;
                    try {
                        pretty = json.toString(2);
                    } catch (JSONException e) {
                        pretty = json.toString();
                    }
                    // Render embedded multi-line text as real lines, not \n-escaped strings.
                    pretty = pretty.replace("\\n", "\n").replace("\\t", "    ");
                    rendered = OutputFormatter.json(pretty, false);
                }
                result.setText(rendered);
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

    /**
     * Dedicated renderer for help — a terminal shows help as a formatted page,
     * not a JSON blob. Returns null for non-help commands (fall back to JSON).
     */
    private CharSequence renderHelp(String command, JSONObject json) {
        if (!command.trim().startsWith("help")) return null;
        JSONObject resp = json.optJSONObject("response");
        if (resp == null) return null;

        // help command:x -> title + full help page.
        String fullhelp = resp.optString("fullhelp", "");
        if (!fullhelp.isEmpty()) {
            android.text.SpannableStringBuilder sb = new android.text.SpannableStringBuilder();
            sb.append(OutputFormatter.colored(resp.optString("command", "") + "  "
                    + resp.optString("help", ""), OutputFormatter.COL_KEY));
            sb.append(OutputFormatter.colored("\n\n" + fullhelp.replace("\\n", "\n")
                    .replace("\\t", "    ").trim(), OutputFormatter.COL_PLAIN));
            return sb;
        }

        // A `help command:x` reply with no fullhelp must NOT fall into the bare-listing
        // branch (it would print the response's own keys as if they were commands).
        if (command.contains("command:")) return null;

        // bare help -> aligned "command  description" listing.
        java.util.Iterator<String> keys = resp.keys();
        java.util.List<String> names = new java.util.ArrayList<>();
        while (keys.hasNext()) {
            String k = keys.next();
            if (resp.opt(k) instanceof String) names.add(k);
        }
        if (names.isEmpty()) return null;
        java.util.Collections.sort(names);
        int width = 0;
        for (String n : names) width = Math.max(width, n.length());
        android.text.SpannableStringBuilder sb = new android.text.SpannableStringBuilder();
        for (String n : names) {
            StringBuilder pad = new StringBuilder(n);
            while (pad.length() < width + 2) pad.append(' ');
            sb.append(OutputFormatter.colored(pad.toString(), OutputFormatter.COL_KEY));
            sb.append(OutputFormatter.colored(resp.optString(n) + "\n", OutputFormatter.COL_PLAIN));
        }
        return sb;
    }

    /** Full session text (for sharing/export). */
    public String exportText() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < mOutput.getChildCount(); i++) {
            sb.append(((TextView) mOutput.getChildAt(i)).getText()).append("\n\n");
        }
        return sb.toString();
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
        // Native selection: long-press selects, drag handles, Copy/Select-all toolbar.
        tv.setTextIsSelectable(true);
        mOutput.addView(tv);
        scrollToBottom();
        return tv;
    }

    private void scrollToBottom() {
        mScroller.post(() -> mScroller.fullScroll(View.FOCUS_DOWN));
    }
}
