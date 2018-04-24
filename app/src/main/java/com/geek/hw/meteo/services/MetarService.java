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
import com.geek.hw.meteo.models.MetarData;
import com.geek.hw.meteo.ui.METARdataFragment;
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

public class MetarService extends IntentService {

    private static final String LOG_TAG = METARdataFragment.class.getSimpleName();
    private final static String METAR_URL = "https://api.checkwx.com/metar/lat/%f/lon/%f/decoded";
    private final static String KEY = "X-API-Key";

    public MetarService() {
        super("METAR");
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {

        if (intent != null) {
            float lat = intent.getFloatExtra(METARdataFragment.LATI, 0.00f);
            float lon = intent.getFloatExtra(METARdataFragment.LONG, 0.00f);

            try {
                OkHttpClient client = new OkHttpClient();
                HttpUrl.Builder urlBuilder = HttpUrl.parse(String.format(Locale.US, METAR_URL, lon, lat))
                        .newBuilder();
                final Request request = new Request.Builder().url(urlBuilder.build().toString())
                        .addHeader(KEY, getString(R.string.checkwx_api_key))
                        .build();

                Response response = client.newCall(request).execute();

                if (!response.isSuccessful()) {
                    Toast.makeText(getApplicationContext(), getString(R.string.metar_not_found),
                            Toast.LENGTH_LONG).show();
                } else {
                    String resp = response.body().string();
                    GsonBuilder builder = new GsonBuilder();
                    Gson gson = builder.create();
                    MetarData metarData = gson.fromJson(resp, MetarData.class);

                    ResultReceiver receiver = intent.getParcelableExtra(METARdataFragment.RECEIVER);
                    Bundle data = new Bundle();
                    data.putSerializable(MainActivity.CITY_NAME, metarData);
                    receiver.send(METARdataFragment.RES_CODE, data);

                }
            } catch (Exception e) {
                Log.e(LOG_TAG, e.getLocalizedMessage());
                Toast.makeText(getApplicationContext(), getString(R.string.server_error), Toast.LENGTH_LONG).show();
            }
        }

    }
}
