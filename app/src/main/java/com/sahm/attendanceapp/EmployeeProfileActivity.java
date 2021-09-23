package com.sahm.attendanceapp;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class EmployeeProfileActivity extends AppCompatActivity {
    private static int RESULT_LOAD_IMAGE = 1;
    CircleImageView circleImageView;
    ImageView imageView, whatsapp, smsemployee, backarrow,edit_info,edit_data;
    LinearLayout ll_Dm,track,salary_cal,edit_shift,edit_salary;
    Button show_task,show_report,cancel,submit,update;
    TextView name,tv_shift,tv_salaryperiod,tv_salaryamount;
    TextView tv_phone;
    String namee,email,updated_shift,updated_duration,updated_salary;
    EditText editText_salary,fullname,phonenumber;
    String name_update,phone_update;
    ProgressDialog progressDialog,pd1;
    String Manager_email,usertype,Manager_name,employee_email,target_name,manager_username,employee_username,target_username,paid_type="";
    DatabaseReference databaseReference;
    String shift,salary,phone,salary_duration,currency;
    SharedPreferences sp;
    String punch;
    String type;
    private AdView mAdView;
    Dialog customDialog,customDialog2,customDialog3;
    ArrayList list_time, list_salary;
    ArrayAdapter<String> adapter,adapter2;
    Spinner spinner, spinner_salary;
    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(LocaleHelper.onAttach(base));
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sp=getSharedPreferences("login",MODE_PRIVATE);

        setContentView(R.layout.activity_employee_profile);
        ll_Dm = findViewById(R.id.ll_DM);
        salary_cal = findViewById(R.id.salary_cal);
        track = findViewById(R.id.track);
        show_task = findViewById(R.id.show_task);
        show_report = findViewById(R.id.show_reports);
        edit_shift = findViewById(R.id.edit_shift);
        edit_salary = findViewById(R.id.edit_salary);
        edit_info = findViewById(R.id.edit_info);
        edit_data = findViewById(R.id.edit_data);

        if( sp.contains("user_type")){

            String v=  sp.getString("user_type",usertype).trim();
            if (v.equals("manager"))
            {
                Manager_email  =  sp.getString("email",email).trim();
                Manager_name  =  sp.getString("name",namee).trim();
                manager_username  =  sp.getString("username",namee).trim();
                paid_type = sp.getString("paid_type", "").trim();
                salary_cal.setEnabled(true);
                type="manager";
            }
            else if (v.equals("employee"))
            {
                paid_type = sp.getString("paid_type", "").trim();
                type="employee";
               ll_Dm.setVisibility(View.GONE);
               show_task.setVisibility(View.GONE);
               show_report.setVisibility(View.GONE);
               track.setVisibility(View.GONE);
               edit_shift.setVisibility(View.GONE);
               edit_salary.setVisibility(View.GONE);
               edit_info.setVisibility(View.GONE);
               edit_data.setVisibility(View.GONE);
               salary_cal.setEnabled(false);
            }
            else{
                Intent intent = new Intent(EmployeeProfileActivity.this, LoginDashboardActivity.class);
                startActivity(intent);
            }
        }
        databaseReference = FirebaseDatabase.getInstance().getReference("userinfo");
        imageView = findViewById(R.id.callemployee);
        smsemployee = findViewById(R.id.smsemployeeprofile);
        backarrow = findViewById(R.id.backarrow);
        name = findViewById(R.id.name);
        tv_phone = findViewById(R.id.phone);
        tv_salaryamount = findViewById(R.id.salary_amount);
        tv_salaryperiod = findViewById(R.id.salary_period);
        tv_shift = findViewById(R.id.shift);
        circleImageView = findViewById(R.id.employeeprofileimage);
        whatsapp = findViewById(R.id.whatsappemployeeprofile);
        employee_email = getIntent().getStringExtra("target_email");
        target_username = getIntent().getStringExtra("target_username");
        currency = getIntent().getStringExtra("currency");
        mAdView = findViewById(R.id.adView);
        progressDialog = new ProgressDialog(this);
        pd1 = new ProgressDialog(this);
        progressDialog.setMessage(getResources().getString(R.string.Fetching_Data));
        progressDialog.setCancelable(false);
        pd1.setMessage(getResources().getString(R.string.Loading));
        pd1.setCancelable(false);
        list_time = new ArrayList<String>();
        list_time.clear();
        list_salary = new ArrayList<String>();
        list_salary.clear();
        adapter = new ArrayAdapter<>(this, R.layout.spinner_item, list_time);list_salary.add("Per Hour");
        list_salary.add("Per Day");
        list_salary.add("Per Week");
        list_salary.add("Per Month");
        adapter2 = new ArrayAdapter<>(this, R.layout.spinner_item, list_salary);
        get_Employe_info();
        getting_shifts();

        if(paid_type.equals("free"))
        {

        }
        else
        {
            LoadBannerAdd();
        }


        edit_data.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                show_dialogue();
            }
        });



        track.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                if(punch.equals("no"))
                {
                    Toast.makeText(EmployeeProfileActivity.this, R.string.Employee_is_not_present, Toast.LENGTH_SHORT).show();
                }
                else
                    {
                    Intent intent = new Intent(EmployeeProfileActivity.this, MapsActivity.class);
                    intent.putExtra("employee_username",employee_username);
                    intent.putExtra("employee_email",employee_email);
                    startActivity(intent);
                    }
            }
        });

        edit_shift.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {


                customDialog = new Dialog(EmployeeProfileActivity.this);
                // customDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
                customDialog.setContentView(R.layout.dialogue_shift);
                int width = (int) (getResources().getDisplayMetrics().widthPixels * 0.90);
//                int height = (int) (getResources().getDisplayMetrics().heightPixels * 0.57);
                customDialog.getWindow().setLayout(width, ViewGroup.LayoutParams.WRAP_CONTENT);
                spinner = customDialog.findViewById(R.id.spinner_shift);
                cancel = customDialog.findViewById(R.id.cancel);
                submit = customDialog.findViewById(R.id.submit);
                spinner.setAdapter(null);
                spinner.setAdapter(adapter);
                int spinnerPosition = adapter.getPosition(shift);
                spinner.setSelection(spinnerPosition);
                submit.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view)
                    {
                        pd1.show();
                        updated_shift = spinner.getSelectedItem().toString();
                        Query q1 = FirebaseDatabase.getInstance().getReference("Employee_data").orderByChild("email").equalTo(employee_email);
                        q1.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                if (dataSnapshot.exists()) {
                                    for (DataSnapshot snapshot : dataSnapshot.getChildren())
                                    {
                                        HashMap<String,Object> map1 = new HashMap<>();
                                        map1.put("shift",updated_shift);
                                        snapshot.getRef().updateChildren(map1);
                                        Toast.makeText(EmployeeProfileActivity.this, getResources().getString(R.string.Updated), Toast.LENGTH_SHORT).show();
                                        pd1.dismiss();
                                        customDialog.dismiss();
                                    }

                                }
                                else
                                {
                                    pd1.dismiss();

                                }
                            }
                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError)
                            {
                                pd1.dismiss();
                            }
                        });
                    }
                });
                cancel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        customDialog.dismiss();
                    }
                });




                customDialog.setCancelable(true);
                customDialog.show();

            }
        });

        edit_salary.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {


                customDialog2 = new Dialog(EmployeeProfileActivity.this);
                // customDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
                customDialog2.setContentView(R.layout.dialogue_editsalary);
                int width = (int) (getResources().getDisplayMetrics().widthPixels * 0.99);
//                int height = (int) (getResources().getDisplayMetrics().heightPixels * 0.57);
                customDialog2.getWindow().setLayout(width, ViewGroup.LayoutParams.WRAP_CONTENT);
                spinner_salary = customDialog2.findViewById(R.id.spinner_salary);
                editText_salary = customDialog2.findViewById(R.id.salary);
                cancel = customDialog2.findViewById(R.id.cancel);
                submit = customDialog2.findViewById(R.id.submit);

                spinner_salary.setAdapter(null);
                spinner_salary.setAdapter(adapter2);
                int spinnerPosition = adapter2.getPosition(salary_duration);
                spinner_salary.setSelection(spinnerPosition);
                submit.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view)
                    {
                        pd1.show();
                        updated_duration = spinner_salary.getSelectedItem().toString();
                        updated_salary = editText_salary.getText().toString().trim();
                        if(TextUtils.isEmpty(updated_salary))
                        {
                            Toast.makeText(EmployeeProfileActivity.this, getResources().getString(R.string.All_fields_are_required), Toast.LENGTH_SHORT).show();
                        }
                        else
                        {

                            Query q1 = FirebaseDatabase.getInstance().getReference("Employee_data").orderByChild("email").equalTo(employee_email);
                            q1.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    if (dataSnapshot.exists()) {
                                        for (DataSnapshot snapshot : dataSnapshot.getChildren())
                                        {
                                            HashMap<String,Object> map1 = new HashMap<>();
                                            map1.put("Salary",updated_salary);
                                            map1.put("duration",updated_duration);
                                            snapshot.getRef().updateChildren(map1);
                                            Toast.makeText(EmployeeProfileActivity.this,  getResources().getString(R.string.Updated), Toast.LENGTH_SHORT).show();
                                            pd1.dismiss();
                                            customDialog2.dismiss();
                                        }

                                    }
                                    else
                                    {
                                        pd1.dismiss();

                                    }
                                }
                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError)
                                {
                                    pd1.dismiss();
                                }
                            });

                        }

                    }
                });
                cancel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        customDialog2.dismiss();
                    }
                });




                customDialog2.setCancelable(true);
                customDialog2.show();

            }
        });

        edit_info.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                Intent intent = new Intent(EmployeeProfileActivity.this, Edit_info.class);
                intent.putExtra("target_email", employee_email);
                intent.putExtra("target_name",target_name);
                intent.putExtra("target_username",target_username);
                startActivity(intent);

            }
        });


        ll_Dm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {

                Intent intent = new Intent(EmployeeProfileActivity.this, Messenger.class);
                intent.putExtra("target_email", employee_email);
                intent.putExtra("target_name",target_name);
                intent.putExtra("target_username",target_username);
                startActivity(intent);
            }
        });
        salary_cal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {

                Intent intent = new Intent(EmployeeProfileActivity.this, salary_calculate.class);
                intent.putExtra("target_email", employee_email);
                intent.putExtra("target_name",target_name);
                intent.putExtra("target_username",target_username);
                intent.putExtra("salary",salary);
                intent.putExtra("currency",currency);
                intent.putExtra("duration",salary_duration);
                startActivity(intent);
            }
        });
        show_task.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {

                Intent intent = new Intent(EmployeeProfileActivity.this, Employee_task.class);
                intent.putExtra("target_email", employee_email);
                intent.putExtra("target_name",target_name);
                intent.putExtra("target_phone",phone);
                intent.putExtra("target_username",employee_username);
                startActivity(intent);
            }
        });
        show_report.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                Intent intent = new Intent(EmployeeProfileActivity.this, Employee_reportview.class);
                intent.putExtra("target_email", employee_email);
                intent.putExtra("target_name",target_name);
                startActivity(intent);
            }
        });

        backarrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
              EmployeeProfileActivity.super.onBackPressed();
            }
        });
        smsemployee.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent sendIntent = new Intent(Intent.ACTION_VIEW);
                sendIntent.setData(Uri.parse("sms:"));
                startActivity(sendIntent);

            }
        });
        whatsapp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                PackageManager pm = getPackageManager();
                try {

                    Intent waIntent = new Intent(Intent.ACTION_SEND);
                    waIntent.setType("text/plain");
                    String text = "YOUR TEXT HERE";

                    PackageInfo info = pm.getPackageInfo("com.whatsapp", PackageManager.GET_META_DATA);
                    //Check if package exists or not. If not then code
                    //in catch block will be called
                    waIntent.setPackage("com.whatsapp");

                    waIntent.putExtra(Intent.EXTRA_TEXT, text);
                    startActivity(Intent.createChooser(waIntent,  getResources().getString(R.string.Share_with)));

                } catch (PackageManager.NameNotFoundException e) {
                    Toast.makeText(EmployeeProfileActivity.this,  getResources().getString(R.string.Whatsapp_not), Toast.LENGTH_SHORT)
                            .show();
                }

            }
        });
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                Intent callIntent = new Intent(Intent.ACTION_CALL);
//                callIntent.setData(Uri.parse("tel:"+"Your phone number"));
//
//                if (ActivityCompat.checkSelfPermission(EmployeeProfileActivity.this,
//                        Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
//                    return;
//                }
//                startActivity(callIntent);
                Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + Uri.encode("")));
                startActivity(intent);

            }
        });
        circleImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent,  getResources().getString(R.string.Select_picture)), RESULT_LOAD_IMAGE);

            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RESULT_LOAD_IMAGE && resultCode == RESULT_OK && data != null && data.getData() != null) {

            Uri uri = data.getData();

            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
                // Log.d(TAG, String.valueOf(bitmap));

                circleImageView.setImageBitmap(bitmap);
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(this,  getResources().getString(R.string.Error) + e, Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void get_Employe_info()
    {
        progressDialog.show();
        Query query2 = FirebaseDatabase.getInstance().getReference("Employee_data")
                .orderByChild("email").equalTo(employee_email);
        query2.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot snapshot : dataSnapshot.getChildren())
                    {
                        salary = snapshot.child("Salary").getValue().toString();
                        salary_duration = snapshot.child("duration").getValue().toString();
                        shift = snapshot.child("shift").getValue().toString();
                        phone = snapshot.child("phone").getValue().toString().trim();
                        target_name=snapshot.child("name").getValue().toString().trim();
                        employee_username= snapshot.child("username").getValue().toString().trim();
                        tv_phone.setText(phone);
                        tv_shift.setText(shift);
                        name.setText(target_name);
                        tv_salaryamount.setText(salary+" "+currency);
                        tv_salaryperiod.setText("Salary/"+salary_duration);
                        progressDialog.dismiss();
                    }
                    if(type.equals("manager"))
                    {
                        check_punch();
                    }
                }
                else
                {
                    progressDialog.dismiss();

                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError)
            {

                progressDialog.dismiss();

            }
        });
    }
    private void check_punch()
    {
        Query query = FirebaseDatabase.getInstance().getReference("Attendance")
                .child(manager_username).child(employee_username).orderByChild("Punched").equalTo("yes");
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            {
                if (dataSnapshot.exists())
                {
                    for (DataSnapshot snapshot: dataSnapshot.getChildren())
                    {
                        punch = snapshot.child("Punched").getValue().toString().trim();
                    }
                }
                else
                {
                    punch="no";
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

    public void show_dialogue()

    {

        customDialog3 = new Dialog(EmployeeProfileActivity.this);
        // customDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        customDialog3.setContentView(R.layout.dialogue_editinfo);
        int width = (int) (getResources().getDisplayMetrics().widthPixels * 0.90);
//                int height = (int) (getResources().getDisplayMetrics().heightPixels * 0.57);
        customDialog3.getWindow().setLayout(width, ViewGroup.LayoutParams.WRAP_CONTENT);
        update = customDialog3.findViewById(R.id.cirLoginButton);
        fullname = customDialog3.findViewById(R.id.fullname);
        phonenumber = customDialog3.findViewById(R.id.phone_number);
        phonenumber.setText(phone);
        fullname.setText(target_name);
        update.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                name_update="";
                phone_update="";
                pd1.show();
                name_update = fullname.getText().toString().trim();
                phone_update = phonenumber.getText().toString().trim();
                if(TextUtils.isEmpty(name_update) || TextUtils.isEmpty(phone_update))
                {
                    Toast.makeText(EmployeeProfileActivity.this, R.string.All_fields_are_required, Toast.LENGTH_SHORT).show();
                }
                else
                {
                    HashMap<String,Object> map1 = new HashMap<>();
                    map1.put("name",name_update);
                    map1.put("phone",phone_update);

                    Query q2 = FirebaseDatabase.getInstance().getReference("userinfo").orderByChild("email").equalTo(employee_email);

                    Query q1 = FirebaseDatabase.getInstance().getReference("Employee_data").orderByChild("email").equalTo(employee_email);
                    q1.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if (dataSnapshot.exists()) {
                                for (DataSnapshot snapshot : dataSnapshot.getChildren())
                                {

                                    snapshot.getRef().updateChildren(map1);

                                    q2.addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                            if (dataSnapshot.exists()) {
                                                for (DataSnapshot snapshot : dataSnapshot.getChildren())
                                                {

                                                    snapshot.getRef().updateChildren(map1);
                                                    pd1.dismiss();
                                                    customDialog3.dismiss();
                                                    Toast.makeText(EmployeeProfileActivity.this,  getResources().getString(R.string.Updated), Toast.LENGTH_SHORT).show();
                                                }

                                            }
                                            else
                                            {
                                                Toast.makeText(EmployeeProfileActivity.this,  getResources().getString(R.string.NO_data_found), Toast.LENGTH_SHORT).show();
                                                customDialog3.dismiss();
                                                pd1.dismiss();

                                            }
                                        }
                                        @Override
                                        public void onCancelled(@NonNull DatabaseError databaseError)
                                        {
                                            Toast.makeText(EmployeeProfileActivity.this,  getResources().getString(R.string.Error), Toast.LENGTH_SHORT).show();
                                            pd1.dismiss();
                                        }
                                    });




                                }

                            }
                            else
                            { Toast.makeText(EmployeeProfileActivity.this,  getResources().getString(R.string.NO_data_found), Toast.LENGTH_SHORT).show();
                                customDialog3.dismiss();
                                pd1.dismiss();

                            }
                        }
                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError)
                        {
                            pd1.dismiss();
                        }
                    });
                }


            }
        });





        customDialog3.setCancelable(true);
        customDialog3.show();


    }



}
