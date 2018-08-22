package com.geek.hw.meteo.ui;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import com.geek.hw.meteo.MainActivity;
import com.geek.hw.meteo.MetarDataLoader;
import com.geek.hw.meteo.R;
import com.geek.hw.meteo.db.DbContract;
import com.geek.hw.meteo.db.DbHelper;
import com.geek.hw.meteo.models.MetarData;

///////////////////////////////////////////////////////////////////////////
// Fragment with METAR meteo data closest to a parameter latitude/longitude
//from OpenWeatherMap data
///////////////////////////////////////////////////////////////////////////

public class METARdataFragment extends Fragment {

    private static double LAT;
    private static double LON;

    private final Handler handler = new Handler();
    private DbHelper dbHelper;
    private static final String LOG_TAG = METARdataFragment.class.getSimpleName();

    private TextView icao;
    private TextView name;
    private TextView metarRaw;
    private TextView observed;
    private TextView currTemp;
    private TextView dewpoint;
    private TextView humidity;
    private TextView flightRules;
    private TextView wind;
    private TextView visibility;
    private TextView clouds;
    private TextView altimeter;
    private TextView pressure;
    private TextView elevation;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_metardata, container, false);

        dbHelper = new DbHelper(getContext());

        LAT = getArguments().getDouble(MainActivity.LATITUDE);
        LON = getArguments().getDouble(MainActivity.LONGITUDE);

        icao = view.findViewById(R.id.icao);
        name = view.findViewById(R.id.name);
        metarRaw = view.findViewById(R.id.metar_raw);
        observed = view.findViewById(R.id.observed);
        currTemp = view.findViewById(R.id.text_current_temp);
        dewpoint = view.findViewById(R.id.text_dewpoint_temp);
        humidity = view.findViewById(R.id.text_humidity_p);
        flightRules = view.findViewById(R.id.text_fr_p);
        wind = view.findViewById(R.id.text_wind_p);
        visibility = view.findViewById(R.id.text_visibility_p);
        clouds = view.findViewById(R.id.text_clouds_p);
        altimeter = view.findViewById(R.id.text_alt_p);
        pressure = view.findViewById(R.id.text_press_p);
        elevation = view.findViewById(R.id.text_elev_p);

       getDataCache(getArguments().getString(MainActivity.CITY_NAME));

        if (LAT != 0 && LON != 0)
            updateMetarData(LAT, LON);
        else
            Log.i(LOG_TAG, "No METAR coordinates");

        return view;
    }

///////////////////////////////////////////////////////////////////////////
// Get cache
///////////////////////////////////////////////////////////////////////////

    private void getDataCache(String city) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String selection = "city = ?";
        String[] args = {city};

        Cursor cursor = db.query(DbContract.MetarEntry.TAB_NAME,
                                    null, selection, args, null, null, null);

        if(cursor.moveToFirst()) {
            icao.setText(cursor.getString(cursor.getColumnIndex(DbContract.MetarEntry.COL_ICAO)));
            name.setText(cursor.getString(cursor.getColumnIndex(DbContract.MetarEntry.COL_NAME)));
            metarRaw.setText(cursor.getString(cursor.getColumnIndex(DbContract.MetarEntry.COL_RAW)));
            observed.setText(cursor.getString(cursor.getColumnIndex(DbContract.MetarEntry.COL_UPD)));
            currTemp.setText(cursor.getString(cursor.getColumnIndex(DbContract.MetarEntry.COL_TEMP)));
            dewpoint.setText(cursor.getString(cursor.getColumnIndex(DbContract.MetarEntry.COL_DEW)));
            humidity.setText(cursor.getString(cursor.getColumnIndex(DbContract.MetarEntry.COL_HUMID)));
            clouds.setText(cursor.getString(cursor.getColumnIndex(DbContract.MetarEntry.COL_CLOUD)));
            wind.setText(cursor.getString(cursor.getColumnIndex(DbContract.MetarEntry.COL_WIND)));
            visibility.setText(cursor.getString(cursor.getColumnIndex(DbContract.MetarEntry.COL_VISIB)));
            flightRules.setText(cursor.getString(cursor.getColumnIndex(DbContract.MetarEntry.COL_FR)));
            altimeter.setText(cursor.getString(cursor.getColumnIndex(DbContract.MetarEntry.COL_ALT)));
            pressure.setText(cursor.getString(cursor.getColumnIndex(DbContract.MetarEntry.COL_PRESS)));
            elevation.setText(cursor.getString(cursor.getColumnIndex(DbContract.MetarEntry.COL_ELEV)));
        }

        cursor.close();
        db.close();

    }

