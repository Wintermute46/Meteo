package com.geek.hw.meteo.models;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class CityData implements Serializable {

    public CoordinatesModel coord;
    public List<WeatherModel> weather = new ArrayList<WeatherModel>();
    public String base;
    public Long id;
    public String name;
    public int cod;
    public MainWeatherInfo main;
    public System sys;
    public long dt;

}
