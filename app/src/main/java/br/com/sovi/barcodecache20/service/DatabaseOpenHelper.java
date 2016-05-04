package br.com.sovi.barcodecache20.service;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import br.com.sovi.barcodecache20.entity.Cache;
import br.com.sovi.barcodecache20.entity.Reading;

/**
 * Created by Joao on 14/04/2016.
 */
public class DatabaseOpenHelper extends SQLiteOpenHelper {
    private static final String TAG = "DatabaseOpenHelper";

    private static final int DATABASE_VERSION = 1;

    private static final String DATABASE_NAME = "barcodecache2";

    private static final String CACHE_TABLE_CREATION_SQL = "CREATE TABLE " + Cache.TABLE_NAME + " (" + Cache.FIELD_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
            Cache.FIELD_NAME + " TEXT NOT NULL, " + Cache.FIELD_COLOR + " INTEGER, " + Cache.FIELD_ARCHIVE + " INTEGER)";


    private static final String READING_TABLE_CREATION_SQL = "CREATE TABLE " + Reading.TABLE_NAME + " (" + Reading.FIELD_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
            Reading.FIELD_CONTENT + " TEXT NOT NULL, " + Reading.FIELD_DATE + " DATETIME DEFAULT CURRENT_TIMESTAMP, " + Reading.FIELD_CACHE + " INTEGER)";

    public DatabaseOpenHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CACHE_TABLE_CREATION_SQL);
        db.execSQL(READING_TABLE_CREATION_SQL);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.i(TAG, "Upgrading from " + oldVersion + " to " + newVersion);
    }
}
