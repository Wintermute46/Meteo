package com.geek.hw.meteo.models;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class MetarData implements Serializable {
    public int results;
    public List<MetarDescription> data = new ArrayList<>();
}
