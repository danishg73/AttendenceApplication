package com.sahm.attendanceapp;

import android.Manifest;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;
import androidx.work.WorkRequest;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.google.android.gms.tasks.OnCompleteListener;
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
import com.sahm.attendanceapp.Model.AddEmployee;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;

import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

public class Manager_Dashboard extends AppCompatActivity{

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(LocaleHelper.onAttach(base));
    }

    String usertype, Manager_email, manager_username, current_currency, curr,paid_type;
    SharedPreferences sp;
    TextView profile_name, selectemployees, punched, notpunched, currency;
    EditText empname, emphone, empid, empdes, salary, uname;
    String email, phone, password, shift, name, salary_duration, username, m_username, m_name, manager_name;
    String manager, get_salary;
    Spinner spinner, spinner_salary;
    LinearLayout Add_emloyee_layout, ll_allemployee, ll_punched, ll_notpunched, office_location,currency_select,premium;
    ArrayList list_time, list_salary;
    ProgressDialog progressDialog, pd1;
    String press, country_name, city_name;
    Button cancel, empadded;
    ImageView faq;
    DatabaseReference databaseReference;
    ArrayAdapter<String> adapter;
    Dialog customDialog;
    ListView listBar;
    List<Em_list> array_employeelist = new ArrayList<>();
    List<Em_list> array_employeelist_punched = new ArrayList<>();
    List<Em_list> array_employeelist_unpunched = new ArrayList<>();
    Em_list_adapter adapter_main, adapter_notpunched;
    private AdView mAdView;
    private final int REQUEST_LOCATION_PERMISSION = 1;
    private InterstitialAd mInterstitialAd;
    WorkRequest uploadWorkRequest;
    long back_pressed;


    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sp = getSharedPreferences("login", MODE_PRIVATE);

        if (sp.contains("user_type")) {

            String v = sp.getString("user_type", usertype).trim();
            if (v.equals("manager")) {

                Manager_email = sp.getString("email", manager).trim();
                manager_username = sp.getString("username", m_username).trim();
                manager_name = sp.getString("name", m_name).trim();
                current_currency = sp.getString("currency", curr).trim();
                paid_type = sp.getString("paid_type", "").trim();


            } else if (v.equals("employee")) {
                Intent intent = new Intent(Manager_Dashboard.this, Employee_Dashboard.class);
                startActivity(intent);
            } else {
                Intent intent = new Intent(Manager_Dashboard.this, LoginDashboardActivity.class);
                startActivity(intent);
            }
        }

        setContentView(R.layout.activity_manager_dashboard);
        setTitle(R.string.Manager_Dasboard);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setItemIconTintList(null);

        selectemployees = findViewById(R.id.employees);
        punched = findViewById(R.id.punched);
        notpunched = findViewById(R.id.notpunched);
        listBar = findViewById(R.id.listview);
        Add_emloyee_layout = findViewById(R.id.add_employee_layout);
        ll_allemployee = findViewById(R.id.ll_allemployee);
        ll_punched = findViewById(R.id.ll_punched);
        ll_notpunched = findViewById(R.id.ll_notpunched);
        faq = findViewById(R.id.faq);
        office_location = findViewById(R.id.office_location);
        currency_select = findViewById(R.id.curr);
        premium = findViewById(R.id.premium);
        View headerView = navigationView.getHeaderView(0);
        profile_name = headerView.findViewById(R.id.profile_name);

        progressDialog = new ProgressDialog(this);
        pd1 = new ProgressDialog(this);
        pd1.setMessage(getResources().getString(R.string.Fetching_Data));
        pd1.setCancelable(false);
        pd1.show();

        profile_name.setText(manager_name);




        MobileAds.initialize(this, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {}
        });
        MobileAds.initialize(this,getResources().getString(R.string.adds_key));
        mInterstitialAd = new InterstitialAd(this);
        mInterstitialAd.setAdUnitId(getResources().getString(R.string.interstitial_full_screen));






        list_time = new ArrayList<String>();
        list_time.clear();
        list_salary = new ArrayList<String>();
        list_salary.clear();
        press = "employee";
        databaseReference = FirebaseDatabase.getInstance().getReference("userinfo");
        Intent intent = new Intent(this, MyService.class);
        intent.putExtra("manager_email", Manager_email);
        startService(intent);

        list_salary.add("Per Hour");
        list_salary.add("Per Day");
        list_salary.add("Per Week");
        list_salary.add("Per Month");

        FirebaseMessaging.getInstance().subscribeToTopic("Managers");
        FirebaseMessaging.getInstance().subscribeToTopic(manager_username);
        FirebaseMessaging.getInstance().subscribeToTopic("All");
        adapter = new ArrayAdapter<>(this, R.layout.spinner_item, list_time);
        ArrayAdapter<String> adapter2 = new ArrayAdapter<>(this, R.layout.spinner_item, list_salary);
        mAdView = findViewById(R.id.adView);


        //------------------------------------------------------
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {

                // Handle navigation view item clicks here.
                int id = menuItem.getItemId();

                if (id == R.id.nav_user) {
                } else if (id == R.id.nav_pdf_report) {
                    Intent intent = new Intent(Manager_Dashboard.this, pdf_report.class);
                    startActivity(intent);
                } else if (id == R.id.nav_timings) {

                    Intent intent = new Intent(Manager_Dashboard.this, WorkTimingsActivity.class);
                    startActivity(intent);
                } else if (id == R.id.nav_holidays) {
                    Intent intent = new Intent(Manager_Dashboard.this, HolidaysActivity.class);
                    startActivity(intent);
                } else if (id == R.id.nav_setting) {
                    Intent intent = new Intent(Manager_Dashboard.this, SettingsActivity.class);
                    startActivity(intent);
                }  else if (id == R.id.nav_work_report) {

                } else if (id == R.id.nav_currency) {
                    Intent intent = new Intent(Manager_Dashboard.this, Currency.class);
                    startActivity(intent);
                } else if (id == R.id.nav_share) {

                    Intent i=new Intent(Intent.ACTION_SEND);
                    i.setType("text/plain");
                    i.putExtra(Intent.EXTRA_SUBJECT,"SAHM TIME");
                    i.putExtra(Intent.EXTRA_TEXT,"Get SHAM TIME App by clicking the link given below: \nhttps://pla");
                    startActivity(getIntent().createChooser(i, getResources().getString(R.string.Share_with)));

                } else if (id == R.id.nav_logout)
                {
                    FirebaseMessaging.getInstance().unsubscribeFromTopic("Managers");
                    FirebaseMessaging.getInstance().unsubscribeFromTopic("All");
                    WorkManager.getInstance(Manager_Dashboard.this).cancelAllWorkByTag("not_punch_alert");
                    resetInstanceId();
                    stopService(new Intent(Manager_Dashboard.this, MyService.class));
                    SharedPreferences.Editor e = sp.edit();
                    e.clear();
                    e.commit();
                    Intent intent = new Intent(Manager_Dashboard.this, LoginDashboardActivity.class);
                    startActivity(intent);
                    finish();

                }
                DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
                drawer.closeDrawer(GravityCompat.START);

                return false;
            }
        });

        //-----------------------------------------------------------
        version_check();
        check_paid();
        getting_shifts();
        getdata();
        requestLocationPermission();
        WorkManager.getInstance(Manager_Dashboard.this).cancelAllWorkByTag("not_punch_alert");
        not_punch_Alert();

        faq.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(Manager_Dashboard.this, "FAQ's", Toast.LENGTH_SHORT).show();
            }
        });
        office_location.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent1 = new Intent(Manager_Dashboard.this, office_location.class);
                startActivity(intent1);
            }
        });

        currency_select.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Manager_Dashboard.this, Currency.class);
                startActivity(intent);
            }
        });
        premium.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Manager_Dashboard.this, Inapp_purchase.class);
                startActivity(intent);
            }
        });

        ll_punched.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                press = "punched";
                adapter_main = new Em_list_adapter(array_employeelist_punched, getBaseContext(), Manager_Dashboard.this, "punch", manager_username);
                listBar.setAdapter(null);
                listBar.setAdapter(adapter_main);
                adapter_main.notifyDataSetChanged();
                punched.setTextColor(getResources().getColor(R.color.white));
                ll_punched.setBackgroundResource(R.drawable.buttondesign);
                ll_allemployee.setBackgroundResource(0);
                ll_notpunched.setBackgroundResource(0);
                selectemployees.setTextColor(getResources().getColor(android.R.color.black));
                notpunched.setTextColor(getResources().getColor(android.R.color.black));
            }
        });
        ll_notpunched.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                press = "not_punched";
                adapter_notpunched = new Em_list_adapter(array_employeelist_unpunched, getBaseContext(), Manager_Dashboard.this, "unpunch", manager_username);
                listBar.setAdapter(null);
                listBar.setAdapter(adapter_notpunched);
                adapter_notpunched.notifyDataSetChanged();
                ll_notpunched.setBackgroundResource(R.drawable.buttondesign);
                ll_allemployee.setBackgroundResource(0);
                ll_punched.setBackgroundResource(0);
                notpunched.setTextColor(getResources().getColor(R.color.white));
                selectemployees.setTextColor(getResources().getColor(android.R.color.black));
                punched.setTextColor(getResources().getColor(android.R.color.black));
            }
        });
        ll_allemployee.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                press = "employee";
                adapter_main = new Em_list_adapter(array_employeelist, getBaseContext(), Manager_Dashboard.this, "all", manager_username);
                listBar.setAdapter(null);
                listBar.setAdapter(adapter_main);
                adapter_main.notifyDataSetChanged();
                ll_allemployee.setBackgroundResource(R.drawable.buttondesign);
                ll_notpunched.setBackgroundResource(0);
                ll_punched.setBackgroundResource(0);
                selectemployees.setTextColor(getResources().getColor(R.color.white));
                notpunched.setTextColor(getResources().getColor(android.R.color.black));
                punched.setTextColor(getResources().getColor(android.R.color.black));
            }
        });
        listBar.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {


                //like this:
                if (press.equals("employee")) {
                    Intent intent = new Intent(Manager_Dashboard.this, EmployeeProfileActivity.class);
                    intent.putExtra("target_email", array_employeelist.get(position).email);
                    intent.putExtra("target_name", array_employeelist.get(position).name);
                    intent.putExtra("target_username", array_employeelist.get(position).username);
                    intent.putExtra("currency", current_currency);
                    startActivity(intent);
                } else if (press.equals("not_punched")) {
                    Intent intent = new Intent(Manager_Dashboard.this, EmployeeProfileActivity.class);
                    intent.putExtra("target_email", array_employeelist_unpunched.get(position).email);
                    intent.putExtra("target_name", array_employeelist_unpunched.get(position).name);
                    intent.putExtra("target_username", array_employeelist_unpunched.get(position).username);
                    intent.putExtra("currency", current_currency);
                    startActivity(intent);
                } else if (press.equals("punched")) {
                    Intent intent = new Intent(Manager_Dashboard.this, EmployeeProfileActivity.class);
//                   Intent intent = new Intent(FirstScreen.this, Employee_task.class);
//                    Intent intent = new Intent(FirstScreen.this, Messenger.class);
                    intent.putExtra("target_email", array_employeelist_punched.get(position).email);
                    intent.putExtra("target_name", array_employeelist_punched.get(position).name);
                    intent.putExtra("target_username", array_employeelist_punched.get(position).username);
                    intent.putExtra("currency", current_currency);
                    startActivity(intent);
                }

            }
        });

        Add_emloyee_layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                customDialog = new Dialog(Manager_Dashboard.this);
                // customDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
                customDialog.setContentView(R.layout.custom_employee);
                int width = (int) (getResources().getDisplayMetrics().widthPixels * 0.90);
