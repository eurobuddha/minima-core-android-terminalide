package com.eurobuddha.terminalide;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.widget.TextView;
import com.eurobuddha.terminalide.receiver.ReceiverDB;
import com.eurobuddha.terminalide.terminal.TerminalOutput;
import java.util.concurrent.ThreadPoolExecutor;

/** A background-loaded, bounded preview of the persisted node logs. */
public class LogsView extends BaseView {
    private final TextView text;
    private final Context context;
    private final Handler main = new Handler(Looper.getMainLooper());
    private final ThreadPoolExecutor worker = TerminalOutput.worker();
    private boolean loading;
    private boolean destroyed;

    public LogsView(Activity activity) {
        super(activity, R.layout.view_logs);
        context = activity.getApplicationContext();
        text = getMainView().findViewById(R.id.logs_maintext);
    }

    @Override public void refreshView() {
        if (destroyed || loading) return;
        loading = true;
        worker.execute(() -> {
            String snapshot = null;
            try (ReceiverDB db = new ReceiverDB(context)) {
                snapshot = db.getRecentLogPreview();
            } catch (RuntimeException error) {
                // Keep database details out of the UI; the user can retry by reopening this tab.
            }
            final String result = snapshot;
            main.post(() -> {
                loading = false;
                if (destroyed) return;
                if (result == null) text.setText("Could not load logs. Reopen this tab to retry.");
                else text.setText("Recent log preview · long entries are shortened. Stored logs are unchanged." + "\n\n" + result);
            });
        });
    }

    @Override public void onDestroy() {
        destroyed = true;
        worker.shutdownNow();
        main.removeCallbacksAndMessages(null);
    }
}
