package com.tarun.skyvision;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import org.json.JSONArray;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.lang.ref.WeakReference;
import java.net.URL;
import java.net.URLConnection;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.TreeMap;

public class MainActivity extends AppCompatActivity {
    static String URL_BASE = "http://api.openweathermap.org";
    static String END_POINT = "/data";
    static String VERSION = "/2.5";
    static String SERVICE = "/weather?";
    static String CITY_PARAM = "id=";
    static String LAT_PARAM = "lat=";
    static String LON_PARAM = "&lon=";
    static String API_KEY_PARAM = "&APPID=";
    final String TAG = "SK_APP";
    static String API_KEY;
    static long cityID=0;
    static double latitude;
    static double longitude;
    TextView cityTV;
    TextView tempTV;
    TextView sunrTV;
    TextView sunsTV;
    ImageView mImage;
    TreeMap<String,Long> cityMap;
    String[] items;
    List<String> predictions = Arrays.asList(new String[]{"Haze","wind","Rain","Sun","Haze","wind","Rain","Sun","Haze","wind","Rain","Sun","Haze","wind","Rain","Sun"});

    private boolean hasPermissions(Context context, String... permissions) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && context != null && permissions != null) {
            for (String permission : permissions) {
                if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;
    }



    private static class FetchTask extends AsyncTask<Void, Void, String> {

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
                JSONObject json = new JSONObject(response);
                MainActivity activity = activityReference.get();
                if (activity == null || activity.isFinishing()) return;
                activity.cityTV.setText(json.getString("name"));
                JSONObject main = (JSONObject) json.get("main");
                activity.tempTV.setText(String.format(Locale.US, "%.2f C", main.getDouble("temp") - 273));
                JSONObject weather = ((JSONArray)json.get("weather")).getJSONObject(0);
                Log.d("WCODE: ",weather.toString()+" "+(getImage(weather.getInt("id"))==R.drawable.haze));
                activity.mImage.setImageResource(getImage(weather.getInt("id")));
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

    public void getPermissions() {
        if (Build.VERSION.SDK_INT >= 23) {
            String[] PERMISSIONS = {Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.ACCESS_NETWORK_STATE};
            if (!hasPermissions(this, PERMISSIONS)) {
                ActivityCompat.requestPermissions(this, PERMISSIONS, 1);
            }
        }
    }
    public void loadCityMap()
    {

        try {
            ObjectInputStream in = new ObjectInputStream(getResources().openRawResource(R.raw.citymap));
            cityMap = (TreeMap<String, Long>) in.readObject();
            items = cityMap.keySet().toArray(new String[cityMap.keySet().size()]);
            in.close();
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }
    }
    public void startSearch(View view)
    {
        SelectCity selectCity = new SelectCity(this,items) {
            @Override
            public void onClick(View view) {
                TextView selected = (TextView)view;
                cityID=cityMap.get(selected.getText());
                //Toast.makeText(this.contextReference,cityID+"",Toast.LENGTH_LONG).show();
                this.dismiss();
                FetchTask fetchTask = new FetchTask((MainActivity)contextReference);
                fetchTask.execute();
            }
        };
        selectCity.show();
    }



    @SuppressLint("MissingPermission")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        API_KEY = getString(R.string.api_key);
        mImage = findViewById(R.id.weather_iv);
        cityTV = findViewById(R.id.city_tv);
        tempTV = findViewById(R.id.temp_tv);
        getPermissions();
        if(cityMap==null)
            loadCityMap();


    }


    @Override
    protected void onStart() {
        super.onStart();
        SingleShotLocationProvider.requestSingleUpdate(this,
                new SingleShotLocationProvider.LocationCallback() {
                    @Override
                    public void onNewLocationAvailable(SingleShotLocationProvider.GPSCoordinates location,Context context) {
                        Log.d("Location", "my location is " + location.latitude + " " + location.longitude);
                        latitude=location.latitude;
                        longitude=location.longitude;
                        MainActivity activity = (MainActivity)context;
                        FetchTask fetchTask = new FetchTask(activity);
                        fetchTask.execute();
                    }
                });

    }
}
