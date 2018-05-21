package com.geek.hw.meteo;

import android.Manifest;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;
import com.geek.hw.meteo.models.CityData;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import java.util.List;
import java.util.Locale;

import static android.graphics.Bitmap.createBitmap;

public class Widget extends AppWidgetProvider {
    private static final String LOG_TAG = Widget.class.getSimpleName();

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        super.onUpdate(context, appWidgetManager, appWidgetIds);
        for (int i : appWidgetIds) {
            updateWidget(context, appWidgetManager, i);
        }
    }

    private void updateWidget(final Context context, final AppWidgetManager appWidgetManager, final int appWidgetId) {

        FusedLocationProviderClient locationClient = LocationServices.getFusedLocationProviderClient(context);

        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            locationClient.getLastLocation().addOnSuccessListener(new OnSuccessListener<Location>() {
                @Override
                public void onSuccess(Location location) {
                    final RemoteViews rv = new RemoteViews(context.getPackageName(), R.layout.layout_widget);
                    final double lat = location.getLatitude();
                    final double lon = location.getLongitude();

                    Geocoder gc = new Geocoder(context, Locale.getDefault());
                    try {
                        List<Address> addr = gc.getFromLocation(lat, lon, 1);
                        String city = addr.get(0).getLocality();

                        rv.setTextViewText(R.id.widget_city_name, city);

                        new Thread() {
                            public void run() {
                                final CityData data = OwmDataLoader.getOwmData(context, lat, lon);

                                if (data != null) {
                                    rv.setTextViewText(R.id.widget_currtemp, String.format("%.0f", data.main.tempBig) + " ℃");
                                    rv.setTextViewText(R.id.widget_wind, String.format("%.0f", data.wind.speed) + " m/s");

                                    rv.setImageViewResource(R.id.widget_icon,
                                            context.getResources()
                                                    .getIdentifier("ic_" + data.weather.get(0).icon
                                                            , "drawable", "com.geek.hw.meteo"));

                                    rv.setImageViewBitmap(R.id.widget_wind_icon, rotatedArrow(context, data.wind.deg));

                                    appWidgetManager.updateAppWidget(appWidgetId, rv);
                                }
                            }
                        }.start();

                    } catch (Exception e) {
                        e.printStackTrace();
                        Log.e(LOG_TAG, e.getMessage());
                    }
                }
            });
        } else {
            RemoteViews rv = new RemoteViews(context.getPackageName(), R.layout.layout_widget);
            rv.setViewVisibility(R.id.widget_icon, View.INVISIBLE);
            rv.setViewVisibility(R.id.widget_icon, View.INVISIBLE);
            rv.setViewVisibility(R.id.widget_icon, View.INVISIBLE);
            rv.setTextViewText(R.id.widget_city_name, context.getResources().getString(R.string.text_no_data));
            appWidgetManager.updateAppWidget(appWidgetId, rv);
        }
    }

    private Bitmap rotatedArrow(Context context, float degrees) {
        Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_wind_dir);
        Matrix matrix = new Matrix();
        matrix.preRotate(degrees + 180f);
        return createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
    }
}