///////////////////////////////////////////////////////////////////////////
// Get the weather
///////////////////////////////////////////////////////////////////////////

    private void updateMetarData(final double lon, final double lat) {
        new Thread(){
            public void run() {
                final MetarData data = MetarDataLoader.getMetarData(getActivity(), lon, lat);

                if (data == null) {
                    handler.post(new Runnable() {
                        public void run() {
                            Toast.makeText(getActivity(), getString(R.string.metar_not_found),
                                    Toast.LENGTH_LONG).show();
                        }
                    });
                } else {
                    handler.post(new Runnable() {
                        public void run() {
                            renderMetar(data);
                        }
                    });
                }
            }
        }.start();
    }

///////////////////////////////////////////////////////////////////////////
// Display the weather data
///////////////////////////////////////////////////////////////////////////

    private void renderMetar(MetarData metar) {

        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues cv = new ContentValues();

        try {
            cv.put(DbContract.MetarEntry.COL_ICAO, metar.data.get(0).icao);
            icao.setText(metar.data.get(0).icao);

            String icaoName = metar.data.get(0).name + " " + getString(R.string.airport) + " " + getArguments().getString(MainActivity.CITY_NAME);
            cv.put(DbContract.MetarEntry.COL_CITY, getArguments().getString(MainActivity.CITY_NAME));
            cv.put(DbContract.MetarEntry.COL_NAME, icaoName);
            name.setText(icaoName);

            cv.put(DbContract.MetarEntry.COL_RAW, metar.data.get(0).rawText);
            metarRaw.setText(metar.data.get(0).rawText);

            String observ = metar.data.get(0).observed + " " + getString(R.string.utc);
            cv.put(DbContract.MetarEntry.COL_UPD, observ);
            observed.setText(observ);

            String currT = metar.data.get(0).temperature.celsius + " " + getString(R.string.celsius) +
                    " / " + metar.data.get(0).temperature.fahrenheit + " " + getString(R.string.fahrenheit);
            cv.put(DbContract.MetarEntry.COL_TEMP, currT);
            currTemp.setText(currT);

            String dew = metar.data.get(0).dewpoint.celsius + " " + getString(R.string.celsius) +
                    " / " + metar.data.get(0).dewpoint.fahrenheit + " " + getString(R.string.fahrenheit);
            cv.put(DbContract.MetarEntry.COL_DEW, dew);
            dewpoint.setText(dew);

            String humidit = metar.data.get(0).humidityPercent + getString(R.string.percent);
            cv.put(DbContract.MetarEntry.COL_HUMID, humidit);
            humidity.setText(humidit);

            String cloud = metar.data.get(0).clouds.get(0).text;
            cv.put(DbContract.MetarEntry.COL_CLOUD, cloud);
            clouds.setText(cloud);

            String winds = metar.data.get(0).wind.degrees + getString(R.string.deg) + " " +
                    metar.data.get(0).wind.speedKts + " " + getString(R.string.speed_knots) +
                    " / " + knotsToMeters(metar.data.get(0).wind.speedKts) + " " + getString(R.string.speed_meters);
            cv.put(DbContract.MetarEntry.COL_WIND, winds);
            wind.setText(winds);

            String visible = metar.data.get(0).visibility.miles + " / " + metar.data.get(0).visibility.meters;
            cv.put(DbContract.MetarEntry.COL_VISIB, visible);
            visibility.setText(visible);

            cv.put(DbContract.MetarEntry.COL_FR, metar.data.get(0).flightCategory);
            flightRules.setText(metar.data.get(0).flightCategory);

            String alt = metar.data.get(0).barometer.hg + " " + getString(R.string.mercury);
            cv.put(DbContract.MetarEntry.COL_ALT, alt);
            altimeter.setText(alt);

            String pres = metar.data.get(0).barometer.mb + " " + getString(R.string.pressure);
            cv.put(DbContract.MetarEntry.COL_PRESS, pres);
            pressure.setText(pres);

            String elev = metar.data.get(0).elevation.feet + " " + getString(R.string.foot) +
                    " / " + metar.data.get(0).elevation.meters + getString(R.string.meter) + " " + getString(R.string.msl);
            cv.put(DbContract.MetarEntry.COL_ELEV, elev);
            elevation.setText(elev);

            long newId = db.insertWithOnConflict(DbContract.MetarEntry.TAB_NAME, null, cv,
                                                    SQLiteDatabase.CONFLICT_REPLACE);
            Log.d(LOG_TAG, getString(R.string.db_entry_add) + newId);

        } catch (Exception e) {
            Log.d(LOG_TAG, getString(R.string.json_error));
            Toast.makeText(getActivity(), getString(R.string.json_error), Toast.LENGTH_LONG).show();
        } finally {
            db.close();
        }

    }

    private int knotsToMeters (int knots) {
        return (int)(knots * 0.514444);
    }


}
