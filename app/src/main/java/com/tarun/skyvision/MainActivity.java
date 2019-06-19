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
import android.text.Layout;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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

    @Override
    public void onItemClick(View view, int position) {
        Toast.makeText(this, "You clicked " + adapter.getItem(position) + " on item position " + position, Toast.LENGTH_SHORT).show();
    }



    public void updateUI()
    {

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
        recyclerView.setVisibility(View.INVISIBLE);
        selectCity.show();
    }



    @SuppressLint("MissingPermission")
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
        if(cityMap==null)
            loadCityMap();
        mImage.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if(motionEvent.getAction()==MotionEvent.ACTION_DOWN && !revealed) {
                    recyclerView.setVisibility(View.VISIBLE);
                    revealed=true;
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
                        MainActivity activity = (MainActivity)context;
                        FetchTask fetchTask = new FetchTask(activity);
                        fetchTask.execute();
                    }
                });

    }
}
