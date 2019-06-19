package com.tarun.skyvision;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.ObjectInputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.TreeMap;

public class MainActivity extends AppCompatActivity implements WeatherRecycler.ItemClickListener
{

    static String API_KEY;
    static long cityID=0;
    static double latitude;
    static double longitude;
    static boolean revealed=false;
    TextView cityTV;
    TextView tempTV;
    TextView sunrTV;
    TextView sunsTV;
    RelativeLayout mImage;
    RecyclerView recyclerView;
    TreeMap<String,Long> cityMap;
    Weather currentWeather;
    Weather foreCast;

    FetchTaskCallback callback = new FetchTaskCallback() {
        @Override
        void updateUI() {
            try {
                JSONObject json = currentWeather.getWeatherData();
                cityTV.setText(json.getString("name"));
                JSONObject main = (JSONObject) json.get("main");
                tempTV.setText(String.format(Locale.US, "%.2f C", main.getDouble("temp") - 273));
                JSONObject weather = ((JSONArray) json.get("weather")).getJSONObject(0);
                Log.d("WCODE: ", weather.toString() + " " + (getImage(weather.getInt("id")) == R.drawable.haze));
                mImage.setBackgroundResource(getImage(weather.getInt("id")));
            }
            catch (Exception e)
            {
                Log.d("SK_ERROR",e.getMessage());
            }
        }
    };


    String[] items;
    static List<String> predictions = Arrays.asList(new String[]{"Haze","wind","Rain","Sun","Haze","wind","Rain","Sun","Haze","wind","Rain","Sun","Haze","wind","Rain","Sun"});
    static List<Integer> dummy = Arrays.asList(new Integer[]{1,2,3,4,5,6,7});
    private WeatherRecycler adapter;


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

    @Override
    public void onItemClick(View view, int position) {
        Toast.makeText(this, "You clicked " + adapter.getItem(position) + " on item position " + position, Toast.LENGTH_SHORT).show();
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
                FetchTask fetchTask = new FetchTask(callback,R.integer.CURRENT_WEATHER,currentWeather);
                fetchTask.execute();
            }
        };
        recyclerView.setVisibility(View.INVISIBLE);
        selectCity.show();
    }




    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        API_KEY = getString(R.string.api_key);
        mImage = findViewById(R.id.root_layout);
        cityTV = findViewById(R.id.city_tv);
        tempTV = findViewById(R.id.temp_tv);
        getPermissions();
        recyclerView = findViewById(R.id.forecast_rv);
        LinearLayoutManager horizontalLayoutManager = new LinearLayoutManager(MainActivity.this, LinearLayoutManager.HORIZONTAL, false);
        recyclerView.setLayoutManager(horizontalLayoutManager);
        adapter = new WeatherRecycler(this, dummy, predictions);
        adapter.setClickListener(this);
        recyclerView.setAdapter(adapter);
        currentWeather = new Weather();
        if(cityMap==null)
            loadCityMap();
        mImage.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if(motionEvent.getAction()==MotionEvent.ACTION_DOWN && !revealed) {
                    recyclerView.setVisibility(View.VISIBLE);
                    revealed=true;
                    FetchTask fetchTask = new FetchTask(callback,0,foreCast);
                    fetchTask.execute();
                }
                else if(motionEvent.getAction()==MotionEvent.ACTION_DOWN && revealed) {
                    recyclerView.setVisibility(View.INVISIBLE);
                    revealed=false;
                }
                return revealed;
            }
        });



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
                        FetchTask fetchTask = new FetchTask(callback,R.integer.CURRENT_WEATHER,currentWeather);
                        fetchTask.execute();
                    }
                });

    }
}
