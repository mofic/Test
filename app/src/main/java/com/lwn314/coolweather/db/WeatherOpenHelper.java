package com.lwn314.coolweather.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by lwn31 on 2016/4/2.
 */
public class WeatherOpenHelper extends SQLiteOpenHelper{

    public static final String CREATE_WEATHER = "create table weather("
            + "id integer primary key autoincrement ,"
            + "code integer ,"
            + "icon text )";

    public WeatherOpenHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version){
        super(context,name,factory,version);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_WEATHER);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
