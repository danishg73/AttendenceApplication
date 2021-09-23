package com.sahm.attendanceapp;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class officeloc_employee extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    Marker marker;
    String   manager_username ,title;
    SharedPreferences sp;
    Double lt,ln;

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(LocaleHelper.onAttach(base));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_officeloc_employee);


        sp=getSharedPreferences("login",MODE_PRIVATE);

        if( sp.contains("user_type")){

            String v=  sp.getString("user_type","usertype").trim();
            if (v.equals("manager"))
            {

                Intent intent = new Intent(officeloc_employee.this, Employee_Dashboard.class);
                startActivity(intent);
            }
            else if (v.equals("employee")){

                manager_username = getIntent().getStringExtra("manager_username");
            }
            else{
                Intent intent = new Intent(officeloc_employee.this, LoginDashboardActivity.class);
                startActivity(intent);
            }
        }


        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        getdata();
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

        // Add a marker in Sydney and move the camera
        LatLng sydney = new LatLng(-34, 151);
        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
    }


    void getdata()
    {
        FirebaseDatabase.getInstance().getReference("Office_Location").
                child(manager_username).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            {
                if(dataSnapshot.exists())
                {
                    lt = Double.parseDouble(dataSnapshot.child("lat").getValue().toString().trim());
                    ln =  Double.parseDouble(dataSnapshot.child("long").getValue().toString().trim());
                    title =   dataSnapshot.child("name").getValue().toString().trim();
                    LatLng newpos = new LatLng( lt, ln);
                    marker= mMap.addMarker(new MarkerOptions().position(newpos).title(title));
                    CameraUpdate cameraPosition = CameraUpdateFactory.newLatLngZoom( newpos, (float) 16.3);
                    marker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ROSE));
                    mMap.moveCamera(cameraPosition);
                    mMap.animateCamera(cameraPosition);
                }
                else
                {
                    Toast.makeText(officeloc_employee.this,  getResources().getString(R.string.Manager_didnt_insert), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

}