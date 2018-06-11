package com.project.smartvehicle;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.util.Timer;
import java.util.TimerTask;

public class StartDrivingActivity extends AppCompatActivity {

    LocationManager locationManager;
    LocationListener locationListener;

    public static boolean isDriving = false;
    Button btn_start;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start_driving);
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            AlertDialog.Builder ad = new AlertDialog.Builder(StartDrivingActivity.this);
            ad.setTitle("GPS is turned off!").setMessage("You need to turn GPS on...")
                    .setPositiveButton("Turn it on!", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                        }
                    })
                    .setNegativeButton("OK!", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.dismiss();
                        }
                    })
                    .create();
            ad.show();
        }

        btn_start = (Button) findViewById(R.id.btn_start);
        btn_start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(isDriving){
                    isDriving = false;
                    btn_start.setText("Start Driving!");
                    stoplocation();
                } else {
                    final ProgressDialog pd = ProgressDialog.show(StartDrivingActivity.this,null,"Starting...");
                    Thread t = new Thread(new Runnable() {
                        @Override
                        public void run() {

                            boolean success = APIManager.getInstance().startride(StartDrivingActivity.this);
                            if(success){
                                success = APIManager.getInstance().startride_to_device();
                            }

                            pd.dismiss();
                            if(success)
                            {
                                StartDrivingActivity.this.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        startdriving();
                                    }
                                });
                            }

                        }
                    });
                    t.start();
                }
            }
        });

        final Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                if(isDriving) {

                    boolean success = APIManager.getInstance().getridedata();
                    if(success){
                        StartDrivingActivity.this.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                TextView tv1 = (TextView) findViewById(R.id.textView1);
                                TextView tv2 = (TextView) findViewById(R.id.textView2);
                                TextView tv3 = (TextView) findViewById(R.id.textView3);

                                tv1.setText("Speed: "+APIManager.getInstance().speed + " km/h");
                                tv2.setText(APIManager.getInstance().status);
                                tv3.setText("SpeedLimit: "+APIManager.getInstance().speedlimit + " km/h");
                            }
                        });
                    }
                }
            }
        }, 100,2000);

    }

    private void startdriving(){
        startlocation();
        isDriving = true;
        btn_start.setText("Stop Driving!");
    }

    private void startlocation()
    {
        locationListener = new MyLocationListener();

        if ( ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, MyLocationListener.MY_PERMISSION_ACCESS_FINE_LOCATION);
        } else {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 5, locationListener);
        }
    }

    private void stoplocation()
    {
        try {
            locationManager.removeUpdates(locationListener);
        }catch (SecurityException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == MyLocationListener.MY_PERMISSION_ACCESS_FINE_LOCATION) {
            startlocation();
        }
    }


    private class MyLocationListener implements LocationListener {

        private static final int MY_PERMISSION_ACCESS_FINE_LOCATION = 1012;

        @Override
        public void onLocationChanged(Location loc) {

            final String location = loc.getLatitude() + "," + loc.getLongitude();
            Thread t = new Thread(new Runnable() {
                @Override
                public void run() {
                    boolean success = APIManager.getInstance().newlocation_to_device(location);

                }
            });
            t.start();

        }

        @Override
        public void onProviderDisabled(String provider) {}

        @Override
        public void onProviderEnabled(String provider) {}

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {}
    }
}
