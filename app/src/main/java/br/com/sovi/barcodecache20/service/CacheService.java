package br.com.sovi.barcodecache20.service;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import br.com.sovi.barcodecache20.entity.Cache;
import br.com.sovi.barcodecache20.entity.Reading;

/**
 * Created by Joao on 14/04/2016.
 */
public class CacheService {

    private DatabaseOpenHelper cacheOpenHelper;

    private SQLiteDatabase db;

    public CacheService(Context context) {
        cacheOpenHelper = new DatabaseOpenHelper(context);
        db = cacheOpenHelper.getWritableDatabase();
    }

    public Cursor findAll() {
        return db.rawQuery("select * from " + Cache.TABLE_NAME + " where " + Cache.FIELD_ARCHIVE + " = 0", null);
    }

    public Cursor findArchived() {
        return db.rawQuery("select * from " + Cache.TABLE_NAME + " where " + Cache.FIELD_ARCHIVE + " = 1", null);
    }

    public boolean save(int id, String name, int color, boolean archive) {
        ContentValues values = new ContentValues();
        values.put(Cache.FIELD_NAME, name);
        values.put(Cache.FIELD_COLOR, color);
        values.put(Cache.FIELD_ARCHIVE, archive ? 1 : 0);

        if (id > 0) {
            return db.update(Cache.TABLE_NAME, values, Cache.FIELD_ID + " = ?", new String[]{Integer.toString(id)}) > 0;
        } else {
            db.insert(Cache.TABLE_NAME, null, values);
            return true;
        }
    }

    public boolean save(Cache cache) {
        return save(cache.getId(), cache.getName(), cache.getColor(), cache.isArchive());
    }

    public Cache findById(int cacheId) {
        Cursor cursor = db.rawQuery("SELECT * FROM " + Cache.TABLE_NAME + " WHERE " + Cache.FIELD_ID + " = " + cacheId, null);
        cursor.moveToFirst();
        Cache cache = new Cache(cursor);
        cursor.close();
        return cache;
    }

    public void toggleArchiveCache(int cacheId) {
        Cache cache = findById(cacheId);
        if (cache != null) {
            cache.setArchive(!cache.isArchive());
            save(cache);
        }
    }
}
