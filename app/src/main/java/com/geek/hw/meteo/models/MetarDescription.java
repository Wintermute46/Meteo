package com.geek.hw.meteo.models;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

public class MetarDescription {
    public String icao;
    public String name;
    public String observed;
    @SerializedName("raw_text")
    public String rawText;
    public BaroData barometer;
    public List<CloudsData> clouds = new ArrayList<>();
    public DewpointData dewpoint;
    public ElevationData elevation;
    @SerializedName("flight_category")
    public String flightCategory;
    @SerializedName("humidity_percent")
    public int humidityPercent;
    @SerializedName("rain_in")
    public int rainIn;
    @SerializedName("snow_in")
    public int snowIn;
    public TemperatureData temperature;
    public VisibilityData visibility;
    public WindData wind;
}
