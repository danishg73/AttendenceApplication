package com.sahm.attendanceapp;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;

import androidx.appcompat.app.AppCompatActivity;

public class SplashActivity extends AppCompatActivity
{
    private  static final int code=2000;

    SharedPreferences sp;


    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(LocaleHelper.onAttach(base));
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        sp=getSharedPreferences("login",MODE_PRIVATE);

        Handler handler=new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run()
            {

                if( sp.contains("user_type")){

                    String v=  sp.getString("user_type","").trim();
                    if (v.equals("manager"))
                    {
                        Intent intent = new Intent(SplashActivity.this, Manager_Dashboard.class);
                        startActivity(intent);
                        finish();
                    }
                    else if (v.equals("employee"))
                    {
                        Intent intent = new Intent(SplashActivity.this, Employee_Dashboard.class);
                        startActivity(intent);
                        finish();

                    }
                    else{
                        Intent intent = new Intent(SplashActivity.this, LoginDashboardActivity.class);
                        startActivity(intent);
                        finish();
                    }
                }
                else
                {

                    Intent intent=new Intent(SplashActivity.this,LoginDashboardActivity.class);
                    startActivity(intent);
                    finish();

                }
            }
        },code);
    }
}
