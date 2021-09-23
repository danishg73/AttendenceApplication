package com.sahm.attendanceapp;

import android.Manifest;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
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
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;

public class office_location extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    Marker marker;
    Button cancel,addlocation;
    EditText l_name;
    String usertype, Manager_email,manager_username,getname,title;
    SharedPreferences sp;
    ProgressDialog progressDialog;
    Double lt,ln;
    String  manager,m_username,m_name,manager_name;
    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(LocaleHelper.onAttach(base));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        sp=getSharedPreferences("login",MODE_PRIVATE);

        if( sp.contains("user_type")){

            String v=  sp.getString("user_type",usertype).trim();
            if (v.equals("manager"))
            {

                Manager_email = sp.getString("email",manager).trim();
                manager_username = sp.getString("username",m_username).trim();
                manager_name = sp.getString("name",m_name).trim();
            }
            else if (v.equals("employee")){
                Intent intent = new Intent(office_location.this, Employee_Dashboard.class);
                startActivity(intent);
            }
            else{
                Intent intent = new Intent(office_location.this, LoginDashboardActivity.class);
                startActivity(intent);
            }
        }


        setContentView(R.layout.activity_office_location);
        progressDialog = new ProgressDialog(this);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
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
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        mMap.setMyLocationEnabled(true);
        getdata();

        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng clickCoords) {
                if (marker!=null) {
                    marker.remove();
                    marker=null;
                }
                LatLng newpos = new LatLng(clickCoords.latitude, clickCoords.longitude);
                marker= mMap.addMarker(new MarkerOptions().position(newpos).title(""));
                CameraUpdate cameraPosition = CameraUpdateFactory.newLatLngZoom( newpos, (float) 16.3);

                marker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ROSE));
                mMap.moveCamera(cameraPosition);
                mMap.animateCamera(cameraPosition);
                addlocation(""+clickCoords.latitude,""+clickCoords.longitude);
            }
        });




    }
    void addlocation(String lat, String lng)
    {

        Dialog customDialog = new Dialog(office_location.this);
        // customDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        customDialog.setContentView(R.layout.popup_addlocation);
        int width = (int) (getResources().getDisplayMetrics().widthPixels * 0.90);
//                int height = (int) (getResources().getDisplayMetrics().heightPixels * 0.57);
        customDialog.getWindow().setLayout(width, ViewGroup.LayoutParams.WRAP_CONTENT);

        cancel = customDialog.findViewById(R.id.cancel);
        l_name = customDialog.findViewById(R.id.name);
        addlocation = customDialog.findViewById(R.id.submit);
        addlocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getname =l_name.getText().toString().trim();
                if (TextUtils.isEmpty(getname))
                {
                    Toast.makeText(office_location.this, R.string.All_fields_are_required, Toast.LENGTH_SHORT).show();
                }
                else
                    {
                        progressDialog.setMessage(getResources().getString(R.string.Processing));
                        progressDialog.setCancelable(false);
                        progressDialog.show();
                        HashMap<String,Object> map = new HashMap<>();
                        map.put("lat",lat);
                        map.put("long",lng);
                        map.put("name",getname);
                        map.put("manager",Manager_email);
                        map.put("manager_username",manager_username);
                        FirebaseDatabase.getInstance().getReference("Office_Location").child(manager_username)
                                .setValue(map)
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful())
                                        {
                                            Toast.makeText(office_location.this, R.string.Location_Added, Toast.LENGTH_SHORT).show();
                                            customDialog.dismiss();
                                            progressDialog.dismiss();
                                        }
                                        else
                                            {
                                                Toast.makeText(office_location.this, R.string.Error, Toast.LENGTH_SHORT).show();
                                                progressDialog.dismiss();
                                        }
                                    }
                                });
                    }



            }
        });


        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                customDialog.dismiss();
                getdata();
            }
        });
        customDialog.setCancelable(true);
        customDialog.show();
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
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }



}