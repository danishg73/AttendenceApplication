package com.sahm.attendanceapp;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;

import androidx.core.app.NotificationCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Map;

public class MyFirebaseMessagingService extends FirebaseMessagingService {
    static int k = 0;
    String a[]=null;
    String receive="";
    PendingIntent pendingIntent;
    NotificationManager notificationManager;
    Intent intent;
    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(LocaleHelper.onAttach(base));
    }

    /**
     * this function will be called whenever fcm will push data to the application
     * @param remoteMessage
     */
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        if(remoteMessage.getData() != null) {
            sendNotificacion(remoteMessage);
        }
//        if(remoteMessage.getNotification() != null) {
//            sendNotificacion(remoteMessage);
//        }
    }

    /**
     * this function will generate the notification
     * @param remoteMessage
     */
    @SuppressLint("WrongConstant")
    private void sendNotificacion(RemoteMessage remoteMessage) {

        /**
         * Map data type is type of data structure for fast indexing, every value will be composed of (key, value)
         * to access the data we will give call to the key, key for every value will be different
         */
        Map<String, String> data = remoteMessage.getData();

        /**
         * data.get("title"), in this statement title is the key of the value of title, on calling this key the
         * respective value, will saved to the string title, and same case for body
         */
        String title = remoteMessage.getNotification().getTitle();
        String body = remoteMessage.getNotification().getBody();
        String  sender_username=data.get("sender_username");
        String target_email=data.get("target_email");
//        String title ="hi";
//        String body="hi";

        if(title.equals("Assigment"))
        {

            intent = new Intent(this, task_view.class);
        }
        else if(title.equals("Attendance"))
        {

            intent = new Intent(this,Manager_Dashboard.class);
        }
        else
        {

            intent = new Intent(this, Messenger.class);
            intent.putExtra("target_email", target_email);
            intent.putExtra("target_name", title);
            intent.putExtra("target_username", sender_username);
        }

        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        String NOTIFICATION_CHANNEL_ID = "SahmApplication";





        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        pendingIntent = PendingIntent.getActivity(this, 0 /* Request code */, intent,
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
                .setContentTitle(title)
                .setContentText(body)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .setVibrate(new long[] { 0, 500 })
                .setStyle(new NotificationCompat.BigTextStyle().bigText(body ))

                .setDefaults(Notification.DEFAULT_SOUND)
                .setContentInfo(getResources().getString(R.string.Message));

        manager.notify(++k, builder.build());


}}
