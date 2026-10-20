package com.eurobuddha.terminalide;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.ScrollView;
import android.widget.TextView;
import com.eurobuddha.terminalide.receiver.ReceiverDB;
import com.eurobuddha.terminalide.terminal.TerminalOutput;
import java.util.concurrent.ThreadPoolExecutor;

/** A background-loaded, bounded preview of the persisted node logs. */
public class LogsView extends BaseView {
    private final TextView text;
    private final ScrollView scroller;
    private final Context context;
    private final Handler main = new Handler(Looper.getMainLooper());
    private final ThreadPoolExecutor worker = TerminalOutput.worker();
    private boolean loading;
    private boolean dirty;   // a refresh asked for during a load; re-run once it finishes
    private boolean destroyed;

    public LogsView(Activity activity) {
        super(activity, R.layout.view_logs);
        context = activity.getApplicationContext();
        text = getMainView().findViewById(R.id.logs_maintext);
        scroller = getMainView().findViewById(R.id.terminal_scroller);
    }

    @Override public void refreshView() {
        if (destroyed) return;
        if (loading) { dirty = true; return; }
        loading = true;
        dirty = false;
        worker.execute(() -> {
            String snapshot = null;
            try (ReceiverDB db = new ReceiverDB(context)) {
                snapshot = db.getRecentLogPreview();
            } catch (RuntimeException error) {
                // Keep database details out of the UI; the user can retry by reopening this tab.
                android.util.Log.w("LogsView", "log preview failed", error);
            }
            final String result = snapshot;
            main.post(() -> {
                loading = false;
                if (destroyed) return;
                if (result == null) text.setText("Could not load logs. Reopen this tab to retry.");
                else text.setText(result);   // the one-line hint lives in the layout (logs_hint)
                // Newest entries are at the end: open at the bottom, like the terminal tab does
                // for fresh output (posted so it runs after the new text has been laid out).
                scroller.post(() -> scroller.fullScroll(View.FOCUS_DOWN));
                if (dirty) refreshView();
            });
        });
    }

    @Override public void onDestroy() {
        destroyed = true;
        worker.shutdownNow();
        main.removeCallbacksAndMessages(null);
    }
}
