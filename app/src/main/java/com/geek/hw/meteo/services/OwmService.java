package com.geek.hw.meteo.services;

import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

import com.geek.hw.meteo.MainActivity;
import com.geek.hw.meteo.R;
import com.geek.hw.meteo.models.CityData;
import com.geek.hw.meteo.ui.OWMdataFragment;
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

public class OwmService extends IntentService {

    private static final String LOG_TAG = OWMdataFragment.class.getSimpleName();
    private final static String OWM_URL = "http://api.openweathermap.org/data/2.5/weather?q=%s&lang=ru&units=metric";
    private final static String KEY = "APPID";  //"x-api-key";
    private final static int OK_RESP = 200;

    public OwmService() {
        super("OWM");
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        String city;

        if (intent != null) {
            city = intent.getStringExtra(MainActivity.CITY_NAME);

            try {
                OkHttpClient client = new OkHttpClient();
                HttpUrl.Builder urlBuilder = HttpUrl.parse(String.format(OWM_URL, city)).newBuilder();
                urlBuilder.addQueryParameter(KEY, getString(R.string.owm_api_key));
                final Request request = new Request.Builder().url(urlBuilder.build().toString()).build();

                Response response = client.newCall(request).execute();
                String resp = response.body().string();

                GsonBuilder builder = new GsonBuilder();
                Gson gson = builder.create();
                CityData cityData = gson.fromJson(resp, CityData.class);

                if (cityData.cod != OK_RESP)
                    Toast.makeText(getApplicationContext(), getString(R.string.place_not_found),
                            Toast.LENGTH_LONG).show();
                else {
                    ResultReceiver receiver = intent.getParcelableExtra(OWMdataFragment.RECEIVER);
                    Bundle data = new Bundle();
                    data.putSerializable(MainActivity.CITY_NAME, cityData);
                    receiver.send(OWMdataFragment.RES_CODE, data);
                }


            } catch (Exception e) {
                Log.e(LOG_TAG, e.getLocalizedMessage());
                Toast.makeText(getApplicationContext(), getString(R.string.server_error), Toast.LENGTH_LONG).show();
            }
        }
    }
}
