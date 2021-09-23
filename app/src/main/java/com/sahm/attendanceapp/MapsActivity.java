package com.sahm.attendanceapp;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    String employee_username,usertype;
    SharedPreferences sp;
    Query query;
    Marker marker;
    LatLng p;
    String la,lo;
    CameraUpdate cameraPosition;
    double lat, lon;
    LinearLayout tracking;
    TextView track;
    AlertDialog.Builder builder1;
    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(LocaleHelper.onAttach(base));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        sp=getSharedPreferences("login",MODE_PRIVATE);

        if( sp.contains("user_type")){

            String v=  sp.getString("user_type",usertype).trim();
            if (v.equals("manager"))
            {
            }
            else if (v.equals("employee"))
            {
                Intent intent = new Intent(MapsActivity.this, LoginDashboardActivity.class);
                startActivity(intent);
            }
            else{
                Intent intent = new Intent(MapsActivity.this, LoginDashboardActivity.class);
                startActivity(intent);
            }
        }
        tracking = findViewById(R.id.ll_track);
        track = findViewById(R.id.tracking);
        builder1 = new AlertDialog.Builder( this);
        track.setAnimation(AnimationUtils.loadAnimation(MapsActivity.this, R.anim.flash_leave_now));
        employee_username = getIntent().getStringExtra("employee_username");
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        query = FirebaseDatabase.getInstance().getReference("Tracking")
                .orderByChild("employee_username").equalTo(employee_username);







    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            {
                if(dataSnapshot.exists())
                {
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        if (marker!=null) {
                            marker.remove();
                            marker=null;
                        }
                        if(snapshot.child("lat").getValue().toString().trim().equals("abc"))
                        {
                            tracking.setVisibility(View.INVISIBLE);
                            builder1.setMessage(getResources().getString(R.string.Low_gps_Signals));
                            builder1.setCancelable(false);

                            builder1.setNegativeButton(getResources().getString(R.string.Ok),
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int id) {
                                            dialog.cancel();
                                            MapsActivity.super.onBackPressed();
                                        }
                                    });

                            AlertDialog alert11 = builder1.create();
                            alert11.show();



                        }
                        else
                        {
                            tracking.setVisibility(View.VISIBLE);
                            lat =Double.parseDouble(snapshot.child("lat").getValue().toString().trim());
                            lon =Double.parseDouble(snapshot.child("long").getValue().toString().trim());
                            p = new LatLng(lat,lon);
                            marker = mMap.addMarker(new MarkerOptions().position(p).title("Time: "+snapshot.child("current_time").getValue().toString().trim()));
                            cameraPosition = CameraUpdateFactory.newLatLngZoom( p, (float) 17.4);
                            mMap.moveCamera(cameraPosition);
                            mMap.animateCamera(cameraPosition);
                            mMap.moveCamera(CameraUpdateFactory.newLatLng(p));

                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}