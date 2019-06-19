package com.tarun.skyvision;

import org.json.JSONObject;

public class Weather {
    JSONObject weatherData;


    public void setWeatherData(JSONObject weatherData) {
        this.weatherData = weatherData;
    }

    public JSONObject getWeatherData() {
        return weatherData;
    }

}
