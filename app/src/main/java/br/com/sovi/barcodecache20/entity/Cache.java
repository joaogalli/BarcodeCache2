package br.com.sovi.barcodecache20.entity;

import android.database.Cursor;

/**
 * Created by Joao on 14/04/2016.
 */
public class Cache {

    public static final String TABLE_NAME = "caches";

    public static final String FIELD_ID = "_id";

    public static final String FIELD_NAME = "name";

    public static final String FIELD_COLOR = "color";

    public static final String FIELD_ARCHIVE = "archive";

    private int id, color;

    private String name;

    private boolean archive;

    public Cache() {
    }

    public Cache(Cursor cursor) {
        setId(cursor.getInt(cursor.getColumnIndex(FIELD_ID)));
        setName(cursor.getString(cursor.getColumnIndex(FIELD_NAME)));
        setColor(cursor.getInt(cursor.getColumnIndex(FIELD_COLOR)));
        setArchive(cursor.getInt(cursor.getColumnIndex(FIELD_ARCHIVE)) > 0);
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getColor() {
        return color;
    }

    public void setColor(int color) {
        this.color = color;
    }

    public boolean isArchive() {
        return archive;
    }

    public void setArchive(boolean archive) {
        this.archive = archive;
    }

    @Override
    public String toString() {
        return "Cache{" +
                "id=" + id +
                ", color=" + color +
                ", name='" + name + '\'' +
                ", archive=" + archive +
                '}';
    }
}
