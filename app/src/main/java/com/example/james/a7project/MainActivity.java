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
    int count = 0; //onCreate sets this to 0
    Line[] lines = new Line[3000];


    double InitLat;
    double InitLon;

    LocationListener locLis = new LocationListener() {

        int width = Resources.getSystem().getDisplayMetrics().widthPixels;
        int height = Resources.getSystem().getDisplayMetrics().heightPixels;
        //0.01 are degrees of latitude I want the map to cover
        double degrees = 0.02;
        double scale = width / degrees;

        @Override
        public void onLocationChanged(Location location) {
            Log.d("A7", "Location information received");
            latitude = -location.getLatitude();
            longitude = location.getLongitude();
            Log.d("A7","New location latitude = " + latitude
             + ", longitude = " + longitude);
            TextView lt = findViewById(R.id.textViewLat);
            lt.setText("" + latitude);
            TextView lg = findViewById(R.id.textViewLong);
            lg.setText("" + longitude);

            //set top left corner so initial position is centre
            if(count == 0) {
                InitLat = latitude - (degrees/2);
                InitLon = longitude - (degrees/2);
            }

            Log.d("A7", "scale is " + scale + ". Top left corner is "
                    + InitLat + "," + InitLon);
            double adjustedLat = (latitude - InitLat) * scale;
            double adjustedLon = (longitude - InitLon) * scale;
            //double adjustedLat = (latitude - 37.4);
            //double adjustedLon = (-longitude - 122);

            if(adjustedLat < 0) adjustedLat = 0;
            if(adjustedLon < 0) adjustedLon = 0;
            if(adjustedLat > width) adjustedLat = width;
            if(adjustedLon > height) adjustedLon = height;

            //get information for new line
            if(count > 0) {
                //adjust for scale. ratio of 0.01 degree equals width of map
                lines[count] = new Line(lines[count - 1], adjustedLon, adjustedLat);
                Log.d("A7", "Created line " + count + " from " + lines[count].getStartX() + "," + lines[count].getStartY()
                                    + " to " + lines[count].getEndX() + "," + lines[count].getEndY());
                count++;
            }
            else {
                lines[0] = new Line(adjustedLon, adjustedLat);
                count++;
            }
            Log.d("A7","Stored location " + count + ": latitude = " + adjustedLat
                    + ", longitude = " + adjustedLon);
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

    class Line {
        private float startX, startY, endX, endY;
        public Line(double startX, double startY, double endX, double endY) {
            this.startX = (float)startX;
            this.startY = (float)startY;
            this.endX = (float)endX;
            this.endY = (float)endY;
        }

        //for the first point
        public Line(double startX, double startY) {
            this.startX = (float)startX;
            this.startY = (float)startY;
            this.endX = (float)startX;
            this.endY = (float)startY;
        }

        public Line(Line previous, double endX, double endY) {
            this.startX = previous.getEndX();
            this.startY = previous.getEndY();
            this.endX = (float)endX;
            this.endY = (float)endY;
        }

        public float getStartX() {
            return startX;
        }

        public float getStartY() {
            return startY;
        }

        public float getEndX() {
            return endX;
        }

        public float getEndY() {
            return endY;
        }
    }

    public class GraphicsView extends View {

        public GraphicsView(Context c) {
            super(c);
        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            Paint p = new Paint();
            p.setColor(Color.BLUE);
            Log.d("A7", "onDraw");
            if(count > 1) {
                Log.d("A7", "Drawing to point " + (count - 1));
                Log.d("A7", lines[count - 1].getEndX() + "," + lines[count - 1].getEndY());
                //draw all lines
                for (int i = 0; i < count; i++) {
                    canvas.drawLine(lines[i].getStartX(), lines[i].getStartY(), lines[i].getEndX(), lines[i].getEndY(), p);
                }
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d("A7", "onDestroy");
        count = 0;
    }
}
