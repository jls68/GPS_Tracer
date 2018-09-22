package com.example.james.a7project;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Toolbar;

public class MainActivity extends AppCompatActivity {

    boolean locatON;
    LocationManager locMan;
    double latitude;
    double longitude;

    LocationListener locLis = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            Log.d("A7", "Location information received");
            latitude = location.getLatitude();
            longitude = location.getLongitude();
            Log.d("A7","New location latitude = " + latitude
             + ", longitude = " + longitude);
            TextView lt = findViewById(R.id.textViewLat);
            lt.setText("" + latitude);
            TextView lg = findViewById(R.id.textViewLong);
            lg.setText("" + longitude);
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

    public class GraphicsView extends View {
        //LinearLayout map = findViewById(R.id.myMap);

        public GraphicsView(Context c) {
            super (c);
        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            //right top (-37.781,175.300) intersection of Ruakura Rd and Wairere Dr
            float x = 100;
            float y = 100;
            //float x = map.getLeft() + 100;
            //float y = map.getTop() + 100;
            //if( x >= map.getLeft() &&
            //    x <= map.getRight() &&
            //    y <= map.getTop() &&
            //    y >= map.getBottom()){
                Log.d("A7", "Drawing point: " + latitude + "," + longitude);
                Paint p = new Paint();
                p.setColor(Color.BLUE);
                canvas.drawCircle(x, y, 10, p);
            //}
            invalidate();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        locMan = (LocationManager)getSystemService(Context.LOCATION_SERVICE);

        LinearLayout map = findViewById(R.id.myMap);
        GraphicsView gv = new GraphicsView(this);
        map.addView(gv);

        locatON = false;
        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 42);
        } else {
            locatON = true;
        }
    }

    public void mapButtonHandler(View v) {
        if (locatON) {
            Uri intentUri = Uri.parse("geo:" + latitude + "," + longitude);
            Intent mapIntent = new Intent(Intent.ACTION_VIEW, intentUri);
            mapIntent.setPackage("com.google.android.apps.maps");
            startActivity(mapIntent);
        }
        else Toast.makeText(getApplicationContext(), "Waiting on location services to be allowed", Toast.LENGTH_SHORT);
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
        Log.d("A7", "onResume");
        if (locatON) {
            startLocationUpdates();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d("A7", "onPause");
        if (locatON) {
            stopLocationUpdates();
        }
    }

    public void startLocationUpdates() {
        try {
            locMan.requestLocationUpdates(locMan.GPS_PROVIDER, 2000, 1, locLis);
            Log.d("A7", "starting LocationUpdates");
        } catch (SecurityException ex) {
            Log.d("A7","Can't start location updates");
            finish();
        }
    }

    public void stopLocationUpdates() {
        locMan.removeUpdates(locLis);
        Log.d("A7", "stopping LocationUpdates");
    }
}
