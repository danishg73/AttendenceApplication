package com.sahm.attendanceapp;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

public class LoginDashboardActivity extends AppCompatActivity
{

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(LocaleHelper.onAttach(base));
    }
    CardView card_manager,card_employee;
    SharedPreferences sp;
    String usertype;


    @Override
    protected void onCreate(Bundle savedInstanceState) {



        super.onCreate(savedInstanceState);
        sp = this.getSharedPreferences("login", MODE_PRIVATE);

        if( sp.contains("user_type")){

            String v=  sp.getString("user_type",usertype).trim();
            if (v.equals("manager")){

                Intent intent = new Intent(LoginDashboardActivity.this, Manager_Dashboard.class);
                startActivity(intent);
            }
            else if (v.equals("employee")){
                Intent intent = new Intent(LoginDashboardActivity.this, Employee_Dashboard.class);
                startActivity(intent);
            }
        }



        setContentView(R.layout.activity_select_role);
        card_manager= findViewById(R.id.card_manager);
        card_employee= findViewById(R.id.card_employee);

        card_manager.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(LoginDashboardActivity.this,Login_manager.class);
                startActivity(intent);
            }
        });

        card_employee.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(LoginDashboardActivity.this,Login_Employee.class);

                startActivity(intent);
            }
        });
    }
}
