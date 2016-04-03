package com.lwn314.coolweather.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.lwn314.coolweather.model.Weather;

/**
 * Created by lwn31 on 2016/4/2.
 */
public class WeatherDB {

    private static final String DB_NAME = "weather";
    private static final int VERSION = 1;
    private static WeatherDB weatherDB;
    private SQLiteDatabase db;

    private WeatherDB(Context context){
        WeatherOpenHelper weatherOpenHelper = new WeatherOpenHelper(context,DB_NAME,null,VERSION);
        db = weatherOpenHelper.getWritableDatabase();
    }

    public synchronized static WeatherDB getInstance(Context context){
        if (weatherDB == null){
            weatherDB = new WeatherDB(context);
        }
        return  weatherDB;
    }

    public void saveWeather(Weather weather){
        if (weather != null){
            ContentValues values = new ContentValues();
            values.put("code",weather.getCode());
            values.put("icon",weather.getIcon());
            db.insert("weather",null,values);
        }
    }

    public static String loadWeather(String weatherId){
        Cursor cursor = weatherDB.db.query("weather",null,"code = ?",new String[]{weatherId},null,null,null);
        String icon = null;
        if (cursor.moveToFirst()){
             icon = cursor.getString(cursor.getColumnIndex("icon"));
        }
        return icon;
    }
}
