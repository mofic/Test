package com.lwn314.coolweather.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.text.TextUtils;

import com.lwn314.coolweather.db.CoolWeatherDB;
import com.lwn314.coolweather.db.WeatherDB;
import com.lwn314.coolweather.model.City;
import com.lwn314.coolweather.model.County;
import com.lwn314.coolweather.model.Province;
import com.lwn314.coolweather.model.Weather;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by lwn31 on 2016/4/1.
 */
public class Utility {

    public static int flag;

    public synchronized static void handleWeatherResponse(WeatherDB weatherDB,String response){
        if (!TextUtils.isEmpty(response)){
            try {
                JSONObject jsonObject = new JSONObject(response);
                JSONArray condInfo = jsonObject.getJSONArray("cond_info");
                for (int i = 0 ; i < condInfo.length() ; i++){
                    Weather weather = new Weather();
                    JSONObject object = condInfo.getJSONObject(i);
                    weather.setCode(object.getString("code"));
                    weather.setIcon(object.getString("icon"));
                    weatherDB.saveWeather(weather);
                    flag = 1;
                }
            }catch (JSONException e){
                e.printStackTrace();
            }
        }
    }

    public static boolean isInit(){
        if (flag == 1){
            return true;
        }else {
            return false;
        }
    }

    public synchronized static boolean handleProvincesResponse(CoolWeatherDB coolWeatherDB,
                                                               String response) {
        if (!TextUtils.isEmpty(response)) {
            String[] allProvince = response.split(",");
            if (allProvince != null && allProvince.length > 0) {
                for (String p : allProvince) {
                    String[] array = p.split("\\|");
                    Province province = new Province();
                    province.setProvinceCode(array[0]);
                    province.setProvinceName(array[1]);
                    coolWeatherDB.saveProvince(province);
                }
                return true;
            }
        }
        return false;
    }

    public synchronized static boolean handleCitiesResponse(CoolWeatherDB coolWeatherDB, String
            response, int provinceId) {
        if (!TextUtils.isEmpty(response)) {
            String[] allCities = response.split(",");
            if (allCities != null && allCities.length > 0) {
                for (String c : allCities) {
                    String[] array = c.split("\\|");
                    City city = new City();
                    city.setCityCode(array[0]);
                    city.setCityName(array[1]);
                    city.setProvinceId(provinceId);
                    coolWeatherDB.saveCity(city);
                }
                return true;
            }
        }
        return false;
    }

    public synchronized static boolean handleCountriesResponse(CoolWeatherDB coolWeatherDB,
                                                               String response, int cityId) {
        if (!TextUtils.isEmpty(response)) {
            String[] allCounties = response.split(",");
            if (allCounties != null && allCounties.length > 0) {
                for (String c : allCounties) {
                    String[] array = c.split("\\|");
                    County county = new County();
                    county.setCountyCode(array[0]);
                    county.setCountyName(array[1]);
                    county.setcityId(cityId);
                    coolWeatherDB.saveCounty(county);
                }
                return true;
            }
        }
        return false;
    }

    public static void handleWeatherResponse(Context context, String response) {

        try {
            JSONObject jsonObject = new JSONObject(response);
            JSONObject weatherInfo = jsonObject.getJSONArray("HeWeather data service 3.0")
                    .getJSONObject(0);

            String cityAqi;
            String qlty;
            if (weatherInfo.has("aqi")){
                JSONObject aqi = weatherInfo.getJSONObject("aqi");
                JSONObject aqiCity = aqi.getJSONObject("city");
                cityAqi = aqiCity.getString("aqi");
                qlty = aqiCity.getString("qlty");
            }else {
                cityAqi = "服务器无数据";
                qlty = "";
            }

            JSONObject basic = weatherInfo.getJSONObject("basic");
            String cityName = basic.getString("city");
            String weatherCode = basic.getString("id");
            JSONObject update = basic.getJSONObject("update");
            String updateTime = update.getString("loc");

            JSONObject today = weatherInfo.getJSONArray("daily_forecast").getJSONObject(0);
            JSONObject cond = today.getJSONObject("cond");
            String weatherDay = cond.getString("txt_d");
            String weatherNight = cond.getString("txt_n");
            JSONObject tmp = today.getJSONObject("tmp");
            String tempmin = tmp.getString("min");
            String tempmax = tmp.getString("max");


            JSONObject now = weatherInfo.getJSONObject("now");
            JSONObject nowcond = now.getJSONObject("cond");
            String weather = nowcond.getString("txt");
            String weatherId = nowcond.getString("code");

            saveWeatherInfo(context, cityAqi, qlty, cityName, updateTime, weatherDay,
                    weatherNight, tempmin, tempmax, weather, weatherId,weatherCode);

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private static void saveWeatherInfo(Context context, String cityAqi, String qlty, String
            cityName, String updateTime, String weatherDay, String weatherNight,
                                        String tempmin, String tempmax, String weather, String
                                                weatherId,String weatherCode) {
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(context)
                .edit();
        String[] array = weatherCode.split("N");
        String wCode = array[1];
        editor.putString("weatherCode",wCode);
        editor.putBoolean("city_selected", true);
        editor.putString("cityAqi", cityAqi);
        editor.putString("qlty", qlty);
        editor.putString("cityName", cityName);
        editor.putString("updateTime", updateTime);
        editor.putString("weatherDay", weatherDay);
        editor.putString("weatherNight", weatherNight);
        editor.putString("tempmin", tempmin);
        editor.putString("tempmax", tempmax);
        editor.putString("weather", weather);
        editor.putString("weatherId", weatherId);
        editor.putBoolean("dnequals",weatherDay.equals(weatherNight));
        editor.commit();
    }

}
