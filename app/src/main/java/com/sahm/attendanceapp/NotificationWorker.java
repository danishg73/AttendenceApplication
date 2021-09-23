package com.sahm.attendanceapp;

import android.content.Context;
import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

public class NotificationWorker extends Worker {
    public NotificationWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);



    }



    @NonNull
    @Override
    public Result doWork() {
        try
        {
            Intent service1 = new Intent(getApplicationContext(), attendanceCheck_service.class);
            getApplicationContext().startService(service1);
            return Result.success();

        }
        catch (Exception e)
        {

            return null;
        }


    }
}
