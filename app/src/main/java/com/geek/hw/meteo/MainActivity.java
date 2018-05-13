package com.geek.hw.meteo;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import com.geek.hw.meteo.ui.AddCityDialogListener;
import com.geek.hw.meteo.ui.METARdataFragment;
import com.geek.hw.meteo.ui.OWMdataFragment;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.AutocompleteFilter;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocomplete;
import com.google.android.gms.tasks.OnSuccessListener;
import com.orhanobut.hawk.Hawk;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

///////////////////////////////////////////////////////////////////////////
// MainActivity with drawer
///////////////////////////////////////////////////////////////////////////

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, AddCityDialogListener {

    private static double LAT;
    private static double LON;
    private static String selectedCity;
    private static String selectedRegion;

    public final static String CITY_NAME = "city";
    public final static String REG_NAME = "region";
    public final static String LATITUDE = "latitude";
    public final static String LONGITUDE = "longitude";
    public final static int SEL_CITY_REQUEST_CODE = 1;
    public final static int REQ_PERMISSION = 101;
    private static final String LOG_TAG = MainActivity.class.getSimpleName();
    private final Handler handler = new Handler();
    private NavigationView navigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Hawk.init(this).build();

        if (Hawk.contains(CITY_NAME))
            selectedCity = Hawk.get(CITY_NAME);

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        if (savedInstanceState == null)
            showGeoWeather();

    }

///////////////////////////////////////////////////////////////////////////
// Get location
///////////////////////////////////////////////////////////////////////////

    private void showGeoWeather() {

        FusedLocationProviderClient locationClient = LocationServices.getFusedLocationProviderClient(this);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQ_PERMISSION);
        } else {
            locationClient.getLastLocation().addOnSuccessListener(this, new OnSuccessListener<Location>() {
                @Override
                public void onSuccess(Location location) {
                    if (location != null) {
                        LAT = location.getLatitude();
                        LON = location.getLongitude();

                        getCityRegionName();

                        handler.post(new Runnable() {
                            public void run() {
                                setScreen(R.id.nav_open_weather);
                            }
                        });
                    }
                }
            });
        }
    }

    private void getCityRegionName() {
        Geocoder gc = new Geocoder(getApplicationContext(), Locale.getDefault());
        try {
            List<Address> addr = gc.getFromLocation(LAT, LON, 1);
            selectedCity = addr.get(0).getLocality();
            selectedRegion = addr.get(0).getAdminArea();
        } catch (IOException e) {
            e.printStackTrace();
            Log.e(LOG_TAG, e.getMessage());
        }
    }

///////////////////////////////////////////////////////////////////////////
// Save data
///////////////////////////////////////////////////////////////////////////

    @Override
    protected void onStop() {
        super.onStop();

        Hawk.put(CITY_NAME, selectedCity);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case REQ_PERMISSION:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                    showGeoWeather();
        }
    }

///////////////////////////////////////////////////////////////////////////
// Menu and navigation
///////////////////////////////////////////////////////////////////////////

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.nav_open_weather:
                setScreen(R.id.nav_open_weather);
                break;
            case R.id.nav_metar:
                setScreen(R.id.nav_metar);
                break;
        }


        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

///////////////////////////////////////////////////////////////////////////
// Google places
///////////////////////////////////////////////////////////////////////////

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_set_city:
                try {
                    AutocompleteFilter filter = new AutocompleteFilter.Builder()
                            .setTypeFilter(AutocompleteFilter.TYPE_FILTER_CITIES).build();
                    Intent intent = new PlaceAutocomplete.IntentBuilder(PlaceAutocomplete.MODE_OVERLAY)
                            .setFilter(filter).build(this);
                    startActivityForResult(intent, SEL_CITY_REQUEST_CODE);
                } catch (GooglePlayServicesNotAvailableException | GooglePlayServicesRepairableException e) {
                    Log.e(LOG_TAG, e.getMessage());
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

///////////////////////////////////////////////////////////////////////////
// Set another city and coordinates
///////////////////////////////////////////////////////////////////////////

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == SEL_CITY_REQUEST_CODE && resultCode == RESULT_OK) {
            Place place = PlaceAutocomplete.getPlace(this, data);
            LAT = place.getLatLng().latitude;
            LON = place.getLatLng().longitude;
            getCityRegionName();
//            selectedCity = place.getName().toString();
            setScreen(R.id.nav_open_weather);
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

///////////////////////////////////////////////////////////////////////////
// Fragments call
///////////////////////////////////////////////////////////////////////////

    private void setScreen(int itemId) {
        Fragment fragment;

        switch (itemId) {
            case R.id.nav_open_weather:
                fragment = new OWMdataFragment();
                break;
            case R.id.nav_metar:
                fragment = new METARdataFragment();
                break;
            default:
                fragment = new OWMdataFragment();
                break;
        }

        FragmentManager fragmentManager = getSupportFragmentManager();
        Bundle bundle = new Bundle();
        bundle.putString(CITY_NAME, selectedCity);
        bundle.putString(REG_NAME, selectedRegion);
        bundle.putDouble(LATITUDE, LAT);
        bundle.putDouble(LONGITUDE, LON);
        fragment.setArguments(bundle);
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.container, fragment);
        transaction.commit();
        navigationView.setCheckedItem(itemId);
    }

    @Override
    public void onSelectCity(String city) {
        selectedCity = city;
        setScreen(R.id.nav_open_weather);
    }

}
