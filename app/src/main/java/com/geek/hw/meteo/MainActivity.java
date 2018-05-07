package com.geek.hw.meteo;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
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
import com.geek.hw.meteo.ui.SelectCityDialog;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.places.AutocompleteFilter;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.location.places.ui.PlaceAutocomplete;
import com.orhanobut.hawk.Hawk;

///////////////////////////////////////////////////////////////////////////
// MainActivity with drawer
///////////////////////////////////////////////////////////////////////////

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, AddCityDialogListener {

    public static double LAT;
    public static double LON;

    public final static String CITY_NAME = "city";
    public final static int SEL_CITY_REQUEST_CODE = 1;
    public final static int REQ_PERMISSION = 101;
    private static final String LOG_TAG = MainActivity.class.getSimpleName();
    private static String selectedCity;
    private NavigationView navigationView;
    private LocationManager locationManager;
    private Location location;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Hawk.init(this).build();

        if (Hawk.contains(CITY_NAME))
            selectedCity = Hawk.get(CITY_NAME);
        else
            selectedCity = getResources().getStringArray(R.array.cities)[0];

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQ_PERMISSION);
        } else {
            if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 5000.0f, locationListener);
                LAT = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER).getLatitude();
                LON = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER).getLongitude();
            } else if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                locationManager.requestSingleUpdate(LocationManager.NETWORK_PROVIDER, locationListener, null);
            }
//            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5, 5000.0f, locationListener);
//            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 5, 5000.0f, locationListener);
//            locationManager.requestLocationUpdates(LocationManager.PASSIVE_PROVIDER, 5, 5000.0f, locationListener);

        }

        if (savedInstanceState == null)
            setScreen(R.id.nav_open_weather);

    }

///////////////////////////////////////////////////////////////////////////
// Get location
///////////////////////////////////////////////////////////////////////////

    @Override
    protected void onResume() {
        super.onResume();

////        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
//        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
//            ActivityCompat.requestPermissions(this,
//                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQ_PERMISSION);
////            return;
//        } else {
////            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5, 5000.0f, locationListener);
////            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 5, 5000.0f, locationListener);
////            locationManager.requestLocationUpdates(LocationManager.PASSIVE_PROVIDER, 5, 5000.0f, locationListener);
//            locationManager.requestSingleUpdate(LocationManager.GPS_PROVIDER, locationListener, null);
////            locationManager.requestSingleUpdate(LocationManager.NETWORK_PROVIDER, locationListener, null);
////            locationManager.requestSingleUpdate(LocationManager.PASSIVE_PROVIDER, locationListener, null);
//        }

    }

    @Override
    protected void onPause() {
        super.onPause();
        locationManager.removeUpdates(locationListener);
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
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    this.onResume();
                }
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
//                new SelectCityDialog().show(getSupportFragmentManager(), "branch_filter_mode_dialog");
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
//            Log.i(LOG_TAG, place.getLatLng().latitude);
//            String s = place.getName().toString();
            LAT = place.getLatLng().latitude;
            LON = place.getLatLng().longitude;
            selectedCity = place.getName().toString();
            setScreen(R.id.nav_open_weather);
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

///////////////////////////////////////////////////////////////////////////
// Fragments call
///////////////////////////////////////////////////////////////////////////

    //    int itemId, String city
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

    LocationListener locationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
//            LAT = location.getLatitude();
//            LON = location.getLongitude();
        }

        @Override
        public void onStatusChanged(String s, int i, Bundle bundle) {

        }

        @Override
        public void onProviderEnabled(String s) {

        }

        @Override
        public void onProviderDisabled(String s) {

        }
    };
}
