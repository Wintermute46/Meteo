package com.geek.hw.meteo;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
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
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.location.places.ui.PlaceAutocomplete;
import com.orhanobut.hawk.Hawk;

///////////////////////////////////////////////////////////////////////////
// MainActivity with drawer
///////////////////////////////////////////////////////////////////////////

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, AddCityDialogListener {

    public static float LAT;
    public static float LON;

    public final static String CITY_NAME = "city";
    public final static int SEL_CITY_REQUEST_CODE = 1;
    private static final String LOG_TAG = MainActivity.class.getSimpleName();
    private static String selectedCity;
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
        else
            selectedCity = getResources().getStringArray(R.array.cities)[0];

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        if (savedInstanceState == null)
            setScreen(R.id.nav_open_weather, selectedCity);

    }

///////////////////////////////////////////////////////////////////////////
// Save data
///////////////////////////////////////////////////////////////////////////

    @Override
    protected void onStop() {
        super.onStop();

        Hawk.put(CITY_NAME, selectedCity);
    }

///////////////////////////////////////////////////////////////////////////
// Menu and navigation
///////////////////////////////////////////////////////////////////////////

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == SEL_CITY_REQUEST_CODE && resultCode == RESULT_OK) {
            Place place = PlaceAutocomplete.getPlace(this, data);
            Log.i(LOG_TAG, place.getName().toString());
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

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
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_set_city:
                try {
                    Intent intent = new PlaceAutocomplete.IntentBuilder(PlaceAutocomplete.MODE_OVERLAY).build(this);
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

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.nav_open_weather:
                setScreen(R.id.nav_open_weather, selectedCity);
                break;
            case R.id.nav_metar:
                setScreen(R.id.nav_metar, selectedCity);
                break;
        }


        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onSelectCity(String city) {
        selectedCity = city;
        setScreen(R.id.nav_open_weather, selectedCity);
    }

///////////////////////////////////////////////////////////////////////////
// Fragments call
///////////////////////////////////////////////////////////////////////////

    private void setScreen(int itemId, String city) {
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
        bundle.putString(CITY_NAME, city);
        fragment.setArguments(bundle);
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.container, fragment);
        transaction.commit();
        navigationView.setCheckedItem(itemId);
    }
}
