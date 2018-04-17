package com.geek.hw.meteo.models;

import com.google.gson.annotations.SerializedName;

public class WindData {
    public int degrees;
    @SerializedName("speed_kts")
    public int speedKts;
    @SerializedName("speed_mph")
    public int speedMph;
    @SerializedName("speed_mps")
    public int speedMps;
    @SerializedName("gust_kts")
    public int gustKts;
    @SerializedName("gust_mph")
    public int gustMph;
    @SerializedName("gust_mps")
    public int gustMps;
}
