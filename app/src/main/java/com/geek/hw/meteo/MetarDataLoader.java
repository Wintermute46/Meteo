package com.geek.hw.meteo;

import com.geek.hw.meteo.models.MetarData;
import android.content.Context;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.squareup.okhttp.HttpUrl;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import java.util.Locale;

///////////////////////////////////////////////////////////////////////////
// Get the latest METAR information in a decoded format
// for a single station closest to a parameter latitude/longitude.
//provider https://checkwx.com/
//METAR https://ru.wikipedia.org/wiki/METAR
///////////////////////////////////////////////////////////////////////////

public class MetarDataLoader {

    private final static String METAR_URL = "https://api.checkwx.com/metar/lat/%f/lon/%f/decoded";
    private final static String KEY = "X-API-Key";

    public static MetarData getMetarData(final Context context, final double lat, final double lon) {

            try {
                OkHttpClient client = new OkHttpClient();
                HttpUrl.Builder urlBuilder = HttpUrl.parse(String.format(Locale.US, METAR_URL, lat, lon))
                                                    .newBuilder();
                final Request request = new Request.Builder().url(urlBuilder.build().toString())
                                                    .addHeader(KEY, context.getString(R.string.checkwx_api_key))
                                                    .build();

                Response response = client.newCall(request).execute();

                if(response.isSuccessful()) {
                    String resp = response.body().string();

                    GsonBuilder builder = new GsonBuilder();
                    Gson gson = builder.create();
                    return gson.fromJson(resp, MetarData.class);

                } else
                    return null;

            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(context, context.getString(R.string.server_error), Toast.LENGTH_LONG).show();
                return null;
            }
    }

}
