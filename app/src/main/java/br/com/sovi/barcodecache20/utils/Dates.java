package br.com.sovi.barcodecache20.utils;

import java.text.SimpleDateFormat;

/**
 * Created by Joao on 18/04/2016.
 */
public abstract class Dates {

    public static final SimpleDateFormat sqlite_format = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");

    public static final SimpleDateFormat reading_date_format = new SimpleDateFormat("hh:mm:ss dd/MM/yy");

}
