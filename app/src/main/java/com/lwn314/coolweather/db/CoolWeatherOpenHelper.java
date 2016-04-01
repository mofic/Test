package com.lwn314.coolweather.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by lwn31 on 2016/3/31.
 */
public class CoolWeatherOpenHelper extends SQLiteOpenHelper{

    /**
     * Province表建表语句
     */
    public static final String CREATE_PROVINCE = "create table Province ("
                    + "id integer primary key autoincrement ,"
                    + "province_name text, "
                    + "province_code integer)";

    /**
     * City表建表语句
     */
    public static final String CREATE_CITY = "create table Province ("
            + "id integer primary key autoincrement ,"
            + "city_name text, "
            + "city_code integer,"
            + "province_id integer)";

    /**
     * County表建表语句
     */
    public static final String CREATE_COUNTY = "create table Province ("
            + "id integer primary key autoincrement ,"
            + "county_name text, "
            + "county_code integer,"
            + "city_id integer)";

    public CoolWeatherOpenHelper(Context context,String name,SQLiteDatabase.CursorFactory factory,int version){
        super(context,name,factory,version);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}