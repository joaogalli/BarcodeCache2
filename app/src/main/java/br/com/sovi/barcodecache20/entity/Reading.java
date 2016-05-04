package br.com.sovi.barcodecache20.entity;

import android.database.Cursor;
import android.util.Log;

import java.text.ParseException;
import java.util.Date;

import br.com.sovi.barcodecache20.utils.Dates;

/**
 * Created by Joao on 18/04/2016.
 */
public class Reading {

    public static final String TABLE_NAME = "readings";

    public static final String FIELD_ID = "_id";

    public static final String FIELD_CONTENT = "content";

    public static final String FIELD_DATE = "date";

    public static final String FIELD_CACHE = "cache";

    private int id;

    private String content;

    private Date date;

    private int cache;

    public Reading() {
    }

    public Reading(Cursor cursor) {
        setId(cursor.getInt(cursor.getColumnIndex(FIELD_ID)));
        setContent(cursor.getString(cursor.getColumnIndex(FIELD_CONTENT)));

        try {
            setDate(Dates.sqlite_format.parse(cursor.getString(cursor.getColumnIndex(FIELD_DATE))));
        } catch (ParseException e) {
            e.printStackTrace();
        }

        setCache(cursor.getInt(cursor.getColumnIndex(FIELD_CACHE)));
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public int getCache() {
        return cache;
    }

    public void setCache(int cache) {
        this.cache = cache;
    }
}
