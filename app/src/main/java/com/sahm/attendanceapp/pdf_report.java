package com.sahm.attendanceapp;

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

public class pdf_report extends AppCompatActivity {
    ImageView backarroow;
    String usertype, Manager_email, manager_username, manager_name, v;
    SharedPreferences sp;
    ListView listBar;
    pdf_report_adapter adapter_main;
    List<Em_list> array_employeelist = new ArrayList<>();

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(LocaleHelper.onAttach(base));
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pdf_report);
        backarroow = findViewById(R.id.backarrow);

        sp = getSharedPreferences("login", MODE_PRIVATE);
        if (sp.contains("user_type")) {
            v = sp.getString("user_type", usertype).trim();
            if (v.equals("manager")) {

                Manager_email = sp.getString("email", "manager").trim();
                manager_username = sp.getString("username", "m_username").trim();
                manager_name = sp.getString("name", "m_name").trim();


            } else if (v.equals("employee")) {
                Intent intent = new Intent(pdf_report.this, Employee_Dashboard.class);
                startActivity(intent);
                this.finish();
            } else {
                Intent intent = new Intent(pdf_report.this, LoginDashboardActivity.class);
                startActivity(intent);
                this.finish();
            }

        }



        listBar = findViewById(R.id.listview);
        getdata();



//        listBar.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//            @Override
//            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l)
//            {
//
//
//                    Intent intent = new Intent(pdf_report.this, EmployeeProfileActivity.class);
//                    intent.putExtra("target_email", array_employeelist.get(position).email);
//                    intent.putExtra("target_name", array_employeelist.get(position).name);
//                    intent.putExtra("target_username", array_employeelist.get(position).username);
//                    intent.putExtra("currency", " ");
//                    startActivity(intent);
//
//
//
//
//
//            }
//        });





        backarroow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pdf_report.super.onBackPressed();
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
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        Em_list em_list = new Em_list();
                        em_list.name = snapshot.child("name").getValue().toString();
                        em_list.email = snapshot.child("email").getValue().toString();
                        em_list.phone = snapshot.child("phone").getValue().toString();
                        em_list.salary = snapshot.child("Salary").getValue().toString();
                        em_list.duration = snapshot.child("duration").getValue().toString();
                        em_list.username = snapshot.child("username").getValue().toString().trim();
                        array_employeelist.add(em_list);
                        em_list = null;
                    }
                    adapter_main = new pdf_report_adapter(array_employeelist, getBaseContext(),  pdf_report.this  );
                    listBar.setAdapter(null);
                    listBar.setAdapter(adapter_main);
                    adapter_main.notifyDataSetChanged();



                } else {
                    Toast.makeText(pdf_report.this, R.string.No_Employee_Found, Toast.LENGTH_SHORT).show();

                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


    }


}