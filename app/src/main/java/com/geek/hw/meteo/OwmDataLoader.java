package com.geek.hw.meteo;

import android.content.Context;
import android.widget.Toast;

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

    private final static String OWM_URL = "http://api.openweathermap.org/data/2.5/weather?q=%s&lang=ru&units=metric";
    private final static String KEY = "APPID";  //"x-api-key";
    private final static int OK_RESP = 200;

    public static CityData getOwmData(final Context context, final String city) {
        try {
            OkHttpClient client = new OkHttpClient();
            HttpUrl.Builder urlBuilder = HttpUrl.parse(String.format(OWM_URL, city)).newBuilder();
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
            Toast.makeText(context, context.getString(R.string.server_error), Toast.LENGTH_LONG).show();
            return null;
        }
    }


}
