package com.sahm.attendanceapp;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class Employee_reportview extends AppCompatActivity {

    SharedPreferences sp;
    String Manager_email,usertype,manager,employee_email,target_name;
    ImageView backarrow;
    ProgressDialog progressDialog;
    ListView listBar;
    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(LocaleHelper.onAttach(base));
    }

    List<Task_list> array_report = new ArrayList<>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_employee_report);
        sp=getSharedPreferences("login",MODE_PRIVATE);

        if( sp.contains("user_type")){

            String v=  sp.getString("user_type",usertype).trim();
            if (v.equals("manager"))
            {

                Manager_email = sp.getString("email",manager).trim();
            }
            else if (v.equals("employee")){
                Intent intent = new Intent(Employee_reportview.this, LoginDashboardActivity.class);
                startActivity(intent);
            }
            else{
                Intent intent = new Intent(Employee_reportview.this, LoginDashboardActivity.class);
                startActivity(intent);
            }
        }


        listBar = findViewById(R.id.listview);
        backarrow=findViewById(R.id.backarrow);

        progressDialog = new ProgressDialog(this);
        employee_email = getIntent().getStringExtra("target_email");
        progressDialog.setMessage( getResources().getString(R.string.Fetching_Data));
        progressDialog.setCancelable(false);
        progressDialog.show();



        get_Report();


        backarrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Employee_reportview.super.onBackPressed();
            }
        });



    }



    public void get_Report()
    {
        Query query2 = FirebaseDatabase.getInstance().getReference("Employee_work_report")
                .orderByChild("Employee_email").equalTo(employee_email);
        query2.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {

                        Task_list task_list = new Task_list();
                        task_list.task_description = snapshot.child("work_description").getValue().toString();
                        task_list.file_url = snapshot.child("File_url").getValue().toString();
                        task_list.file_name = snapshot.child("File_name").getValue().toString();
                        task_list.submitted_date = snapshot.child("Submitted_date").getValue().toString();
                        task_list.period = snapshot.child("Period").getValue().toString();
                        array_report.add(task_list);
                        task_list = null;

                    }
                    report_adapter adapter_main = new report_adapter(array_report,getBaseContext(),Employee_reportview.this);
                    listBar.setAdapter(adapter_main);
                    adapter_main.notifyDataSetChanged();
                    progressDialog.dismiss();

                }
                else
                {
                    Toast.makeText(Employee_reportview.this,  getResources().getString(R.string.No_Task_found), Toast.LENGTH_SHORT).show();
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
}
