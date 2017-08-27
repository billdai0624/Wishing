package com.intern.ab.starwish.util;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DBHelper extends SQLiteOpenHelper {
    private final static String DATABASE_NAME = "StarWish.db";
    private final static int DATABASE_VERSION = 2;

    public DBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String sqlWish = "CREATE TABLE wish(_Id INTEGER PRIMARY KEY AUTOINCREMENT, DeviceId TEXT, Wish TEXT" +
                ", DateTime TEXT, Longitude REAL, Latitude REAL, Cheering INTEGER, Public INTEGER, Realized INTEGER)";
        db.execSQL(sqlWish);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("drop table if exists wish");
        onCreate(db);
    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("drop table if exists wish");
        onCreate(db);
    }
}
