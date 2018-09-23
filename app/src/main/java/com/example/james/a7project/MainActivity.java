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
    Line[] lines;

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
            //points[count][0] = latitude;
            //points[count][1] = longitude;
            //double i = 0.001 * count;
            //points[count][0] = i + 175.3;
            //points[count][1] = -i + -37.781;
            //count++;
            //gv.invalidate();
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
        float startX, startY, endX, endY;
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
            this.startX = (float)startX;
            this.startY = (float)startY;
        }

        public Line(Line previous, double endX, double endY) {
            this.startX = previous.startX;
            this.startY = previous.startY;
            this.endX = (float)endX;
            this.endY = (float)endY;
        }

    }

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

            //get information for new line
            double j = 0.001 * count;
            if(count == 0) {
                //lines[0] = new Line(latitude - 175.3, -longitude - 37.781);
                lines[0] = new Line(j, j);
            }
            else {
                //adjust for scale. ratio of 0.03 degree equals width of map
                //lines[count] = new Line(lines[count - 1], (latitude - 175.3) * scale, (-longitude - 37.781) * scale);
                lines[count] = new Line(lines[count - 1], j * scale, j * scale);
            }

            //right top (-37.781,175.300) intersection of Ruakura Rd and Wairere Dr
            //left bottem (-37.797,175.326) Jansen Park
            //range of map will be roughily 0.03 degrees
            //double x = 175.326 - 175.3;
            //double y = -37.781 - -37.797;

            Log.d("A7", "Drawing to point: " + lines[count].endX + "," + lines[count].endY);
            //for (int i = 0; i<= count; i++) {
            int i = count;
                canvas.drawLine(lines[i].startX, lines[i].startY, lines[i].endX, lines[i].endY, p);
            //}

            count++;
            invalidate();
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
        
        lines = new Line[3000];
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
