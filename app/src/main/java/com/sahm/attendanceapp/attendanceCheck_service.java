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
import android.text.format.DateFormat;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class attendanceCheck_service extends Service {


    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(LocaleHelper.onAttach(base));
    }


    SharedPreferences sp;
    String usertype,manager,Manager_email,Day;
    List<Time_list> teams = new ArrayList<>();
    int k=0;
    Handler handler;
    public int onStartCommand(Intent intent, int flags, int startId){

        sp=getSharedPreferences("login",MODE_PRIVATE);

        if( sp.contains("user_type")){

            String v=  sp.getString("user_type",usertype).trim();
            if (v.equals("manager"))
            {

                Manager_email = sp.getString("email",manager).trim();
            }
            else if (v.equals("employee")){
                stopSelf();

            }
            else
                {
                stopSelf();
                }
        }

//        Toast.makeText(this, "service 1"+k, Toast.LENGTH_SHORT).show();

        Query query1 = FirebaseDatabase.getInstance().getReference("Timings").orderByChild("manager").equalTo(Manager_email);
        query1.addValueEventListener(new ValueEventListener()
        {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists())
                {
                    teams.clear();
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        Time_list object = new Time_list();
                        object.start_time=snapshot.child("start_time").getValue().toString();
                        object.end_time=snapshot.child("end_time").getValue().toString();
                        object.days=snapshot.child("days").getValue().toString();
                        object.shiftname=snapshot.child("shiftname").getValue().toString();
                        teams.add(object);
                        object=null;
                    }
                    dowork();
                }


            }
            @Override
            public void onCancelled(DatabaseError databaseError)
            {
                Toast.makeText(getApplicationContext(), getResources().getString(R.string.Alert_Start_Failed)+databaseError, Toast.LENGTH_SHORT).show();

            }
        });




//        return super.onStartCommand(intent, flags, startId);

        return START_STICKY;
    }
    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }
    @Override
    public void onDestroy()
    {
        stopService(new Intent(this, attendanceCheck_service.class));
        super.onDestroy();
    }



public void dowork()
{

    Date date = new Date();
    Day= DateFormat.format("EEEE", date.getTime()).toString();
    Query data = FirebaseDatabase.getInstance().getReference("Employee_data")
            .orderByChild("manager").equalTo(Manager_email);
    data.addListenerForSingleValueEvent(new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot dataSnapshot)
        {
            if (dataSnapshot.exists())
            {
                for (DataSnapshot snapshot : dataSnapshot.getChildren())
                {
                    if (snapshot.child("Punched").getValue().equals("no")) {
                        String shift = snapshot.child("shift").getValue().toString().trim();
                        int index =0;
                        for (Time_list t : teams) {

                            if (t.shiftname.contains(shift)) {

//                            }


//                        if (Arrays.asList(teams).contains(shift)) {

//                            int pos = Arrays.asList(teams).indexOf(shift);
                            int pos = index;
                            if (teams.get(pos).days.contains(Day)) {
                                String s_time = teams.get(pos).start_time;
                                try {
                                    String gettime = time_diff(s_time);
                                    if (gettime.equals("no")) {

                                    } else {
                                        if (Integer.parseInt(gettime) > 10 && Integer.parseInt(gettime) < 30) {
                                            notification(snapshot.child("name").getValue().toString().trim(),s_time);
                                        }
                                    }


                                } catch (ParseException e) {
                                    e.printStackTrace();
                                }


                            }
                        }
                            index++;

                    }
                    }
                }
            }
        }

        @Override
        public void onCancelled(@NonNull DatabaseError databaseError) {

        }
    });
}



    public String time_diff( String end_time) throws ParseException {

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("hh:mm a");
        String start_time = simpleDateFormat.format(new Date());

        java.text.DateFormat df = java.text.DateFormat.getInstance();
        Date date1 = simpleDateFormat.parse(start_time);
        Date date2 = simpleDateFormat.parse(end_time);
        long difference = date1.getTime() - date2.getTime();


        int day = (int) (difference / (1000*60*60*24));
        int hours = (int) ((difference - (1000*60*60*24*day)) / (1000*60*60));
        int min = (int) (difference - (1000*60*60*24*day) - (1000*60*60*hours)) / (1000*60);
        if(hours<0 )
        {
            hours = hours+24;
        }
        if(min<0)
        {
            min=min+60;
            if(hours==0)
            {
                hours= hours+23;
            }
        }
        if(hours>0)
        {
            return "no";
        }

        return ""+min;
    }


    public void notification(String name,String time)
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
                .setContentTitle(getResources().getString(R.string.Attendance_not_marked))
                .setContentIntent(pendingIntent)
                .setContentText(name+getResources().getString(R.string.did_not_punch_in_yet)+time)
                .setVibrate(new long[] { 0, 500 })
                .setStyle(new NotificationCompat.BigTextStyle().bigText(name+getResources().getString(R.string.did_not_punch_in_yet)+time))

                .setDefaults(Notification.DEFAULT_SOUND)
                .setContentInfo(getResources().getString(R.string.Message));

        manager.notify(++k, builder.build());






    }


}