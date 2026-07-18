package com.eurobuddha.terminalide.ide;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

/** Local library of KISS VM scripts. */
public class ScriptDB extends SQLiteOpenHelper {

    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "scripts.db";

    public static class Script {
        public long id;
        public String name;
        public String source;
        public long modified;
        public String address;   // deployed address, or ""
    }

    private final SQLiteDatabase mDB;

    public ScriptDB(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        mDB = getWritableDatabase();
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("create table if not exists scripts ("
                + "_id integer primary key,"
                + "name text not null,"
                + "source text not null,"
                + "modified integer not null,"
                + "address text not null default '')");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Never drop user scripts on upgrade; migrations added per-version when needed.
    }

    public long insert(String name, String source) {
        ContentValues v = new ContentValues();
        v.put("name", name);
        v.put("source", source);
        v.put("modified", System.currentTimeMillis());
        v.put("address", "");
        return mDB.insert("scripts", null, v);
    }

    public void update(long id, String name, String source) {
        ContentValues v = new ContentValues();
        v.put("name", name);
        v.put("source", source);
        v.put("modified", System.currentTimeMillis());
        mDB.update("scripts", v, "_id=?", new String[]{"" + id});
    }

    public void setAddress(long id, String address) {
        ContentValues v = new ContentValues();
        v.put("address", address);
        mDB.update("scripts", v, "_id=?", new String[]{"" + id});
    }

    public void delete(long id) {
        mDB.delete("scripts", "_id=?", new String[]{"" + id});
    }

    public Script get(long id) {
        Cursor c = mDB.query("scripts", null, "_id=?", new String[]{"" + id}, null, null, null);
        try {
            if (c.moveToFirst()) return fromCursor(c);
        } finally {
            c.close();
        }
        return null;
    }

    public List<Script> getAll() {
        List<Script> out = new ArrayList<>();
        Cursor c = mDB.query("scripts", null, null, null, null, null, "modified DESC");
        try {
            while (c.moveToNext()) out.add(fromCursor(c));
        } finally {
            c.close();
        }
        return out;
    }

    private Script fromCursor(Cursor c) {
        Script s = new Script();
        s.id = c.getLong(c.getColumnIndexOrThrow("_id"));
        s.name = c.getString(c.getColumnIndexOrThrow("name"));
        s.source = c.getString(c.getColumnIndexOrThrow("source"));
        s.modified = c.getLong(c.getColumnIndexOrThrow("modified"));
        s.address = c.getString(c.getColumnIndexOrThrow("address"));
        return s;
    }
}
