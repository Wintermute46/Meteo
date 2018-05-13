package com.geek.hw.meteo.ui;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.geek.hw.meteo.MainActivity;
import com.geek.hw.meteo.OwmDataLoader;
import com.geek.hw.meteo.R;
import com.geek.hw.meteo.db.DbContract;
import com.geek.hw.meteo.db.DbHelper;
import com.geek.hw.meteo.models.CityData;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.location.places.ui.PlaceAutocomplete;
import com.squareup.okhttp.HttpUrl;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import java.io.IOException;
import java.io.InputStream;
import java.text.DateFormat;
import java.util.Date;
import java.util.Locale;

///////////////////////////////////////////////////////////////////////////
// Fragment with OpenStreetMap weather data
///////////////////////////////////////////////////////////////////////////

public class OWMdataFragment extends Fragment implements Animation.AnimationListener {

    private final Handler handler = new Handler();
    private DbHelper dbHelper;
    private static final String LOG_TAG = OWMdataFragment.class.getSimpleName();
    private static String city;
    private static String region;
    private static double LAT;
    private static double LON;

    private TextView cityTextView;
    private TextView regionTextView;
    private TextView updatedTextView;
    private TextView detailsTextView;
    private TextView currentTemperatureTextView;
    private TextView windLabel;
    private TextView windTextView;
    private ImageView windIcon;
    private TextView humidLabel;
    private TextView pressLabel;
    private TextView humidTextView;
    private TextView pressTextView;
    private ImageView weatherIcon;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_owmdata, container, false);

        dbHelper = new DbHelper(getContext());

        cityTextView = view.findViewById(R.id.city_field);
        regionTextView = view.findViewById(R.id.region_field);
        updatedTextView = view.findViewById(R.id.updated_field);
        detailsTextView = view.findViewById(R.id.details_field);
        currentTemperatureTextView = view.findViewById(R.id.current_temperature_field);
        windLabel = view.findViewById(R.id.text_owm_wind);
        windTextView = view.findViewById(R.id.text_owm_wind_p);
        windIcon = view.findViewById(R.id.wind_icon);
        humidLabel = view.findViewById(R.id.text_owm_humidity);
        pressLabel = view.findViewById(R.id.text_owm_press);
        humidTextView = view.findViewById(R.id.text_owm_humidity_p);
        pressTextView = view.findViewById(R.id.text_owm_press_p);
        weatherIcon = view.findViewById(R.id.weather_icon);

        windLabel.setVisibility(View.INVISIBLE);
        windIcon.setVisibility(View.INVISIBLE);
        humidLabel.setVisibility(View.INVISIBLE);
        pressLabel.setVisibility(View.INVISIBLE);


        city = getArguments().getString(MainActivity.CITY_NAME);
        region = getArguments().getString(MainActivity.REG_NAME);
        LAT = getArguments().getDouble(MainActivity.LATITUDE);
        LON = getArguments().getDouble(MainActivity.LONGITUDE);

        if (city != null)
            getDataCache(city);

        if (LAT != 0 && LON != 0)
            updateWeatherData();

        return view;
    }

///////////////////////////////////////////////////////////////////////////
// Get cache
///////////////////////////////////////////////////////////////////////////

    private void getDataCache(String city) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String selection = "city = ?";
        String[] args = {city};

        Cursor cursor = db.query(DbContract.WeatherEntry.TAB_NAME,
                null, selection, args, null, null, null);

        if(cursor.moveToFirst()) {
            cityTextView.setText(cursor.getString(cursor.getColumnIndex("city")));
//            cityTextView.setText(cursor.getString(cursor.getColumnIndex("cityCountry")));
            detailsTextView.setText(cursor.getString(cursor.getColumnIndex("description")));
            currentTemperatureTextView.setText(cursor.getString(cursor.getColumnIndex("temperature")));
            updatedTextView.setText(cursor.getString(cursor.getColumnIndex("observed")));
            humidTextView.setText(cursor.getString(cursor.getColumnIndex("humidity")));
            pressTextView.setText(cursor.getString(cursor.getColumnIndex("pressure")));

            setWeatherIcon(cursor.getString(cursor.getColumnIndex("iconId")));
        }

        cursor.close();
        db.close();
    }

