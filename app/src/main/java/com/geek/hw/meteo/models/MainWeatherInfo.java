package com.geek.hw.meteo.models;

import com.google.gson.annotations.SerializedName;

public class MainWeatherInfo {

    @SerializedName("temp")
    public Double tempBig;
    public double pressure;
    public long humidity;
    public double temp_min;
    public double temp_max;

}
