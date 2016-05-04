package br.com.sovi.barcodecache20.service;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import br.com.sovi.barcodecache20.entity.Cache;
import br.com.sovi.barcodecache20.entity.Reading;

/**
 * Created by Joao on 18/04/2016.
 */
public class ReadingService {

    private DatabaseOpenHelper helper;

    private SQLiteDatabase db;

    public ReadingService(Context context) {
        helper = new DatabaseOpenHelper(context);
        db = helper.getWritableDatabase();
    }

    public boolean save(Reading reading) {
        ContentValues values = new ContentValues();
        values.put(Reading.FIELD_CONTENT, reading.getContent());
        values.put(Reading.FIELD_CACHE, reading.getCache());

        if (reading.getId() > 0) {
            return db.update(Reading.TABLE_NAME, values, Cache.FIELD_ID + " = ?", new String[]{Integer.toString(reading.getId())}) > 0;
        } else {
            db.insert(Reading.TABLE_NAME, null, values);
            return true;
        }
    }

    public int countByCache(int cacheId) {
        Cursor mCount = db.rawQuery("select count(*) from " + Reading.TABLE_NAME + " where cache = " + cacheId, null);
        mCount.moveToFirst();
        int count = mCount.getInt(0);
        mCount.close();
        return count;
    }

    public Cursor findFromCache(int cacheId) {
        return db.rawQuery("select * from " + Reading.TABLE_NAME + " where cache = " + cacheId, null);
    }

    public Cursor findLastFromCache(int cacheId) {
        return db.rawQuery("select * from " + Reading.TABLE_NAME + " where cache = " + cacheId + " order by " + Reading.FIELD_DATE + " desc", null);
    }

    public int clearCache(int cacheId) {
        return db.delete(Reading.TABLE_NAME, "cache = ?", new String[]{Integer.toString(cacheId)});
    }
}
