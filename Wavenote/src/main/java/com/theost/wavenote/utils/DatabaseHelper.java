package com.theost.wavenote.utils;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

public class DatabaseHelper extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "wavenote-local.db";

    public static final String NAME_IMAGES_TABLE = "images_table";
    public static final String NAME_DICTIONARY_TABLE = "dictionary_table";
    public static final String NAME_METRONOME_TABLE = "metronome_table";

    public static final String COL_0 = "ID";

    public static final String COL_1_IMAGES = "NOTE_ID";
    public static final String COL_2_IMAGES = "NAME";
    public static final String COL_3_IMAGES = "URI";
    public static final String COL_4_IMAGES = "DATE";

    public static final String COL_1_DICIONARY = "WORD";
    public static final String COL_2_DICIONARY = "TYPE";

    public static final String COL_1_METRONOME = "SOUND";

    public DatabaseHelper(@Nullable Context context) {
        super(context, DATABASE_NAME, null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS " + NAME_IMAGES_TABLE + " (" + COL_0 + " INTEGER PRIMARY KEY, " + COL_1_IMAGES
                + " TEXT NOT NULL, " + COL_2_IMAGES + " TEXT, " + COL_3_IMAGES + " TEXT NOT NULL, " + COL_4_IMAGES + " TEXT)");
        db.execSQL("CREATE TABLE IF NOT EXISTS " + NAME_DICTIONARY_TABLE + " (" + COL_0 + " INTEGER PRIMARY KEY, " + COL_1_DICIONARY
                + " TEXT NOT NULL, " + COL_2_DICIONARY + " TEXT NOT NULL)");
        db.execSQL("CREATE TABLE IF NOT EXISTS " + NAME_METRONOME_TABLE + " (" + COL_0 + " INTEGER PRIMARY KEY, " + COL_1_METRONOME
                + " TEXT NOT NULL)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + NAME_IMAGES_TABLE);
        db.execSQL("DROP TABLE IF EXISTS " + NAME_DICTIONARY_TABLE);
        db.execSQL("DROP TABLE IF EXISTS " + NAME_METRONOME_TABLE);
        onCreate(db);
    }

    @Override
    protected void finalize() throws Throwable {
        this.close();
        super.finalize();
    }

    public boolean insertImageData(String noteId, String name, String uri, String date) {
        SQLiteDatabase db = this.getReadableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COL_1_IMAGES, noteId);
        contentValues.put(COL_2_IMAGES, name);
        contentValues.put(COL_3_IMAGES, uri);
        contentValues.put(COL_4_IMAGES, date);
        long result = db.insert(NAME_IMAGES_TABLE, null, contentValues);
        return result != -1;
    }

    public Cursor getImageData(String noteId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor result;
        try {
            result = db.rawQuery("SELECT * FROM " + NAME_IMAGES_TABLE
                    + " WHERE " + COL_1_IMAGES + "='" + noteId + "'", null);
        } catch (Exception e) {
            e.printStackTrace();
            result = null;
        }
        return result;
    }

    @SuppressLint("Recycle")
    public String getImageUri(String id) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor result;
        String uri;
        try {
            result = db.rawQuery("SELECT " + COL_3_IMAGES + " FROM " + NAME_IMAGES_TABLE
                    + " WHERE " + COL_0 + "='" + id + "'", null);
        } catch (Exception e) {
            e.printStackTrace();
            result = null;
        }
        if (result.getCount() != 0) {
            result.moveToFirst();
            return result.getString(0);
        }
        return null;
    }

    public boolean renameImageData(String id, String name) {
        SQLiteDatabase db = this.getReadableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COL_2_IMAGES, name);
        long result = db.update(NAME_IMAGES_TABLE, contentValues, COL_0 + "=" + id, null);
        return result != -1;
    }

    public void removeImageData(String id) {
        SQLiteDatabase db = this.getReadableDatabase();
        long result = db.delete(NAME_IMAGES_TABLE, COL_0 + "=" + id, null);
    }

    public void removeAllImageData(String noteId) {
        SQLiteDatabase db = this.getReadableDatabase();
        long result = db.delete(NAME_IMAGES_TABLE, COL_1_IMAGES + "='" + noteId + "'", null);
    }

    public boolean insertDictionaryData(String keyword, String type) {
        SQLiteDatabase db = this.getReadableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COL_1_DICIONARY, keyword);
        contentValues.put(COL_2_DICIONARY, type);
        long result = db.insert(NAME_DICTIONARY_TABLE, null, contentValues);
        return result != -1;
    }

    public Cursor getDictionaryData(String type) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT " + COL_1_DICIONARY + " FROM " + NAME_DICTIONARY_TABLE
                + " WHERE " + COL_2_DICIONARY + "='" + type + "'", null);
    }

    public Cursor getAllDictionaryData() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM " + NAME_DICTIONARY_TABLE, null);
    }

    public boolean renameDictionaryData(String id, String type) {
        SQLiteDatabase db = this.getReadableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COL_2_DICIONARY, type);
        long result = db.update(NAME_DICTIONARY_TABLE, contentValues, COL_0 + "=" + id, null);
        return result != -1;
    }

    public boolean removeDictionaryData(String id) {
        SQLiteDatabase db = this.getReadableDatabase();
        long result = db.delete(NAME_DICTIONARY_TABLE, COL_0 + "=" + id, null);
        return result != -1;
    }

    public boolean insertMetronomeData(String sound) {
        SQLiteDatabase db = this.getReadableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COL_1_METRONOME, sound);
        long result = db.insert(NAME_METRONOME_TABLE, null, contentValues);
        return result != -1;
    }

    public Cursor getMetronomeData() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM " + NAME_METRONOME_TABLE, null);
    }

    public boolean removeMetronomeData(String id) {
        SQLiteDatabase db = this.getReadableDatabase();
        long result = db.delete(NAME_METRONOME_TABLE, COL_0 + "=" + id, null);
        return result != -1;
    }

}
