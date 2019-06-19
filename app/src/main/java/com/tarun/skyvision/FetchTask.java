package com.tarun.skyvision;

import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.Objects;

import static com.tarun.skyvision.MainActivity.API_KEY;
import static com.tarun.skyvision.MainActivity.cityID;
import static com.tarun.skyvision.MainActivity.latitude;
import static com.tarun.skyvision.MainActivity.longitude;

abstract class FetchTaskCallback
{
    abstract void updateUI();
}

class FetchTask extends AsyncTask<Void, Void, String> {
    FetchTaskCallback callback;
    int service_code;
    Weather weather;

    FetchTask(FetchTaskCallback callback,int service_code,Weather weather)
    {
        this.callback=callback;
        this.service_code=service_code;
        this.weather=weather;

    }


    @Override
    protected String doInBackground(Void... voids) {
        String URL_BASE = "http://api.openweathermap.org";
        String END_POINT = "/data";
        String VERSION = "/2.5";
        String CURRENT_WEATHER_PARAM = "/weather?";
        String FORECAST_PARAM="/forecast?";
        String CITY_PARAM = "id=";
        String LAT_PARAM = "lat=";
        String LON_PARAM = "&lon=";
        String API_KEY_PARAM = "&APPID=";
        String URLString="";

        if(service_code==R.integer.CURRENT_WEATHER) {
            if (cityID == 0L)
                URLString = URL_BASE + END_POINT + VERSION + CURRENT_WEATHER_PARAM + LAT_PARAM + latitude + LON_PARAM + longitude + API_KEY_PARAM + API_KEY;

            else
                URLString = URL_BASE + END_POINT + VERSION + CURRENT_WEATHER_PARAM + CITY_PARAM + cityID + API_KEY_PARAM + API_KEY;

        }

        else if(service_code==R.integer.FORECAST)
        {
            if (cityID == 0L)
                URLString = URL_BASE + END_POINT + VERSION +FORECAST_PARAM + LAT_PARAM + latitude + LON_PARAM + longitude + API_KEY_PARAM + API_KEY;

            else
                URLString = URL_BASE + END_POINT + VERSION + FORECAST_PARAM + CITY_PARAM + cityID + API_KEY_PARAM + API_KEY;
        }

        try
        {
            StringBuilder ans = new StringBuilder();
            URL url = new URL(URLString);
            URLConnection urlCon = url.openConnection();
            BufferedReader br = new BufferedReader(new InputStreamReader(urlCon.getInputStream()));
            String line;
            while ((line = br.readLine()) != null)
                ans.append(line);
            return ans.toString();
        }
        catch (Exception e)
        {
            Log.e("SK_ERROR",Objects.requireNonNull(e.toString()));
        }
        return null;
    }


    @Override
    protected void onPostExecute(String response) {
        super.onPostExecute(response);
        try {
            weather.setWeatherData(new JSONObject(response));
            callback.updateUI();
        } catch (Exception e) {
            Log.e("SK_INFO", Objects.requireNonNull(e.getMessage()));
        }

    }
}