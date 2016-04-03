package com.lwn314.coolweather.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.lwn314.coolweather.R;
import com.lwn314.coolweather.db.WeatherDB;
import com.lwn314.coolweather.service.AutoUpdateService;
import com.lwn314.coolweather.util.HttpCallbackListener;
import com.lwn314.coolweather.util.HttpUtil;
import com.lwn314.coolweather.util.Utility;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by lwn31 on 2016/4/2.
 */
public class WeatherActivity extends Activity implements View.OnClickListener {
    private LinearLayout weatherInfoLayout;
    private LinearLayout weatherLayout;
    private TextView cityNameText;
    private TextView publishText;
    private TextView temp1Text;
    private TextView temp2Text;
    private Button switchCity;
    private Button refreshWeather;
    private SensorManager sensorManager;
    private ImageView img;
    private TextView weather;
    private TextView weatherDay;
    private TextView weatherNight;
    private TextView qlty;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.weather_layout);
        weatherLayout = (LinearLayout) findViewById(R.id.weather_layout);
        img = (ImageView) findViewById(R.id.img);
        weatherNight = (TextView) findViewById(R.id.weatherNight);
        qlty = (TextView) findViewById(R.id.qlty);
        weather = (TextView) findViewById(R.id.weather);
        weatherDay = (TextView) findViewById(R.id.weatherDay);
        weatherInfoLayout = (LinearLayout) findViewById(R.id.weather_info_layout);
        cityNameText = (TextView) findViewById(R.id.city_name);
        publishText = (TextView) findViewById(R.id.publish_text);
        temp1Text = (TextView) findViewById(R.id.temp1);
        temp2Text = (TextView) findViewById(R.id.temp2);
        switchCity = (Button) findViewById(R.id.switch_city);
        refreshWeather = (Button) findViewById(R.id.refresh_weather);

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        Sensor sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sensorManager.registerListener(listener, sensor, SensorManager.SENSOR_DELAY_NORMAL);

        switchCity.setOnClickListener(this);
        refreshWeather.setOnClickListener(this);
        String countyCode = getIntent().getStringExtra("county_code");
        if (!TextUtils.isEmpty(countyCode)) {
            publishText.setText("同步中...");
            weatherInfoLayout.setVisibility(View.INVISIBLE);
            cityNameText.setVisibility(View.INVISIBLE);
            queryWeatherCode(countyCode);
        } else {
            showWeather();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (sensorManager != null) {
            sensorManager.unregisterListener(listener);
        }
    }

    private SensorEventListener listener = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent event) {
            float xValue = Math.abs(event.values[0]);
            float yValue = Math.abs(event.values[1]);
            float zValue = Math.abs(event.values[2]);
            if (xValue > 15 || yValue > 15 || zValue > 15) {
                publishText.setText("同步中...");
                SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences
                        (WeatherActivity.this);
                String weatherCode = preferences.getString("weatherCode", "");
                if (!TextUtils.isEmpty(weatherCode)) {
                    queryWeatherInfo(weatherCode);
                }
            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {

        }
    };

    private void queryWeatherCode(String countyCode) {
        String address;
        int cod = 0;
        try {
            cod = Integer.parseInt(countyCode);
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
        if (cod < 100000) {
            address = "http://www.weather.com.cn/data/list3/city0" + countyCode + ".xml";
        } else {
            address = "http://www.weather.com.cn/data/list3/city" + countyCode + ".xml";
        }
        queryFromServer(address, "countyCode");
    }

    private void queryWeatherInfo(String weatherCode) {
        String address = "https://api.heweather.com/x3/weather?cityid=CN" + weatherCode +
                "&key=444242ac0ed4459f92b1b5a89b5c1605";
        queryFromServer(address, "weatherCode");
    }

    private void queryFromServer(final String address, final String type) {
        HttpUtil.sendHttpRequest(address, new HttpCallbackListener() {
            @Override
            public void onFinish(final String response) {
                if ("countyCode".equals(type)) {
                    if (!TextUtils.isEmpty(response)) {
                        String[] array = response.split("\\|");
                        if (array != null && array.length > 0) {
                            String weatherCode = array[1];
                            queryWeatherInfo(weatherCode);
                        }
                    }
                } else if ("weatherCode".equals(type)) {
                    Utility.handleWeatherResponse(WeatherActivity.this, response);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            showWeather();
                        }
                    });
                }
            }

            @Override
            public void onError(Exception e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        publishText.setText("同步失败");
                    }
                });
            }
        });
    }


    private void queryFromWeatherDB() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(WeatherActivity.this);
                String weatherId = preferences.getString("weatherId", "");
                String url = WeatherDB.loadWeather(weatherId);
                final Bitmap bitmap = getHttpBitmap(url);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        img.setImageBitmap(bitmap);
                    }
                });
            }
        }).start();
    }

    private Bitmap getHttpBitmap(String url) {
        URL imgURL;
        Bitmap bitmap = null;
        try{
            imgURL = new URL(url);
            HttpURLConnection connection = (HttpURLConnection)imgURL.openConnection();
            connection.setConnectTimeout(6000);
            connection.setDoInput(true);
            connection.setUseCaches(false);
            InputStream inputStream = connection.getInputStream();
            bitmap = BitmapFactory.decodeStream(inputStream);
            inputStream.close();
        }catch (Exception e){
            e.printStackTrace();
        }
        return bitmap;
    }

    private void showWeather() {
        queryFromWeatherDB();
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        cityNameText.setText(preferences.getString("cityName", ""));
        temp1Text.setText(preferences.getString("tempmin", "") + "°");
        temp2Text.setText(preferences.getString("tempmax", "") + "°");
        qlty.setText(preferences.getString("cityAqi", "") + preferences.getString("qlty", ""));
        publishText.setText(preferences.getString("updateTime", "") + " 发布");
        weather.setText(preferences.getString("weatherDay", ""));
        weatherDay.setText(preferences.getString("weatherDay", ""));
        weatherNight.setText(preferences.getString("weatherNight", ""));
        if (preferences.getBoolean("dnequals", true)) {
            weatherLayout.setVisibility(View.GONE);
        } else {
            weather.setVisibility(View.GONE);
        }
        weatherInfoLayout.setVisibility(View.VISIBLE);
        cityNameText.setVisibility(View.VISIBLE);
        Intent intent = new Intent(this, AutoUpdateService.class);
        startService(intent);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.switch_city:
                Intent intent = new Intent(this, ChooseAreaActivity.class);
                intent.putExtra("from_weather_activity", true);
                startActivity(intent);
                finish();
                break;
            case R.id.refresh_weather:
                publishText.setText("同步中...");
                SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
                String weatherCode = preferences.getString("weatherCode", "");
                if (!TextUtils.isEmpty(weatherCode)) {
                    queryWeatherInfo(weatherCode);
                }
                break;
            default:
                break;
        }
    }
}