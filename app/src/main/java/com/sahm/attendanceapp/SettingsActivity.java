package com.sahm.attendanceapp;

import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Locale;

import static com.sahm.attendanceapp.Manager_Dashboard.resetInstanceId;


public class SettingsActivity extends AppCompatActivity
{
    TextView english,arabic;
    ImageView backarrow;
    LinearLayout logout,ll_password;

    SharedPreferences sp;


    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(LocaleHelper.onAttach(base));
    }



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        sp=getSharedPreferences("login",MODE_PRIVATE);
        logout=findViewById(R.id.ll_logout);
        english=findViewById(R.id.english);
        arabic=findViewById(R.id.arabic);
        ll_password=findViewById(R.id.ll_password);

        backarrow=findViewById(R.id.backarrow);

        backarrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                SettingsActivity.super.onBackPressed();

            }
        });
        String locale = getResources().getConfiguration().locale.toString();
        if(locale.equals("ar"))
        {
            arabic.setBackgroundResource(R.drawable.language_select_left);
            english.setBackgroundResource(R.drawable.language_design_right);
            arabic.setTextColor(getResources().getColor(R.color.white));
            english.setTextColor(getResources().getColor(R.color.black));
        }
        else
        {

        }

        arabic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                setLocale("ar");

            }
        });
        english.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                setLocale("en");

            }
        });

        ll_password.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                Intent intent = new Intent(SettingsActivity.this, change_password.class);
                startActivity(intent);


            }
        });


        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {

                AlertDialog.Builder builder = new AlertDialog.Builder(SettingsActivity.this);
                builder.setCancelable(false);
                builder.setMessage(getResources().getString(R.string.Are_you_sure_you_want_to_exit_app));
                builder.setPositiveButton(getResources().getString(R.string.Yes), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //if user pressed "yes", then he is allowed to exit from application
                        resetInstanceId();
                        stopService(new Intent(SettingsActivity.this, MyService.class));
                        SharedPreferences.Editor e=sp.edit();
                        e.clear();
                        e.commit();

                        Intent intent = new Intent(SettingsActivity.this, LoginDashboardActivity.class);
                        startActivity(intent);
                        finish();
                    }
                });
                builder.setNegativeButton(getResources().getString(R.string.No),new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //if user select "No", just cancel this dialog and continue with app
                        dialog.cancel();
                    }
                });
                AlertDialog alert=builder.create();
                alert.show();
            }
        });
    }


    public void setLocale(String lang)
    {
       Locale myLocale = new Locale(lang);

        Resources resources = getResources();
        DisplayMetrics dm = resources.getDisplayMetrics();
        Configuration config = resources.getConfiguration();
        if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.JELLY_BEAN_MR1){
            config.setLocale(myLocale);
        } else {
            config.locale = myLocale;
        }
        resources.updateConfiguration(config, dm);



//
//        Locale locale = new Locale(lang);
//        Locale.setDefault(locale);
//        Resources resources = getApplicationContext().getResources();
//        Configuration config = resources.getConfiguration();
//        config.setLocale(locale);
//        resources.updateConfiguration(config, resources.getDisplayMetrics());

//        myLocale = new Locale(lang);
//        Resources res = getResources();
//        Configuration conf = res.getConfiguration();
//
//        if (Build.VERSION.SDK_INT >= 17) {
//            conf.setLocale(myLocale);
//        } else {
//            conf.locale = myLocale;
//        }
//        res.updateConfiguration(conf, res.getDisplayMetrics());

        SharedPreferences.Editor edit = sp.edit();
        edit.putString("language", lang);
        edit.commit();
        triggerRebirth(getBaseContext());
//
//        Intent refresh = new Intent(this, SplashActivity.class);
//        refresh.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
//        finish();
//        startActivity(refresh);
    }


    public static void triggerRebirth(Context context) {
        PackageManager packageManager = context.getPackageManager();
        Intent intent = packageManager.getLaunchIntentForPackage(context.getPackageName());
        ComponentName componentName = intent.getComponent();
        Intent mainIntent = Intent.makeRestartActivityTask(componentName);
        context.startActivity(mainIntent);
        Runtime.getRuntime().exit(0);
    }

}
