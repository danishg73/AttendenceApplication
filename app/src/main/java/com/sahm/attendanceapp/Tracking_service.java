package com.sahm.attendanceapp;

/**
 * Created by intag on 2/1/2017.
 */


import android.Manifest;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

/**
 * This page is related to service which sends current info. related to driver
 */

public class Tracking_service extends Service {

    private static final int TODO = 0;
    public  LocationManager locationMangaer = null;
    public LocationListener locationListener = null;
    private double longg, latt, oldlat, oldlong, longg2, latt2;
    private String dir, dir2;
    private String cityName = null;
    private String address = null;
    private String city = null;
    private String state = null;
    private String country = null;
    private String knownName = null;
    private String speed = null;
    private String accuracy = null;
    private static final String TAG = "Debug";
    private Boolean flag = false;
    public String value, username,employee_email,key;
    public  DatabaseReference db;
    SimpleDateFormat date,time;
    String longitude, latitude;
    Calendar c = Calendar.getInstance();
    HashMap<String,Object> map2 = new HashMap<>();


    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(LocaleHelper.onAttach(base));
    }



    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }



    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // Let it continue running until it is stopped.
//        Toast.makeText(this, "Service Started", Toast.LENGTH_SHORT).show();


        if (intent !=null && intent.getExtras()!=null)
        {
            employee_email = intent.getExtras().getString("employee_email");
            key = intent.getExtras().getString("key");
        }
        db = FirebaseDatabase.getInstance().getReference().child("Tracking").child(key);
        date = new SimpleDateFormat("dd-MM-yyyy");// HH:mm:ss");
        time = new SimpleDateFormat("HH:mm:ss");// HH:mm:ss");

        locationMangaer = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        Log.e(TAG, "onClick");
        locationListener = new MyLocationListener();
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return TODO;
        }
        locationMangaer.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 10, locationListener);

        return START_NOT_STICKY;
    }

    /*----------Listener class to get coordinates ------------- */
    public class MyLocationListener implements LocationListener {


        @Override
        public void onLocationChanged(Location loc) {

            longitude =""+loc.getLongitude();
            latitude = ""+loc.getLatitude();
            longg = loc.getLongitude();
            latt = loc.getLatitude();

// getting direction
//            double radians = Math.atan2((longg - oldlong), (latt - oldlat));
//
//            double compassReading = radians * (180 / Math.PI);
//
//            String[] coordNames = {"N", "NE", "E", "SE", "S", "SW", "W", "NW", "N"};
//            long oordIndex = Math.round(compassReading / 45);
//            int coordIndex = Integer.parseInt(String.valueOf(oordIndex));
//            if (coordIndex < 0) {
//                coordIndex = coordIndex + 8;
//            }
//            ;
//            dir = coordNames[coordIndex]; // returns the coordinate value


    /*----------to get City-Name from coordinates ------------- */


            Geocoder gcd = new Geocoder(getBaseContext(),
                    Locale.getDefault());
            List<Address> addresses;
            try {
                addresses = gcd.getFromLocation(loc.getLatitude(), loc
                        .getLongitude(), 1);
                if (addresses.size() > 0)
                    System.out.println(addresses.get(0).getLocality());

                address = addresses.get(0).getAddressLine(0); // If any additional address line present than only, check with max available address lines by getMaxAddressLineIndex()
                //state = addresses.get(0).getAdminArea();
                country = addresses.get(0).getCountryName();
                //postalCode = addresses.get(0).getPostalCode();
                knownName = addresses.get(0).getFeatureName(); // Only if available else return NULL
                cityName = addresses.get(0).getLocality();
                speed = "" + ((loc.getSpeed()) * 3.6);
                accuracy = "" + loc.getAccuracy();

            } catch (IOException e) {
                e.printStackTrace();
            }
            map2.put("long",longitude);
            map2.put("lat",latitude);
            map2.put("employee_email",employee_email);
            map2.put("date",date.format(c.getTime()));
            map2.put("current_time",time.format(c.getTime()));
//            Toast.makeText(Tracking_service.this, z, Toast.LENGTH_LONG).show();
            db.updateChildren(map2).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(Tracking_service.this, getResources().getString(R.string.Error), Toast.LENGTH_SHORT).show();
                }
            });

        }


        @Override
        public void onProviderDisabled(String provider) {
            // TODO Auto-generated method stub
        }

        @Override
        public void onProviderEnabled(String provider) {
            // TODO Auto-generated method stub
        }

        @Override
        public void onStatusChanged(String provider,
                                    int status, Bundle extras) {
            // TODO Auto-generated method stub
        }

    }
    @Override
    public void onDestroy()
    {
        locationMangaer.removeUpdates(locationListener);
        locationMangaer = null;
        stopService(new Intent(this, Tracking_service.class));
        super.onDestroy();

    }

}