//                int height = (int) (getResources().getDisplayMetrics().heightPixels * 0.57);
                customDialog.getWindow().setLayout(width, ViewGroup.LayoutParams.WRAP_CONTENT);
                empname = customDialog.findViewById(R.id.employeeusername);
                spinner = customDialog.findViewById(R.id.spinner1);
                spinner_salary = customDialog.findViewById(R.id.spinnersalary);
                emphone = customDialog.findViewById(R.id.employeephonenumber);
                empid = customDialog.findViewById(R.id.employeeid);
                uname = customDialog.findViewById(R.id.username);
                empdes = customDialog.findViewById(R.id.employeepassword);
                salary = customDialog.findViewById(R.id.salary);
                empadded = customDialog.findViewById(R.id.employeeadded);
                currency = customDialog.findViewById(R.id.currency);
                cancel = customDialog.findViewById(R.id.cancel);
                spinner.setAdapter(null);
                spinner.setAdapter(adapter);
                spinner_salary.setAdapter(adapter2);
                currency.setText(current_currency);
                cancel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        customDialog.dismiss();
                    }
                });

                empadded.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        email = empid.getText().toString();
                        name = empname.getText().toString();
                        username = uname.getText().toString();
                        phone = emphone.getText().toString();
                        password = empdes.getText().toString();
                        get_salary = salary.getText().toString();
                        shift = spinner.getSelectedItem().toString();
                        salary_duration = spinner_salary.getSelectedItem().toString();
                        if (shift.equals("No shift created yet")) {
                            Toast.makeText(Manager_Dashboard.this, R.string.Create_work_timings_first, Toast.LENGTH_SHORT).show();

                        } else {

                            if (TextUtils.isEmpty(username) && TextUtils.isEmpty(name) && TextUtils.isEmpty(email) && TextUtils.isEmpty(phone) && TextUtils.isEmpty(password) && TextUtils.isEmpty(get_salary)) {
                                Toast.makeText(Manager_Dashboard.this, getResources().getString(R.string.All_fields_are_required), Toast.LENGTH_SHORT).show();
                            } else {
                                progressDialog.setTitle(getResources().getString(R.string.Please_wait));
                                progressDialog.setMessage(getResources().getString(R.string.Registring_Employee));
                                progressDialog.show();
                                check_username();
                            }
                        }
                    }
                });
                customDialog.setCancelable(true);
                customDialog.show();
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

