package com.example.james.a7project;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
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
import android.util.DisplayMetrics;
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
    GraphicsView gv;
    int count; //onCreate sets this to 0
    double[][] points; //initialised onCreate

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
            points[count][0] = latitude;
            points[count][1] = longitude;
            count++;
            gv.invalidate();
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
        int width = Resources.getSystem().getDisplayMetrics().widthPixels;

        LinearLayout map = findViewById(R.id.myMap);
        //0.03 are degrees of latitude I want map to cover and the map is 500px wide
        double scale = width / 0.03;

        public GraphicsView(Context c) {
            super(c);
        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            Paint p = new Paint();
            p.setColor(Color.BLUE);
            int i = 0;
            while (points[i][0] != 0.0) {
                float x = (float) points[i][0];
                float y = (float) points[i][1];
                //right top (-37.781,175.300) intersection of Ruakura Rd and Wairere Dr
                //left bottem (-37.797,175.326) Jansen Park
                //range of map will be roughily 0.03 degrees
                Log.d("A7", "Got point: " + x + "," + y + " width: " + width);
                x -= 175.3;
                y *= -1;
                y -= 37.78;
                //double x = 175.326 - 175.3;
                //double y = -37.781 - -37.797;
                //adjust for scale. ratio of 0.03 degree equals width of map
                x *= scale;
                y *= scale;
                Log.d("A7", "Drawing point: " + x + "," + y);
                canvas.drawCircle(x, y, 5, p);
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        locMan = (LocationManager)getSystemService(Context.LOCATION_SERVICE);

        LinearLayout map = findViewById(R.id.myMap);
        gv = new GraphicsView(this);
        map.addView(gv);

        locatON = false;
        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 42);
        } else {
            locatON = true;
        }
        
        points = new double[3000][2];
        count = 0;
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
