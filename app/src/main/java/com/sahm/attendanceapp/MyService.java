package com.sahm.attendanceapp;


import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;


public class MyService extends Service {


    SharedPreferences sp;
    String usertype,manager;
    int k=0;
    String Manager_email,current_date;
    String Employee_name,Task_date;
    Handler handler;
    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(LocaleHelper.onAttach(base));
    }
    public int onStartCommand(Intent intent, int flags, int startId){
//        onTaskRemoved(intent);


        sp=getSharedPreferences("login",MODE_PRIVATE);

        if( sp.contains("user_type")){

            String v=  sp.getString("user_type",usertype).trim();
            if (v.equals("manager"))
            {

                Manager_email = sp.getString("email",manager).trim();
            }
            else if (v.equals("employee")){

            }
            else{
            }
        }


        Query query = FirebaseDatabase.getInstance().getReference("Employee_Task")
                .orderByChild("Manager").equalTo(Manager_email);


        handler=new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run()
            {
                query.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            for (DataSnapshot snapshot : dataSnapshot.getChildren()) {

                                    if (snapshot.child("Status").getValue().toString().equals("Not Completed"))
                                    {
                                        Task_date = snapshot.child("Expiry_Date").getValue().toString();
                                        Employee_name  = snapshot.child("Employee_Name").getValue().toString();
                                        Calendar c = Calendar.getInstance();
                                        SimpleDateFormat df = new SimpleDateFormat("dd-MM-yyyy");
                                        current_date = df.format(c.getTime());
                                        try {

                                            Date date1;
                                            Date date2;
                                            SimpleDateFormat dates = new SimpleDateFormat("dd-MM-yyyy");
                                            date1 = dates.parse(current_date);
                                            date2 = dates.parse(Task_date);
                                            //Comparing dates
                                            long difference = Math.abs(date1.getTime() - date2.getTime());
                                            long differenceDates = difference / (24 * 60 * 60 * 1000);
                                            //Convert long to String
                                            String dayDifference = Long.toString(differenceDates);
                                            if(dayDifference.equals("1"))
                                            {
                                                if(date1.before(date2))
                                                {
                                                    notification();
                                                }
                                            }


                                        } catch (Exception exception) {
                                        }
                                    }
                            }

                        }
                        else
                        {

                        }
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError)
                    {


                    }
                });


                handler.postDelayed(this,8*60*60*1000);
            }
        },10*1000);


        return START_STICKY;
    }
    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }
    @Override
    public void onDestroy() {
        handler.removeCallbacksAndMessages(null);;
    }



    @Override
    public void onTaskRemoved(Intent rootIntent) {
        Intent restartServiceIntent = new Intent(getApplicationContext(),this.getClass());
        restartServiceIntent.setPackage(getPackageName());
        startService(restartServiceIntent);
        super.onTaskRemoved(rootIntent);
    }


    public void notification()
    {



        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        String NOTIFICATION_CHANNEL_ID = "SahmApplication";
        Intent myIntent = new Intent(this, Manager_Dashboard.class);
        myIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0 /* Request code */, myIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);


        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Solo para android Oreo o superior
            @SuppressLint("WrongConstant")
            NotificationChannel channel = new NotificationChannel(NOTIFICATION_CHANNEL_ID,
                    "Sahm Attendance App",
                    NotificationManager.IMPORTANCE_MAX
            );
            //Notification channel configuration
            channel.setDescription(getResources().getString(R.string.Attendance_App));
            channel.enableLights(true);
            channel.setLightColor(Color.BLUE);
            channel.setVibrationPattern(new long[]{0, 500});
            channel.enableVibration(true);
            manager.createNotificationChannel(channel);

        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID);

        builder.setAutoCancel(true)
                .setDefaults(Notification.DEFAULT_ALL)
                .setWhen(System.currentTimeMillis())
                .setSmallIcon(R.drawable.applogo)
                .setContentTitle(getResources().getString(R.string.Task_Not_Completed))
                .setContentIntent(pendingIntent)
                .setContentText(Employee_name+getResources().getString(R.string.didnot_completed_his_task)+Task_date)
                .setVibrate(new long[] { 0, 500 })
                .setStyle(new NotificationCompat.BigTextStyle().bigText(Employee_name+getResources().getString(R.string.didnot_completed_his_task)+Task_date ))

                .setDefaults(Notification.DEFAULT_SOUND)
                .setContentInfo(getResources().getString(R.string.Message));

        manager.notify(++k, builder.build());






    }


}