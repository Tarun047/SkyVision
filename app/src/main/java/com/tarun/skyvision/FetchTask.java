package com.tarun.skyvision;
import android.os.AsyncTask;
import android.util.Log;

import com.tarun.skyvision.MainActivity;
import com.tarun.skyvision.R;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.lang.ref.WeakReference;
import java.net.URL;
import java.net.URLConnection;
import java.util.Locale;
import java.util.Objects;

import static com.tarun.skyvision.MainActivity.API_KEY;
import static com.tarun.skyvision.MainActivity.cityID;
import static com.tarun.skyvision.MainActivity.latitude;
import static com.tarun.skyvision.MainActivity.longitude;

class FetchTask extends AsyncTask<Void, Void, String> {




    static String URL_BASE = "http://api.openweathermap.org";
    static String END_POINT = "/data";
    static String VERSION = "/2.5";
    static String SERVICE = "/weather?";
    static String FORECAST_PARAM="/forecast?";
    static String CITY_PARAM = "id=";
    static String LAT_PARAM = "lat=";
    static String LON_PARAM = "&lon=";
    static String API_KEY_PARAM = "&APPID=";
    final String TAG = "SK_APP";
    private WeakReference<MainActivity> activityReference;

    FetchTask(MainActivity context) {

        activityReference = new WeakReference<>(context);
    }

    @Override
    protected String doInBackground(Void... voids) {
        if(cityID==0)
        {
            try {
                StringBuilder ans = new StringBuilder();
                URL url = new URL(URL_BASE + END_POINT + VERSION + SERVICE + LAT_PARAM + latitude + LON_PARAM +longitude + API_KEY_PARAM + API_KEY);
                URLConnection urlCon = url.openConnection();
                BufferedReader br = new BufferedReader(new InputStreamReader(urlCon.getInputStream()));
                String line;
                while ((line = br.readLine()) != null)
                    ans.append(line);
                return ans.toString();
            } catch (Exception e) {
                Log.e("SK_ERROR", Objects.requireNonNull(e.getMessage()));
            }
        }
        else
        {
            try {
                StringBuilder ans = new StringBuilder();
                URL url = new URL(URL_BASE + END_POINT + VERSION + SERVICE + CITY_PARAM + cityID + API_KEY_PARAM + API_KEY);
                URLConnection urlCon = url.openConnection();
                BufferedReader br = new BufferedReader(new InputStreamReader(urlCon.getInputStream()));
                String line;
                while ((line = br.readLine()) != null)
                    ans.append(line);
                return ans.toString();
            }catch (Exception e)
            {
                Log.e("SK_ERROR",Objects.requireNonNull(e.toString()));
            }
        }
        return null;
    }


    @Override
    protected void onPostExecute(String response) {
        super.onPostExecute(response);
        try {


            MainActivity activity = activityReference.get();
            if (activity == null || activity.isFinishing()) {return;}

            JSONObject json = new JSONObject(response);
            activity.cityTV.setText(json.getString("name"));
            JSONObject main = (JSONObject) json.get("main");
            activity.tempTV.setText(String.format(Locale.US, "%.2f C", main.getDouble("temp") - 273));
            JSONObject weather = ((JSONArray)json.get("weather")).getJSONObject(0);
            Log.d("WCODE: ",weather.toString()+" "+(getImage(weather.getInt("id"))== R.drawable.haze));
            activity.mImage.setBackgroundResource(getImage(weather.getInt("id")));
        } catch (Exception e) {
            Log.e("SK_INFO", Objects.requireNonNull(e.getMessage()));
        }

    }


    private int getImage(int code)
    {
        if(code>=200 && code<300)
            return R.drawable.thunderstormwithrain;
        else if(code>=300 && code<500)
            return R.drawable.raindrizzle;
        else if(code>=500 && code<600)
            return R.drawable.rain;
        else if(code >=600 && code<700)
            return R.drawable.snowy;
        else if(code>=700 && code<800)
            return  R.drawable.haze;
        else
            return R.drawable.sunnyday;
    }
}