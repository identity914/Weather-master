package com.yuleshchenko.weather.util;

import android.os.AsyncTask;
import android.util.Log;

import com.yuleshchenko.weather.activities.ActivityMain;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import io.realm.Realm;
import io.realm.RealmResults;

public class DataLoader extends AsyncTask<String, Void, Void> {

    // Get JSON from openweathermap.org
    private JSONArray getJSONFromOpenweathermap(String city) throws JSONException {

        final String OWM_BASE = "http://api.openweathermap.org/data/2.5/forecast";
        final String OWM_MODE = "json";
        final String OWM_UNIT = "metric";
        final String OWM_ID = "217c6b5d9651d1cea2412cf6f84002d7";

        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(OWM_BASE)
                .append("?q=").append(city)          // the city
                .append("&mode=").append(OWM_MODE)   // json or xml
                .append("&units=").append(OWM_UNIT)  // metrc or imperial
                .append("&appid=").append(OWM_ID);   // API key

        String OWM_URL = stringBuilder.toString();

        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;
        String forecastJsonStr;
        try {
            // Take the URL and get the data:
            URL url = new URL(OWM_URL);
            urlConnection = (HttpURLConnection) url.openConnection();  // Open the connection (should be closed late)
            urlConnection.setReadTimeout(10*1000);     // 10 seconds
            urlConnection.setConnectTimeout(20*1000);  // 20 seconds
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();
            // Read the input stream into a String
            InputStream inputStream = urlConnection.getInputStream();
            StringBuilder buffer = new StringBuilder();
            reader = new BufferedReader(new InputStreamReader(inputStream));
            String line;
            while ((line = reader.readLine()) != null) { buffer.append(line); }
            forecastJsonStr = buffer.toString(); // JSON as String
            JSONObject forecastJson = new JSONObject(forecastJsonStr);
            return forecastJson.getJSONArray("list");
        } catch (IOException e) { e.printStackTrace(); }
        finally {
            if (urlConnection != null) { urlConnection.disconnect(); }  // close connection
            if (reader != null) {
                try { reader.close(); }  // close BufferedReader
                catch (final IOException e) { e.printStackTrace(); }
            }
        }
        return null;
    }

    // JSON -> Realm
    private void jsonToRealm(JSONArray weatherArray, Realm realm) throws JSONException {

        if (weatherArray == null) {
            Log.v(ActivityMain.MY_LOG, "Something is wrong, JSON is null!");
            return;
        }

        RealmResults<ForecastRealm> results = realm.where(ForecastRealm.class).findAll();
        realm.beginTransaction();  // begin load Realm transaction
        results.clear();  // clear db
        for (int i = 0; i < weatherArray.length(); i++) {
            JSONObject oneJSONForecast = weatherArray.getJSONObject(i);  // one forecast
            // Get info from JSON
            Long dt = oneJSONForecast.getLong("dt");
            String dtTxt = oneJSONForecast.getString("dt_txt");
            double temp = Double.parseDouble(oneJSONForecast.getJSONObject("main").get("temp").toString());
            double tempMax = Double.parseDouble(oneJSONForecast.getJSONObject("main").get("temp_max").toString());
            double tempMin = Double.parseDouble(oneJSONForecast.getJSONObject("main").get("temp_min").toString());
            String main = oneJSONForecast.getJSONArray("weather").getJSONObject(0).getString("main");
            String icon = oneJSONForecast.getJSONArray("weather").getJSONObject(0).getString("icon");
            double humidity = Double.parseDouble(oneJSONForecast.getJSONObject("main").get("humidity").toString());
            double wind = Double.parseDouble(oneJSONForecast.getJSONObject("wind").get("speed").toString());
            double deg = Double.parseDouble(oneJSONForecast.getJSONObject("wind").get("deg").toString());
            double pressure = Double.parseDouble(oneJSONForecast.getJSONObject("main").get("pressure").toString());
            // Create new Realm obj and put info into this object
            ForecastRealm forecastRealm = new ForecastRealm();
            forecastRealm.setDt(dt);
            forecastRealm.setDtTxt(dtTxt);
            forecastRealm.setTemp(temp);
            forecastRealm.setTempMax(tempMax);
            forecastRealm.setTempMin(tempMin);
            forecastRealm.setMain(main);
            forecastRealm.setIcon(icon);
            forecastRealm.setHumidity(humidity);
            forecastRealm.setWind(wind);
            forecastRealm.setDeg(deg);
            forecastRealm.setPressure(pressure);
            // Add item to db
            realm.copyToRealmOrUpdate(forecastRealm);
        }
        realm.commitTransaction();  // end load Realm transaction
    }

    @Override
    protected Void doInBackground(String... params) {
        Realm realm = Realm.getDefaultInstance();
        try {
            JSONArray weatherArray = getJSONFromOpenweathermap(params[0]);
            jsonToRealm(weatherArray, realm);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        realm.close();
        return null;
    }
}