///////////////////////////////////////////////////////////////////////////
// Get the weather
///////////////////////////////////////////////////////////////////////////

    private void updateWeatherData() {
        new Thread() {
            public void run() {
                final CityData data = OwmDataLoader.getOwmData(getActivity(), LAT, LON);
//                final CityData data = OwmDataLoader.getOwmData(getActivity(), city);

                if (data == null) {
                    handler.post(new Runnable() {
                        public void run() {
                            Toast.makeText(getActivity(), getString(R.string.place_not_found),
                                    Toast.LENGTH_LONG).show();
                        }
                    });
                } else {
                    handler.post(new Runnable() {
                        public void run() {
                            renderWeather(data);
                        }
                    });
                }

            }
        }.start();
    }

///////////////////////////////////////////////////////////////////////////
// Display the weather data
///////////////////////////////////////////////////////////////////////////

    private void renderWeather(CityData data) {

        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues cv = new ContentValues();

        try {

            String name = data.name.toUpperCase(Locale.US) + ", " + data.sys.country;
            cv.put(DbContract.WeatherEntry.COL_CITY, city);
//            cv.put(DbContract.WeatherEntry.COL_CITY, data.name);
            cv.put(DbContract.WeatherEntry.COL_CITY_C, name);
            name = city;
            cityTextView.setText(name);

            regionTextView.setText(region);

            String description = "";

            if(data.weather.size() != 0){
                description = data.weather.get(0).description.toUpperCase(Locale.US);
            }

            cv.put(DbContract.WeatherEntry.COL_DESCR, description);
            detailsTextView.setText(description);

            String wind = String.format("%.0f", data.wind.speed) + " м/с";

            windLabel.setVisibility(View.VISIBLE);
            windTextView.setText(wind);

            float rot = data.wind.deg + 180.0f;

            windIcon.setVisibility(View.VISIBLE);
            windIcon.setRotation(rot);

            String hum = data.main.humidity + "%";
            cv.put(DbContract.WeatherEntry.COL_HUMID, hum);
            humidLabel.setVisibility(View.VISIBLE);
            humidTextView.setText(hum);

            String press = data.main.pressure + " hPa";
            cv.put(DbContract.WeatherEntry.COL_PRESS, press);
            pressLabel.setVisibility(View.VISIBLE);
            pressTextView.setText(press);

            String currT = String.format("%.0f", data.main.tempBig) + " ℃";
            cv.put(DbContract.WeatherEntry.COL_TEMP, currT);
            currentTemperatureTextView.setText(currT);

            DateFormat df = DateFormat.getDateTimeInstance();
            String updatedOn = df.format(new Date(data.dt * 1000));
            String upd = "Last update: " + updatedOn;
            cv.put(DbContract.WeatherEntry.COL_UPD, upd);
            updatedTextView.setText(upd);

            setWeatherIcon(data.weather.get(0).icon);
            cv.put(DbContract.WeatherEntry.COL_ICON, data.weather.get(0).icon);

            long newId = db.insertWithOnConflict(DbContract.WeatherEntry.TAB_NAME, null, cv,
                                                    SQLiteDatabase.CONFLICT_REPLACE);

            Log.d(LOG_TAG, getString(R.string.db_entry_add) + newId);

        } catch (Exception e) {
            Log.d(LOG_TAG, getString(R.string.json_error));
            Toast.makeText(getActivity(), getString(R.string.json_error), Toast.LENGTH_LONG).show();
        } finally {
            db.close();
        }
    }

///////////////////////////////////////////////////////////////////////////
// Download the weather icon
///////////////////////////////////////////////////////////////////////////

    private void setWeatherIcon(String icon){

        final Animation anim = AnimationUtils.loadAnimation(getActivity(), R.anim.alpha);
        anim.setAnimationListener(this);

        String iconName = "ic_" + icon;

        weatherIcon.setImageResource(getResources()
                .getIdentifier(iconName, "drawable", "com.geek.hw.meteo"));
        weatherIcon.startAnimation(anim);
    }

    @Override
    public void onAnimationStart(Animation animation) {
        weatherIcon.setVisibility(View.VISIBLE);
    }

    @Override
    public void onAnimationEnd(Animation animation) {
        weatherIcon.setVisibility(View.VISIBLE);
    }

    @Override
    public void onAnimationRepeat(Animation animation) {
        weatherIcon.setVisibility(View.VISIBLE);
    }
}
