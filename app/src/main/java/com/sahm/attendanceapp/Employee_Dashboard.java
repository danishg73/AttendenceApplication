package com.sahm.attendanceapp;

import android.Manifest;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.messaging.FirebaseMessaging;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class  Employee_Dashboard extends AppCompatActivity {

    TextView starttime,endtime,profile_name, mon,tues,wed,thurs,fri,sat,sun,team_name;
    String usertype,shift_day,manager_email,today_date_time,current_date, current_time,manager_username;
    SharedPreferences sp;
    String employee_email,email,employee_name,name,username,employee_username,paid_type,current_currency;
    private ProgressDialog progressDialog, progressDialog2;
    LinearLayout assigments;
    Button punch;
    String Day,holiday="no";
    String  x[];
    String mark= "no",country_name,city_name;
    boolean GpsStatus ;
    ImageView msg;
    String current_time_unpunch;
    AlertDialog.Builder builder1;
    Calendar c = Calendar.getInstance();
    Intent intent_location;
    private AdView mAdView;
    private InterstitialAd mInterstitialAd;
    private RequestQueue mRequestQue;
    private String URL = "https://fcm.googleapis.com/fcm/send";
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
                Intent intent = new Intent(Employee_Dashboard.this, Manager_Dashboard.class);
                startActivity(intent);
            }
            else if (v.equals("employee")){
                employee_email  =  sp.getString("email",email).trim();
                employee_name  =  sp.getString("name",name).trim();
                employee_username = sp.getString("username",username).trim();
                paid_type = sp.getString("paid_type", "").trim();

            }
            else{
                Intent intent = new Intent(Employee_Dashboard.this, LoginDashboardActivity.class);
                startActivity(intent);
            }
        }
        setContentView(R.layout.activity_employee__dashboard);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setItemIconTintList(null);
        starttime = findViewById(R.id.start_time);
        endtime = findViewById(R.id.end_time);
        assigments = findViewById(R.id.assignment);
        msg=findViewById(R.id.mail);
        mon=findViewById(R.id.monday);
        tues=findViewById(R.id.tuesday);
        wed=findViewById(R.id.wednesday);
        thurs=findViewById(R.id.thursday);
        fri=findViewById(R.id.friday);
        sat=findViewById(R.id.saturday);
        sun=findViewById(R.id.sunday);
        team_name=findViewById(R.id.team_name);
        punch=findViewById(R.id.punch);
        mAdView = findViewById(R.id.adView);
        View headerView = navigationView.getHeaderView(0);
        profile_name= headerView.findViewById(R.id.profile_name);
        profile_name.setText(employee_name);
        builder1 = new AlertDialog.Builder(Employee_Dashboard.this);
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage( getResources().getString(R.string.Loading));
        progressDialog.show();

        setTitle(R.string.Employee_DashBoard);

        FirebaseMessaging.getInstance().subscribeToTopic("Employees");
        FirebaseMessaging.getInstance().subscribeToTopic(employee_username);
        FirebaseMessaging.getInstance().subscribeToTopic("All");
        MobileAds.initialize(this, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {}
        });
        MobileAds.initialize(this,getResources().getString(R.string.adds_key));
        mInterstitialAd = new InterstitialAd(this);
        mInterstitialAd.setAdUnitId(getResources().getString(R.string.interstitial_full_screen));
        get_data();
        version_check();
        location_permission();
        punch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                String x = punch.getText().toString();
                if(mark.equals("yes"))
                {
                    builder1.setMessage(getResources().getString(R.string.Do_you_want_to_unpunch));
                    builder1.setCancelable(true);

                    builder1.setPositiveButton(getResources().getString(R.string.Yes),
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    punch.setText(R.string.Punch);
                                    punch.setBackgroundResource(R.drawable.buttondesign);
                                    mark="no";
                                    un_mark_attendance();
                                }
                            });
                    builder1.setNegativeButton(getResources().getString(R.string.No),
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    dialog.cancel();
                                }
                            });

                    AlertDialog alert11 = builder1.create();
                    alert11.show();

                }
                else
                {
                    Date date = new Date();
                    Day = DateFormat.format("EEEE", date.getTime()).toString();
                    SimpleDateFormat df1 = new SimpleDateFormat("dd-MM-yyyy");// HH:mm:ss");
                    current_date = df1.format(c.getTime());

                    if(shift_day.contains(Day))
                    {
                        check_holiday();
                    }
                    else
                    {
                        Toast.makeText(Employee_Dashboard.this, R.string.Today_is_not_your_shift_day, Toast.LENGTH_SHORT).show();
                        check_holiday();
                    }
                }
            }

        });
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {

                // Handle navigation view item clicks here.
                int id = menuItem.getItemId();

                if (id == R.id.nav_user)
                {
                    Intent intent = new Intent(Employee_Dashboard.this, EmployeeProfileActivity.class);
                    intent.putExtra("target_email",employee_email);
                    intent.putExtra("target_name",employee_name);
                    intent.putExtra("target_username",employee_username);
                    intent.putExtra("currency",current_currency);
                    startActivity(intent);
                }
                else if (id == R.id.nav_csv_report)
                {

                }
                else if (id == R.id.nav_holidays)
                {
                    Intent intent = new Intent(Employee_Dashboard.this, HolidaysActivity.class);
                    startActivity(intent);

                }
                else if (id == R.id.nav_setting)
                {
                    Intent intent = new Intent(Employee_Dashboard.this, SettingsActivity.class);
                    startActivity(intent);

                }
                else if (id == R.id.nav_work_report)
                {
                    Intent intent = new Intent(Employee_Dashboard.this, Work_report.class);
                    startActivity(intent);

                }
                else if (id == R.id.nav_office_loc)
                {
                    Intent intent = new Intent(Employee_Dashboard.this, officeloc_employee.class);
                    intent.putExtra("manager_username",manager_username);
                    startActivity(intent);

                }
                else if (id == R.id.nav_currency)
                {
//                    Intent intent = new Intent(Employee_Dashboard.this, Currency.class);
//                    startActivity(intent);
                }
                else if (id == R.id.nav_share)
                { }
                else if (id == R.id.nav_logout)
                {

                    FirebaseMessaging.getInstance().unsubscribeFromTopic("Employees");
                    FirebaseMessaging.getInstance().unsubscribeFromTopic("All");
                    intent_location = new Intent(Employee_Dashboard.this, Tracking_service.class);
                    stopService(intent_location );
                    resetInstanceId();
                    SharedPreferences.Editor e=sp.edit();
                    e.clear();
                    e.commit();
                    Intent intent = new Intent(Employee_Dashboard.this, LoginDashboardActivity.class);
                    startActivity(intent);
                    finish();
                }
                DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
                drawer.closeDrawer(GravityCompat.START);
                return false;
            }
        });
        assigments.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Employee_Dashboard.this, task_view.class);
                startActivity(intent);
            }
        });
        msg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {



                Intent intent = new Intent(Employee_Dashboard.this, Userlist_message.class);
                intent.putExtra("manager_email",manager_email);
                startActivity(intent);
            }
        });
    }


    public void un_mark_attendance()
    {

        progressDialog2 = new ProgressDialog(this);
        progressDialog2.setMessage(getResources().getString(R.string.Unmarking));
        progressDialog2.show();

        Calendar ct = Calendar.getInstance();
        SimpleDateFormat df1 = new SimpleDateFormat("HH:mm:ss");// HH:mm:ss");
        current_time_unpunch = df1.format(ct.getTime());
//        x = today_date_time.split("\\s+");
//        current_time =x[1];
        HashMap<String,Object> map = new HashMap<>();
        map.put("Punch_out_time",current_time_unpunch);
        map.put("Punched","no");
        Query query = FirebaseDatabase.getInstance().getReference("Attendance")
                .child(manager_username).child(employee_username).orderByChild("Punched").equalTo("yes");

//        FirebaseDatabase.getInstance().getReference().child("Employee_Task").push()
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            {
                if(dataSnapshot.exists())
                {
                    for (DataSnapshot dataSnapshot1: dataSnapshot.getChildren())
                    {
                        dataSnapshot1.getRef().updateChildren(map);
                        progressDialog2.dismiss();
                        sendNotification(employee_name+getResources().getString(R.string.unmarked_his_Attendance_at)+current_time_unpunch);
                        stop_tracking();

                    }

                }
                else
                {
                    Toast.makeText(Employee_Dashboard.this, R.string.No_Marked_Attendance_found, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        HashMap<String,Object> map2 = new HashMap<>();
        map2.put("Punched","no");
        Query query2 = FirebaseDatabase.getInstance().getReference().child("Employee_data").orderByChild("username").equalTo(employee_username);
        query2.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists())
                {
                    for (DataSnapshot snapshot: dataSnapshot.getChildren())
                    {
                        snapshot.getRef().updateChildren(map2);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
    private void check_holiday()
    {
        Query q1 = FirebaseDatabase.getInstance().getReference("Holidays").orderByChild("manager").equalTo(manager_email);
        q1.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists())
                {
                    for (DataSnapshot snapshot: dataSnapshot.getChildren())
                    {
                        String d = snapshot.child("holiday_date").getValue().toString().trim();
                        if (d.equals(current_date))
                        {
                            holiday="yes";
                        }
                    }
                    if (holiday.equals("no"))
                    {
                        mark_attendance();
                    }
                    else if(holiday.equals("yes"))
                    {
                        builder1.setMessage(getResources().getString(R.string.Today_is_your_holiday_do_you_really_want_to_punch));
                        builder1.setCancelable(true);

                        builder1.setPositiveButton(getResources().getString(R.string.Yes),
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id)
                                    {
                                        mark_attendance();
                                    }
                                });
                        builder1.setNegativeButton(getResources().getString(R.string.No),
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        dialog.cancel();
                                    }
                                });

                        AlertDialog alert11 = builder1.create();
                        alert11.show();
                    }

                }
                else
                    {
                        mark_attendance();
                    }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    private void mark_attendance()
    {

        turnGPSOn();
        progressDialog2 = new ProgressDialog(this);
        progressDialog2.setMessage(getResources().getString(R.string.Punching));
        progressDialog2.show();
        Calendar c = Calendar.getInstance();
        SimpleDateFormat month_date = new SimpleDateFormat("MMMM");
        String month_name = month_date.format(c.getTime());
        SimpleDateFormat df = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");// HH:mm:ss");
        today_date_time = df.format(c.getTime());
        x = today_date_time.split("\\s+");
        current_time =x[1];
        HashMap<String,Object> map = new HashMap<>();
            map.put("day",Day);
            map.put("date",current_date);
            map.put("month",month_name);
            map.put("last_update","not in use");
            map.put("Punch_out_time","not added");
            map.put("Punched","yes");
            map.put("punch_in_time",current_time);
            map.put("employee_email",employee_email);
            DatabaseReference db;
            db = FirebaseDatabase.getInstance().getReference("Attendance").child(manager_username).child(employee_username).push();
            String key= db.getKey();
            map.put("key",key);
//        FirebaseDatabase.getInstance().getReference().child("Employee_Task").push()
            db.setValue(map)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {

                            if(task.isSuccessful())
                            {
                                Toast.makeText(Employee_Dashboard.this, getResources().getString(R.string.Attendance_Marked), Toast.LENGTH_SHORT).show();
                                mark="yes";

                                punch.setBackgroundResource(R.drawable.buttondesign_red);
                                punch.setText(R.string.Unpunch);
                                progressDialog2.dismiss();
                                sendNotification(employee_name+ getResources().getString(R.string.marked_his_Attendance_at)+current_time);
                                start_tracking();
                            }
                            else
                            {
                                Toast.makeText(Employee_Dashboard.this, getResources().getString(R.string.Error), Toast.LENGTH_SHORT).show();
                                progressDialog2.dismiss();
                            }

                        }
                    });
        HashMap<String,Object> map2 = new HashMap<>();
        map2.put("Punched","yes");
        map2.put("punch_in_time",current_time); 
            Query query = FirebaseDatabase.getInstance().getReference().child("Employee_data").orderByChild("username").equalTo(employee_username);
            query.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if(dataSnapshot.exists())
                    {
                        for (DataSnapshot snapshot: dataSnapshot.getChildren())
                        {
                            snapshot.getRef().updateChildren(map2);
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }

    private void start_tracking()
    {
        HashMap<String,Object> map2 = new HashMap<>();
        map2.put("long","abc");
        map2.put("lat","abc");
        map2.put("date",current_date);
        map2.put("current_time",current_time);
        map2.put("employee_email",employee_email);
        map2.put("employee_username",employee_username);
        DatabaseReference db;
        db=FirebaseDatabase.getInstance().getReference("Tracking").push();
        String key= db.getKey();
        map2.put("key",key);
        db.setValue(map2).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task)
            {
                intent_location = new Intent(Employee_Dashboard.this, Tracking_service.class);
                intent_location.putExtra("employee_email", employee_email);
                intent_location.putExtra("key", key);
                startService(intent_location);
            }
        })
        .addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(Employee_Dashboard.this,getResources().getString(R.string.Tracking_failed), Toast.LENGTH_SHORT).show();
            }
        });

    }
    private void stop_tracking()
    {
        intent_location = new Intent(Employee_Dashboard.this, Tracking_service.class);
        stopService(intent_location );
        delete_data();
    }
    public void delete_data()
    {
        Query query1;
        query1 = FirebaseDatabase.getInstance().getReference("Tracking").orderByChild("employee_email").equalTo(employee_email);
        query1.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists())
                {
                    dataSnapshot.getRef().removeValue();
                    Toast.makeText(Employee_Dashboard.this, R.string.Tracking_Stopped, Toast.LENGTH_SHORT).show();

                }
                else
                    {
//                        Toast.makeText(Employee_Dashboard.this, R.string.No_Tracking_Found, Toast.LENGTH_SHORT).show();
                    }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        return super.onOptionsItemSelected(item);
    }

    public void get_data()
    {
        Query query1 = FirebaseDatabase.getInstance().getReference("Employee_data")
                .orderByChild("email").equalTo(employee_email);

        query1.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists())
                {
                    for (DataSnapshot snapshot : dataSnapshot.getChildren())
                    {
                        manager_email = snapshot.child("manager").getValue().toString();
                        manager_username = snapshot.child("manager_username").getValue().toString();

                        SharedPreferences.Editor edit = sp.edit();
                        edit.putString("manager_username", manager_username);
                        edit.commit();

                        String shift_name = snapshot.child("shift").getValue().toString().trim();
                        team_name.setText(shift_name);
                        check_paid();
                        get_currency();
                        Query query2 = FirebaseDatabase.getInstance().getReference("Timings")
                                        .orderByChild("manager").equalTo(manager_email);
                        query2.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
                            {
                                if (dataSnapshot.exists())
                                {
                                    for (DataSnapshot snapshot : dataSnapshot.getChildren())
                                    {
                                        if(snapshot.child("shiftname").getValue().toString().trim().equals(shift_name))
                                        {

                                            punch_check();
                                            shift_day = snapshot.child("days").getValue().toString();
                                            starttime.setText(snapshot.child("start_time").getValue().toString().trim());
                                            endtime.setText(snapshot.child("end_time").getValue().toString().trim());
                                            team_name.setText(snapshot.child("shiftname").getValue().toString().trim());
                                            if(shift_day.contains("Monday"))
                                            {
                                                mon.setBackgroundResource(R.drawable.days_selected);
                                            }
                                            if(shift_day.contains("Tuesday"))
                                            {
                                                tues.setBackgroundResource(R.drawable.days_selected);
                                            }
                                            if(shift_day.contains("Wednesday"))
                                            {
                                                wed.setBackgroundResource(R.drawable.days_selected);
                                            }
                                            if(shift_day.contains("Thursday"))
                                            {
                                                thurs.setBackgroundResource(R.drawable.days_selected);
                                            }
                                            if(shift_day.contains("Friday"))
                                            {
                                                fri.setBackgroundResource(R.drawable.days_selected);
                                            }
                                            if(shift_day.contains("Saturday"))
                                            {
                                                sat.setBackgroundResource(R.drawable.days_selected);
                                            }
                                            if(shift_day.contains("Sunday"))
                                            {
                                                sun.setBackgroundResource(R.drawable.days_selected);
                                            }

                                        }

                                    }
                                }
                                else
                                {
                                    Toast.makeText(Employee_Dashboard.this, R.string.No_Shifts_Found, Toast.LENGTH_SHORT).show();
                                }

                            }
                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });

                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(Employee_Dashboard.this, getResources().getString(R.string.NO_data_found), Toast.LENGTH_SHORT).show();
                progressDialog.dismiss();

            }
        });
    }

    public static void resetInstanceId() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    FirebaseInstanceId.getInstance().deleteInstanceId();
                    FirebaseInstanceId.getInstance().getInstanceId();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }
    private void turnGPSOn()
    {
        LocationManager locationManager = (LocationManager)this.getSystemService(Context.LOCATION_SERVICE);
        assert locationManager != null;
        GpsStatus = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        if(GpsStatus == true) {
        } else {
            Intent intent1 = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            startActivity(intent1);
        }
    }

    public void location_permission()
    {

        if (ContextCompat.checkSelfPermission(Employee_Dashboard.this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
        {

            if (ActivityCompat.shouldShowRequestPermissionRationale(Employee_Dashboard.this,
                    Manifest.permission.ACCESS_FINE_LOCATION))
            {
                ActivityCompat.requestPermissions(Employee_Dashboard.this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            }
            else
                {
                ActivityCompat.requestPermissions(Employee_Dashboard.this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
                }
        }
        else

            {
                update_active();
            }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults){
        switch (requestCode){
            case 1: {
                if (grantResults.length>0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    if (ContextCompat.checkSelfPermission(Employee_Dashboard.this,
                            Manifest.permission.ACCESS_FINE_LOCATION)==PackageManager.PERMISSION_GRANTED)
                    {

                        update_active();
                    }
                }else{
                    Toast.makeText(this, getResources().getString(R.string.Permission_Required), Toast.LENGTH_SHORT).show();
                }
                return;
            }
        }
    }

    public void get_currency()
    {


        FirebaseDatabase.getInstance().getReference("Currency")
                .child(manager_username).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists())
                {
                    current_currency = dataSnapshot.child("Currency").getValue().toString().trim();
                    SharedPreferences.Editor edit = sp.edit();
                    edit.putString("currency", current_currency);
                    edit.commit();

                }
                else
                {
                    SharedPreferences.Editor edit = sp.edit();
                    edit.putString("currency", "USD $");
                    edit.commit();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                progressDialog.dismiss();

            }
        });





    }
    private void punch_check()
    {
        DatabaseReference databaseReference =   FirebaseDatabase.getInstance().getReference("Attendance");
        Query query2 = databaseReference.child(manager_username).
                child(employee_username).orderByChild("Punched").equalTo("yes");
        query2.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists())
                {
                    for(DataSnapshot snapshot : dataSnapshot.getChildren())
                    {
                        String Punched =snapshot.child("Punched").getValue().toString().trim();
                        if(Punched.equals("yes"))
                        {
                            mark="yes";
                            boolean a;
                            punch.setBackgroundResource(R.drawable.buttondesign_red);
                            punch.setText(getResources().getString(R.string.Unpunch));
                            a=isMyServiceRunning(Tracking_service.class);
                            if(a==false)

                            {
                                resume_service();
                            }
                        }
                        else
                        {
                            mark="no";
                            punch.setText(getResources().getString(R.string.Punch));
                            punch.setBackgroundResource(R.drawable.buttondesign);
                            stop_tracking();
                            delete_data();
                        }

                    }

                    progressDialog.dismiss();
                }
                else {
                    mark="no";
                    punch.setText(getResources().getString(R.string.Punch));
                    punch.setBackgroundResource(R.drawable.buttondesign);
                    stop_tracking();
                    delete_data();
                    progressDialog.dismiss();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

                progressDialog.dismiss();
            }
        });

    }

    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }
    private  void resume_service()
    {
        Query query = FirebaseDatabase.getInstance().getReference("Tracking").orderByChild("employee_username").equalTo(employee_username);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists())
                {
                    for (DataSnapshot dataSnapshot1: dataSnapshot.getChildren())
                    {
                        String key = dataSnapshot1.child("key").getValue().toString();
                        intent_location = new Intent(Employee_Dashboard.this, Tracking_service.class);
                        intent_location.putExtra("employee_email", employee_email);
                        intent_location.putExtra("key", key);
                        startService(intent_location);
                    }
                }
                else
                    {
                        start_tracking();
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }
    @Override
    public void onBackPressed() {
        this.moveTaskToBack(true);
    }
    public void update_active()
    {
        LocationManager lm = (LocationManager)getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
        Geocoder geocoder = new Geocoder(getApplicationContext());
        for(String provider: lm.getAllProviders()) {
            @SuppressWarnings("ResourceType") Location location = lm.getLastKnownLocation(provider);
            if(location!=null) {
                try {
                    List<Address> addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
                    if(addresses != null && addresses.size() > 0) {
                        country_name = addresses.get(0).getCountryName();
                        city_name = addresses.get(0).getLocality();
                        break;
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        Date date = new Date();
        String current_date = DateFormat.format("dd-MM-yyyy", date.getTime()).toString();
//        Calendar c = Calendar.getInstance();
//        SimpleDateFormat df = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");// HH:mm:ss");
//        String today_date_time = df.format(c.getTime());
        HashMap<String,Object> map = new HashMap<>();
        map.put("last_active",current_date);
        map.put("country_name",country_name);
        map.put("city_name",city_name);

        Query q1 = FirebaseDatabase.getInstance().getReference("userinfo").orderByChild("username").equalTo(employee_username);
        q1.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists())
                {
                    for (DataSnapshot snapshot: dataSnapshot.getChildren())
                    {
                        snapshot.getRef().updateChildren(map);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }
    public void LoadBannerAdd()
    {
        AdRequest adRequest1 = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest1);
    }
    public void LoadInterstitialAdd()
    {

        AdRequest adRequest2 = new AdRequest.Builder().build();
        mInterstitialAd.setAdListener(new AdListener() {
            @Override
            public void onAdLoaded() {
                mInterstitialAd.show();
            }
        });
        mInterstitialAd.loadAd(adRequest2);
    }

    public void check_paid()
    {
        if(paid_type.equals("free"))
        {
            Toast.makeText(this, getResources().getString(R.string.Free_user), Toast.LENGTH_SHORT).show();

        }
        else if(paid_type.equals("paid"))
        {

        }
        else {

            Query q1 = FirebaseDatabase.getInstance().getReference("paid").orderByChild("username").equalTo(manager_username);
            q1.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            if (snapshot.child("type").getValue().toString().trim().equals("free")) {
                                SharedPreferences.Editor edit = sp.edit();
                                edit.putString("paid_type", snapshot.child("type").getValue().toString().trim());
                                edit.commit();
                                mAdView.setVisibility(View.GONE);
                            } else if (snapshot.child("type").getValue().toString().trim().equals("paid")) {
                                SharedPreferences.Editor edit = sp.edit();
                                edit.putString("paid_type", snapshot.child("type").getValue().toString().trim());
                                edit.putString("valid_till", snapshot.child("valid_till").getValue().toString().trim());
                                edit.commit();

                            } else if (snapshot.child("type").getValue().toString().trim().equals("unpaid")) {
                                SharedPreferences.Editor edit = sp.edit();
                                edit.putString("paid_type", snapshot.child("type").getValue().toString().trim());
                                edit.commit();

                                LoadBannerAdd();
                                LoadInterstitialAdd();

                            }
                        }
                    } else {

                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }


    }
    public void version_check()
    {
        HashMap<String, Object> map2 = new HashMap<>();
        map2.put("email", employee_email);
        map2.put("version", "2.0");
        FirebaseDatabase.getInstance().getReference().child("version").push().setValue(map2)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {

                        if(task.isSuccessful())
                        {
                        }
                        else
                        {
                        }

                    }
                });
    }


    private void sendNotification(String des) {


        mRequestQue = Volley.newRequestQueue(this);
        JSONObject json = new JSONObject();
        try {

            json.put("to", "/topics/" +manager_username );
            // json.put("to", token);
            json.put("content_available", true);

            JSONObject notificationObj = new JSONObject();
            notificationObj.put("title",getResources().getString(R.string.Attendance));
          //  notificationObj.put("body", employee_name+ "Unmarked his Attendance at: "+current_time_unpunch);
            notificationObj.put("body", des);
            notificationObj.put("click_action","Attendance");
            JSONObject extraData = new JSONObject();
            extraData.put("sender_username",employee_email);
            extraData.put("target_email",manager_username);

            json.put("notification",notificationObj);
            json.put("data",extraData);


            JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, URL,
                    json,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {

                            Log.d("MUR", "onResponse: ");
                        }
                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Log.d("MUR", "onError: "+error.networkResponse);
                }
            }
            ){
                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                    Map<String,String> header = new HashMap<>();
                    header.put("content-type","application/json");
                    //         header.put("authorization","key=AAAA6f0HMNg:APA91bFdYWHj6IemP5ZqmX3V6irrhL_YOOx1RD_jUVPOQ9exjFfM0ZajH5IoEGrsCEKwuF6l5dSfWLAmnZTq1MyRr6bl2y9ydb49UPQNcpcs5aN1wBlrDnTz0BecsjOdGZgOloZkvab_");
                    header.put("authorization","key="+getResources().getString(R.string.firebase_notification_key));
                    return header;
                }
            };
            mRequestQue.add(request);
        }
        catch (JSONException e)

        {
            e.printStackTrace();
        }
    }


}


