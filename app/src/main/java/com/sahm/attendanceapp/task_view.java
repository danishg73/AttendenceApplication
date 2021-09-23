package com.sahm.attendanceapp;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class task_view extends AppCompatActivity
{

    String usertype;
    SharedPreferences sp;
    String employee_email,email;
    ListView listBar;
    List<Task_list> array_task_list = new ArrayList<>();
    ProgressDialog progressDialog;
    task_list_adapter adapter_main;
    ImageView backarroow;

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(LocaleHelper.onAttach(base));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_view);


        sp=getSharedPreferences("login",MODE_PRIVATE);

        if( sp.contains("user_type")){

            String v=  sp.getString("user_type",usertype).trim();
            if (v.equals("manager"))
            {
                Intent intent = new Intent(task_view.this, LoginDashboardActivity.class);
                startActivity(intent);
            }
            else if (v.equals("employee")){
                employee_email  =  sp.getString("email",email).trim();

            }
            else{
                Intent intent = new Intent(task_view.this, LoginDashboardActivity.class);
                startActivity(intent);
            }
        }
        listBar = findViewById(R.id.listview);
        backarroow = findViewById(R.id.backarrow);
        checkPermission();

        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle(getResources().getString(R.string.Fetching_Data));
//        fetch_data();

        listBar.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                //like this:

                Intent intent = new Intent(task_view.this, Task_complete_employee.class);
                intent.putExtra("task_name",array_task_list.get(position).task_name);
                intent.putExtra("task_key",array_task_list.get(position).task_key);
                startActivity(intent);


            }
        });

        backarroow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                task_view.super.onBackPressed();
            }
        });




    }
    public void fetch_data()
    {
        progressDialog.show();
        Query query = FirebaseDatabase.getInstance().getReference("Employee_Task")
                .orderByChild("Employee_Email").equalTo(employee_email);

        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {

                        if(snapshot.child("Status").getValue().toString().equals("Not Completed"))
                        {
                            Task_list task_list = new Task_list();
                            task_list.task_name = snapshot.child("Task_Name").getValue().toString();
                            task_list.task_description = snapshot.child("Task_Description").getValue().toString();
                            task_list.status =snapshot.child("Status").getValue().toString();
                            task_list.manager_email =snapshot.child("Manager").getValue().toString();
                            task_list.file_url =snapshot.child("File_url").getValue().toString();
                            task_list.file_name =snapshot.child("File_name").getValue().toString();
                            task_list.expiray_date =snapshot.child("Expiry_Date").getValue().toString();
                            task_list.employee_email =snapshot.child("Employee_Email").getValue().toString();
                            task_list.task_key =snapshot.child("key").getValue().toString();
                            array_task_list.add(task_list);
                            task_list= null;
                        }


                    }
                     adapter_main = new task_list_adapter(array_task_list,getBaseContext(),task_view.this);
                    listBar.setAdapter(adapter_main);
                    adapter_main.notifyDataSetChanged();
                    progressDialog.dismiss();

                }
                else
                {
                    Toast.makeText(task_view.this, getResources().getString(R.string.NO_data_found), Toast.LENGTH_SHORT).show();
                    progressDialog.dismiss();
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError)
            {
            }
        });

    }


    public void checkPermission() {
        int result = ContextCompat.checkSelfPermission(task_view.this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (result == PackageManager.PERMISSION_GRANTED) {

        } else {
            requestPermission();
        }
    }
    private void requestPermission() {


        ActivityCompat.requestPermissions(task_view.this, new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
//        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case 1:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                } else {
                    Toast.makeText(this, getResources().getString(R.string.Permission_Required), Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }

    public void onResume() {
        super.onResume();
        array_task_list.clear();
        listBar.setAdapter(null);
        fetch_data();

    }


}
