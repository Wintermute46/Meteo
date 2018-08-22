package com.geek.hw.meteo;

import android.content.Context;
import android.util.Log;

import com.geek.hw.meteo.models.CityData;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.squareup.okhttp.HttpUrl;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

///////////////////////////////////////////////////////////////////////////
// Get the weather information
//provider https://openweathermap.org/
///////////////////////////////////////////////////////////////////////////

public class OwmDataLoader {

    private final static String OWM_URL = "http://api.openweathermap.org/data/2.5/weather?lat=%f&lon=%f&lang=ru&units=metric";
    private final static String KEY = "APPID";  //"x-api-key";
    private final static int OK_RESP = 200;
    private static final String LOG_TAG = OwmDataLoader.class.getSimpleName();

    public static CityData getOwmData(final Context context, final double lat, final double lon) {
        try {
            OkHttpClient client = new OkHttpClient();
            HttpUrl.Builder urlBuilder = HttpUrl.parse(String.format(OWM_URL, lat, lon)).newBuilder();
            urlBuilder.addQueryParameter(KEY, context.getString(R.string.owm_api_key));
            final Request request = new Request.Builder().url(urlBuilder.build().toString()).build();

            Response response = client.newCall(request).execute();
            String resp = response.body().string();

            GsonBuilder builder = new GsonBuilder();
            Gson gson = builder.create();
            CityData cityData = gson.fromJson(resp, CityData.class);

            if (cityData.cod != OK_RESP)
                return null;

            return cityData;

        } catch (Exception e) {
            e.printStackTrace();
            Log.e(LOG_TAG, e.getMessage());
            return null;
        }
    }


}