//    @Override
//    public void onBackPressed()
//    {
//        this.moveTaskToBack(true);
//    }



    @Override
    public void onBackPressed() {
        if (back_pressed + 1000 > System.currentTimeMillis()){
            super.onBackPressed();
        }
        else{
            Toast.makeText(getBaseContext(),
                    "Press once again to exit!", Toast.LENGTH_SHORT)
                    .show();
        }
        back_pressed = System.currentTimeMillis();
    }


    void getting_shifts() {
        Query query1 = FirebaseDatabase.getInstance().getReference("Timings").orderByChild("manager").equalTo(Manager_email);

        query1.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                list_time.clear();
                if (dataSnapshot.exists()) {
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        String a = snapshot.child("shiftname").getValue().toString();
                        list_time.add(a);
                    }
                } else {
                    list_time.add("No shift created yet");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    public void getdata()
    {

        listBar.setAdapter(null);
        Query query = FirebaseDatabase.getInstance().getReference("Employee_data")
                .orderByChild("manager").equalTo(Manager_email);
//        Query query2 = FirebaseDatabase.getInstance().getReference("Attendance").child(manager_username);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    array_employeelist.clear();
                    array_employeelist_punched.clear();
                    array_employeelist_unpunched.clear();
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        Em_list em_list = new Em_list();
                        em_list.name = snapshot.child("name").getValue().toString();
                        em_list.email = snapshot.child("email").getValue().toString();
                        em_list.phone = snapshot.child("phone").getValue().toString();
                        em_list.username = snapshot.child("username").getValue().toString().trim();
                        array_employeelist.add(em_list);
                        em_list = null;

                        if (snapshot.child("Punched").getValue().toString().equals("yes")) {
                            Em_list em_list_punched = new Em_list();
                            em_list_punched.name = snapshot.child("name").getValue().toString();
                            em_list_punched.email = snapshot.child("email").getValue().toString();
                            em_list_punched.phone = snapshot.child("phone").getValue().toString();
                            em_list_punched.username = snapshot.child("username").getValue().toString().trim();
                            array_employeelist_punched.add(em_list_punched);
                            em_list_punched = null;
                        }
                        if (snapshot.child("Punched").getValue().toString().equals("no")) {
                            Em_list em_list_unpunched = new Em_list();
                            em_list_unpunched.name = snapshot.child("name").getValue().toString();
                            em_list_unpunched.email = snapshot.child("email").getValue().toString();
                            em_list_unpunched.phone = snapshot.child("phone").getValue().toString();
                            em_list_unpunched.username = snapshot.child("username").getValue().toString().trim();
                            array_employeelist_unpunched.add(em_list_unpunched);
                            em_list_unpunched = null;
                        }
                    }
                    if (press.equals("employee")) {

                        adapter_main = new Em_list_adapter(array_employeelist, getBaseContext(), Manager_Dashboard.this, "all", manager_username);
                        listBar.setAdapter(null);
                        listBar.setAdapter(adapter_main);
                        adapter_main.notifyDataSetChanged();

                    } else if (press.equals("not_punched")) {
                        adapter_notpunched = new Em_list_adapter(array_employeelist_unpunched, getBaseContext(), Manager_Dashboard.this, "unpunch", manager_username);
                        listBar.setAdapter(null);
                        listBar.setAdapter(adapter_notpunched);
                        adapter_notpunched.notifyDataSetChanged();

                    } else if (press.equals("punched")) {
                        adapter_main = new Em_list_adapter(array_employeelist_punched, getBaseContext(), Manager_Dashboard.this, "punch", manager_username);
                        listBar.setAdapter(null);
                        listBar.setAdapter(adapter_main);
                        adapter_main.notifyDataSetChanged();

                    }
                    pd1.dismiss();
                } else {
                    Toast.makeText(Manager_Dashboard.this, R.string.No_Employee_Found, Toast.LENGTH_SHORT).show();
                    pd1.dismiss();
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


    }

    private void check_username() {

        Query query1 = FirebaseDatabase.getInstance().getReference("userinfo")
                .orderByChild("username").equalTo(username);
        query1.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    Toast.makeText(Manager_Dashboard.this, getResources().getString(R.string.Username_already), Toast.LENGTH_SHORT).show();
                } else {
                    insert_database();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void insert_database() {

        String key = databaseReference.push().getKey();
        AddEmployee addEmployees = new AddEmployee(key, username, phone, email, password, "employee", "marked", name, shift);
        databaseReference.child(key).setValue(addEmployees, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {

                HashMap<String, Object> map = new HashMap<>();
                map.put("email", email);
                map.put("name", name);
                map.put("username", username);
                map.put("phone", phone);
                map.put("Salary", get_salary);
                map.put("duration", salary_duration);
                map.put("Salary", get_salary);
                map.put("shift", shift);
                map.put("Punched", "no");
                map.put("manager", Manager_email);
                map.put("manager_username", manager_username);
                FirebaseDatabase.getInstance().getReference().child("Employee_data").push()
                        .setValue(map)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                Toast.makeText(Manager_Dashboard.this, getResources().getString(R.string.Employee_Added), Toast.LENGTH_SHORT).show();
                                customDialog.dismiss();
                                make_lastactive();
                            }
                        });
                empid.setText("");
                empdes.setText("");
                emphone.setText("");
                empname.setText("");
                get_salary = "";
                progressDialog.dismiss();

            }
        });
        customDialog.dismiss();
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

    public void update_active()
    {

        LocationManager lm = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
        Geocoder geocoder = new Geocoder(getApplicationContext());
        for (String provider : lm.getAllProviders()) {
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
        HashMap<String,Object> map = new HashMap<>();
        map.put("last_active",current_date);
        map.put("country_name",country_name);
        map.put("city_name",city_name);

        Query q1 = FirebaseDatabase.getInstance().getReference("userinfo").orderByChild("username").equalTo(manager_username);
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
    public void make_lastactive()
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

        HashMap<String,Object> map1 = new HashMap<>();
        map1.put("last_active","no");
        map1.put("country_name",country_name);
        map1.put("city_name",city_name);

        Query q1 = FirebaseDatabase.getInstance().getReference("userinfo").orderByChild("username").equalTo(username);
        q1.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists())
                {
                    for (DataSnapshot snapshot: dataSnapshot.getChildren())
                    {
                        snapshot.getRef().updateChildren(map1);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults)
    {

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        // Forward results to EasyPermissions
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    @AfterPermissionGranted(REQUEST_LOCATION_PERMISSION)
    public void requestLocationPermission()
    {
        String[] perms = {Manifest.permission.ACCESS_FINE_LOCATION};
        if(EasyPermissions.hasPermissions(this, perms)) {

            update_active();
        }
        else {
            EasyPermissions.requestPermissions(this, getResources().getString(R.string.please_grant_location), REQUEST_LOCATION_PERMISSION, perms);
        }
    }
    public void LoadBannerAdd()
    {
        mAdView.setVisibility(View.VISIBLE);
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
    public void not_punch_Alert()
    {

        uploadWorkRequest = new PeriodicWorkRequest.Builder(NotificationWorker.class,15, TimeUnit.MINUTES)
                .addTag("not_punch_alert")
                .build();

        WorkManager.getInstance(getApplicationContext()).enqueue(uploadWorkRequest);



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
        else
        {
            LoadBannerAdd();
            LoadInterstitialAdd();
        }
        Query q1 = FirebaseDatabase.getInstance().getReference("paid").orderByChild("username").equalTo(manager_username);
        q1.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            {
                if (dataSnapshot.exists())
                {
                    for (DataSnapshot snapshot: dataSnapshot.getChildren())
                    {
                        if(snapshot.child("type").getValue().toString().trim().equals("free"))
                        {
                            SharedPreferences.Editor edit = sp.edit();
                            edit.putString("paid_type", snapshot.child("type").getValue().toString().trim());
                            edit.commit();
                            mAdView.setVisibility(View.GONE);
                        }
                        else if(snapshot.child("type").getValue().toString().trim().equals("paid"))
                        {
                            SharedPreferences.Editor edit = sp.edit();
                            edit.putString("paid_type", snapshot.child("type").getValue().toString().trim());
                            edit.putString("valid_till", snapshot.child("valid_till").getValue().toString().trim());
                            edit.commit();

                        }
                        else if(snapshot.child("type").getValue().toString().trim().equals("unpaid"))
                        {
                            SharedPreferences.Editor edit = sp.edit();
                            edit.putString("paid_type", snapshot.child("type").getValue().toString().trim());
                            edit.commit();

                            LoadBannerAdd();
                            LoadInterstitialAdd();

                        }
                    }
                }
                else
                {

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


    }
    public void version_check()
    {
        HashMap<String, Object> map2 = new HashMap<>();
        map2.put("email", Manager_email);
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


}









