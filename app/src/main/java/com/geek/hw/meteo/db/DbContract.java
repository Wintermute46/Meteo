package com.geek.hw.meteo.db;

import android.provider.BaseColumns;

public class DbContract {

    private static final String SEP = ", ";
    private static final String TYPE_PRIMARY = " INTEGER PRIMARY KEY";
    private static final String TYPE_TEXT = " TEXT";
    private static final String TYPE_DOUBLE = " DOUBLE";

    public static final String SQL_CREATE_WEATHER =
            "CREATE TABLE " + WeatherEntry.TAB_NAME + " (" +
                    WeatherEntry.COL_ID + TYPE_PRIMARY + SEP +
                    WeatherEntry.COL_CITY + TYPE_TEXT + " UNIQUE" + SEP +
                    WeatherEntry.COL_UPD + TYPE_TEXT + SEP +
                    WeatherEntry.COL_ICON + TYPE_TEXT + SEP +
                    WeatherEntry.COL_DESCR + TYPE_TEXT + SEP +
                    WeatherEntry.COL_WIND + TYPE_TEXT + SEP +
                    WeatherEntry.COL_DEG + TYPE_DOUBLE + SEP +
                    WeatherEntry.COL_HUMID + TYPE_TEXT + SEP +
                    WeatherEntry.COL_PRESS + TYPE_TEXT + SEP +
                    WeatherEntry.COL_TEMP + TYPE_TEXT + ")";

    public static final String SQL_DELETE_WEATHER =
            "DROP TABLE IF EXISTS " + WeatherEntry.TAB_NAME;

    public static final String SQL_CREATE_METAR =
            "CREATE TABLE " + MetarEntry.TAB_NAME + " (" +
                    MetarEntry.COL_ID + TYPE_PRIMARY + SEP +
                    MetarEntry.COL_ICAO + TYPE_TEXT + " UNIQUE" + SEP +
                    MetarEntry.COL_UPD + TYPE_TEXT + SEP +
                    MetarEntry.COL_NAME + TYPE_TEXT + SEP +
                    MetarEntry.COL_CITY + TYPE_TEXT + SEP +
                    MetarEntry.COL_RAW + TYPE_TEXT + SEP +
                    MetarEntry.COL_TEMP + TYPE_TEXT + SEP +
                    MetarEntry.COL_DEW + TYPE_TEXT + SEP +
                    MetarEntry.COL_HUMID + TYPE_TEXT + SEP +
                    MetarEntry.COL_WIND + TYPE_TEXT + SEP +
                    MetarEntry.COL_CLOUD + TYPE_TEXT + SEP +
                    MetarEntry.COL_VISIB + TYPE_TEXT + SEP +
                    MetarEntry.COL_FR + TYPE_TEXT + SEP +
                    MetarEntry.COL_ALT + TYPE_TEXT + SEP +
                    MetarEntry.COL_PRESS + TYPE_TEXT + SEP +
                    MetarEntry.COL_ELEV + TYPE_TEXT + ")";

    public static final String SQL_DELETE_METAR =
            "DROP TABLE IF EXISTS " + MetarEntry.TAB_NAME;

    public DbContract(){}

    public static abstract class WeatherEntry implements BaseColumns {

        public static final String TAB_NAME = "weather";
        public static final String COL_ID = "id";
        public static final String COL_CITY = "city";
        public static final String COL_UPD = "observed";
        public static final String COL_ICON = "iconId";
        public static final String COL_DESCR = "description";
        public static final String COL_WIND = "wind";
        public static final String COL_DEG = "degree";
        public static final String COL_HUMID = "humidity";
        public static final String COL_PRESS = "pressure";
        public static final String COL_TEMP = "temperature";

    }

    public static abstract class MetarEntry implements BaseColumns {

        public static final String TAB_NAME = "metar";
        public static final String COL_ID = "id";
        public static final String COL_ICAO = "icao";
        public static final String COL_UPD = "observed";
        public static final String COL_NAME = "name";
        public static final String COL_CITY = "city";
        public static final String COL_RAW = "raw";
        public static final String COL_TEMP = "temperature";
        public static final String COL_DEW = "dewpoint";
        public static final String COL_HUMID = "humidity";
        public static final String COL_WIND = "wind";
        public static final String COL_CLOUD = "clouds";
        public static final String COL_VISIB = "visibility";
        public static final String COL_FR = "flightrules";
        public static final String COL_ALT = "altimeter";
        public static final String COL_PRESS = "pressure";
        public static final String COL_ELEV = "elevation";

    }


}
