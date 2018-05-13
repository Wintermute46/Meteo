package com.geek.hw.meteo;

import android.Manifest;
import android.annotation.SuppressLint;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.widget.ImageView;
import android.widget.RemoteViews;

import com.geek.hw.meteo.models.CityData;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

import android.os.Handler;
import android.widget.Toast;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Executor;

public class Widget extends AppWidgetProvider {
    private static final String LOG_TAG = Widget.class.getSimpleName();
    private final Handler handler = new Handler();
    private ImageView img;

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        super.onUpdate(context, appWidgetManager, appWidgetIds);
        for (int i : appWidgetIds) {
            updateWidget(context, appWidgetManager, i);
        }
    }

    @SuppressLint("MissingPermission")
    private void updateWidget(final Context context, AppWidgetManager appWidgetManager, int appWidgetId) {
        final RemoteViews rv = new RemoteViews(context.getPackageName(), R.layout.layout_widget);
//        rv.setImageViewResource(R.id.widget_icon, R.drawable.ic_01d);

        FusedLocationProviderClient locationClient = LocationServices.getFusedLocationProviderClient(context);
        Location location = locationClient.getLastLocation().getResult();
        final Double lat = location.getLatitude();
        final Double lon = location.getLongitude();

        List<Address> addr = null;
        Geocoder gc = new Geocoder(context, Locale.getDefault());
        try {
            addr = gc.getFromLocation(lat, lon, 1);
        } catch (IOException e) {
            e.printStackTrace();
            Log.e(LOG_TAG, e.getMessage());
        }
        final String city = addr.get(0).getLocality();

        new Thread() {
            public void run() {
                final CityData data = OwmDataLoader.getOwmData(context, lat, lon);

                if (data == null) {
                    handler.post(new Runnable() {
                        public void run() {

                        }
                    });
                } else {
                    handler.post(new Runnable() {
                        public void run() {
                            rv.setTextViewText(R.id.widget_city_name, city);

                        }
                    });
                }

            }
        }.start();
//


//        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
//            ActivityCompat.requestPermissions(this,
//                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQ_PERMISSION);
//        } else {
//            locationClient.getLastLocation().addOnSuccessListener(this, new OnSuccessListener<Location>() {
//                @Override
//                public void onSuccess(Location location) {
//                    if (location != null) {
//                        LAT = location.getLatitude();
//                        LON = location.getLongitude();
//
//                        getCityRegionName();
//
//                        handler.post(new Runnable() {
//                            public void run() {
//                                setScreen(R.id.nav_open_weather);
//                            }
//                        });
//                    }
//                }
//            });
//        }
//


        appWidgetManager.updateAppWidget(appWidgetId, rv);
    }
}
