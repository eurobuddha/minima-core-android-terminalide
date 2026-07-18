package com.eurobuddha.terminalide.ide;

import android.app.Activity;
import android.content.Intent;
import android.text.format.DateUtils;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

import com.eurobuddha.terminalide.BaseView;
import com.eurobuddha.terminalide.R;

import java.util.ArrayList;
import java.util.List;

/** Script library tab: list, create (blank/template), open in editor, duplicate/delete. */
public class ScriptsView extends BaseView {

    ScriptDB mDB;
    ListView mList;
    TextView mEmpty;
    List<ScriptDB.Script> mScripts = new ArrayList<>();

    public ScriptsView(Activity zActivity) {
        super(zActivity, R.layout.view_scripts);

        mDB = new ScriptDB(zActivity);
        mList = mMainView.findViewById(R.id.scripts_list);
        mEmpty = mMainView.findViewById(R.id.scripts_empty);

        Button add = mMainView.findViewById(R.id.scripts_new);
        add.setOnClickListener(v -> newScriptDialog());

        mList.setOnItemClickListener((parent, view, pos, id) -> openEditor(mScripts.get(pos).id));
        mList.setOnItemLongClickListener((parent, view, pos, id) -> {
            scriptActionsDialog(mScripts.get(pos));
            return true;
        });

        refreshView();
    }

    @Override
    public void refreshView() {
        mScripts = mDB.getAll();
        List<String> labels = new ArrayList<>();
        for (ScriptDB.Script s : mScripts) {
            String when = DateUtils.getRelativeTimeSpanString(s.modified).toString();
            String deployed = s.address.isEmpty() ? "" : "  ⛓ " + shorten(s.address);
            labels.add(s.name + "\n" + s.source.trim().length() + " chars · " + when + deployed);
        }
        mList.setAdapter(new ArrayAdapter<>(mActivity, android.R.layout.simple_list_item_1, labels));
        mEmpty.setVisibility(mScripts.isEmpty() ? android.view.View.VISIBLE : android.view.View.GONE);
    }

    private static String shorten(String addr) {
        return addr.length() > 14 ? addr.substring(0, 10) + "…" + addr.substring(addr.length() - 4) : addr;
    }

    private void newScriptDialog() {
        new AlertDialog.Builder(mActivity)
                .setTitle("New script from…")
                .setItems(Templates.names(), (d, which) -> nameDialog(which))
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void nameDialog(int templateIdx) {
        EditText name = new EditText(mActivity);
        name.setHint("Script name");
        new AlertDialog.Builder(mActivity)
                .setTitle("Name the script")
                .setView(name)
                .setPositiveButton("Create", (d, w) -> {
                    String n = name.getText().toString().trim();
                    if (n.isEmpty()) n = Templates.ALL[templateIdx][0].equals("Blank")
                            ? "untitled" : Templates.ALL[templateIdx][0];
                    long id = mDB.insert(n, Templates.ALL[templateIdx][1]);
                    refreshView();
                    openEditor(id);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void scriptActionsDialog(ScriptDB.Script s) {
        new AlertDialog.Builder(mActivity)
                .setTitle(s.name)
                .setItems(new String[]{"Open", "Duplicate", "Delete"}, (d, which) -> {
                    if (which == 0) {
                        openEditor(s.id);
                    } else if (which == 1) {
                        mDB.insert(s.name + " copy", s.source);
                        refreshView();
                    } else {
                        new AlertDialog.Builder(mActivity)
                                .setTitle("Delete '" + s.name + "'?")
                                .setMessage("This only deletes the local copy. A deployed script "
                                        + "stays on the node (use removescript to untrack it).")
                                .setPositiveButton("Delete", (dd, ww) -> {
                                    mDB.delete(s.id);
                                    refreshView();
                                    Toast.makeText(mActivity, "Deleted", Toast.LENGTH_SHORT).show();
                                })
                                .setNegativeButton("Cancel", null)
                                .show();
                    }
                })
                .setNegativeButton("Close", null)
                .show();
    }

    private void openEditor(long scriptId) {
        Intent i = new Intent(mActivity, ScriptEditorActivity.class);
        i.putExtra("script_id", scriptId);
        mActivity.startActivity(i);
    }
}
