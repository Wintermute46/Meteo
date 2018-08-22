package com.geek.hw.meteo.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DbHelper extends SQLiteOpenHelper {

    private final static String DB_NAME = "weather.db";
    private final static int DB_VERSION = 3;

    public DbHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqdb) {
        sqdb.execSQL(DbContract.SQL_CREATE_WEATHER);
        sqdb.execSQL(DbContract.SQL_CREATE_METAR);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqdb, int i, int i1) {
        if ((i == 2) && (i1 == 3)) {
            sqdb.execSQL(DbContract.SQL_DELETE_WEATHER);
            sqdb.execSQL(DbContract.SQL_DELETE_METAR);
            onCreate(sqdb);
        }
    }
}
