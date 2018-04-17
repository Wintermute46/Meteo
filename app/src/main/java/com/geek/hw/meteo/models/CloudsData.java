package com.geek.hw.meteo.models;

import com.google.gson.annotations.SerializedName;

public class CloudsData {
    public String code;
    public String text;
    @SerializedName("base_feet_agl")
    public double baseFeetAgl;
    @SerializedName("base_meters_agl")
    public double baseMetersAgl;
}
