package com.example.james.a7project;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toolbar;

public class MainActivity extends AppCompatActivity {

    boolean locatON;
    LocationManager locMan;

    LocationListener locLis = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            Log.d("A7", "Location information received");
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {

        }

        @Override
        public void onProviderEnabled(String provider) {

        }

        @Override
        public void onProviderDisabled(String provider) {

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        locMan = (LocationManager)getSystemService(Context.LOCATION_SERVICE);

        locatON = false;
        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 42);
        } else {
            locatON = true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 42) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                locatON = true;
                startLocationUpdates();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (locatON) {
            startLocationUpdates();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (locatON) {
            stopLocationUpdates();
        }
    }

    public void startLocationUpdates() {
        try {
            locMan.requestLocationUpdates(locMan.GPS_PROVIDER, 2000, 1, locLis);
        } catch (SecurityException ex) {
            Log.d("A7","Can't start location updates");
            finish();
        }
    }

    public void stopLocationUpdates() {
        locMan.removeUpdates(locLis);
    }
}
