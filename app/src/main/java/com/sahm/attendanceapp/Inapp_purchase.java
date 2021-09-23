package com.sahm.attendanceapp;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import com.android.billingclient.api.SkuDetails;
import com.anjlab.android.iab.v3.BillingProcessor;

public class Inapp_purchase extends AppCompatActivity{
    Button subcribe_month,subcribe_year;
    String usertype, Manager_email, manager_username, current_currency, manager_name;
    SharedPreferences sp;
    BillingProcessor bp;
    ImageView backarroow;
    SkuDetails skuDetails;
    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(LocaleHelper.onAttach(base));
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inapp_purchase);
        sp=getSharedPreferences("login",MODE_PRIVATE);

        if (sp.contains("user_type"))
        {

            String v = sp.getString("user_type", usertype).trim();
            if (v.equals("manager")) {

                Manager_email = sp.getString("email", "").trim();
                manager_username = sp.getString("username", "").trim();
                manager_name = sp.getString("name", "").trim();
                current_currency = sp.getString("currency", "").trim();


            } else if (v.equals("employee")) {
                Intent intent = new Intent(Inapp_purchase.this, Employee_Dashboard.class);
                startActivity(intent);
            } else {
                Intent intent = new Intent(Inapp_purchase.this, LoginDashboardActivity.class);
                startActivity(intent);
            }
        }
        subcribe_month = findViewById(R.id.subcribe_month);
        subcribe_year = findViewById(R.id.subcribe_year);
        backarroow = findViewById(R.id.backarrow);





        subcribe_month.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {



            }
        });
        subcribe_year.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


            }
        });

        backarroow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Inapp_purchase.super.onBackPressed();
            }
        });

    }




}