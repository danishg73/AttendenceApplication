package com.sahm.attendanceapp;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class Task_complete_employee extends AppCompatActivity {
    ImageView backarrow;
    String task_name, task_key;
    String usertype;
    SharedPreferences sp;
    String   complete_description,delivery_date,file_name,manager_username,employee_name,employee_email;
    EditText description;
    TextView file,tsk_name;
    Button submit;
    Uri pdfuri;
    private RequestQueue mRequestQue;
    private String URL = "https://fcm.googleapis.com/fcm/send";

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(LocaleHelper.onAttach(base));
    }

    ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_complete_employee);

        sp=getSharedPreferences("login",MODE_PRIVATE);
        if( sp.contains("user_type")){

            String v=  sp.getString("user_type",usertype).trim();
            if (v.equals("manager"))
            {

                Intent intent = new Intent(Task_complete_employee.this, LoginDashboardActivity.class);

                startActivity(intent);

            }
            else if (v.equals("employee"))

            {
                manager_username = sp.getString("manager_username","");
                employee_name  =  sp.getString("name","name").trim();
                employee_email  =  sp.getString("email","email").trim();
            }
            else{
                Intent intent = new Intent(Task_complete_employee.this, LoginDashboardActivity.class);
                startActivity(intent);
            }
        }
        task_name = getIntent().getStringExtra("task_name");
        task_key = getIntent().getStringExtra("task_key");

        backarrow = findViewById(R.id.backarrow);
        description =  findViewById(R.id.complete_description);
        submit =  findViewById(R.id.submit_work);
        file =  findViewById(R.id.attach_file);
        tsk_name =  findViewById(R.id.task_name);
        tsk_name.setText(task_name);
        file_name="No file";

        progressDialog = new ProgressDialog(this);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progressDialog.setTitle(getResources().getString(R.string.Uploading_File));


        file.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(ContextCompat.checkSelfPermission(Task_complete_employee.this, Manifest.permission.READ_EXTERNAL_STORAGE)== PackageManager.PERMISSION_GRANTED)
                {
                    selectpdf();
                }
                else
                {
                    ActivityCompat.requestPermissions(Task_complete_employee.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                            9);
                }
            }
        });


        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                complete_description = description.getText().toString().trim();
                Calendar c = Calendar.getInstance();
                SimpleDateFormat df = new SimpleDateFormat("dd-MM-yyyy");// HH:mm:ss");
                delivery_date = df.format(c.getTime());


                if ( TextUtils.isEmpty(complete_description) ) {
                    Toast.makeText(Task_complete_employee.this, getResources().getString(R.string.All_fields_are_required), Toast.LENGTH_SHORT).show();
                }
                else
                {
                    if (pdfuri!=null)
                    {
                        uploadfile(pdfuri);
                    }
                    else
                    {
                        insert_database("No Attachments found!");

                    }
                }

            }
        });

        backarrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Task_complete_employee.super.onBackPressed();
            }
        });

    }




    private void uploadfile(Uri pdfuri)
    {
        file_name =getFileName(pdfuri);
        progressDialog.setProgress(0);
        progressDialog.show();

        StorageReference storageReference =  FirebaseStorage.getInstance().getReference();
        storageReference.child("Employee_task").child(file_name).putFile(pdfuri)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot)
                    {

                        Task<Uri> uri = taskSnapshot.getStorage().getDownloadUrl();
                        while(!uri.isComplete());
                        Uri downloadurlurl = uri.getResult();
                        insert_database(downloadurlurl.toString());

                    }
                }).addOnFailureListener(new OnFailureListener()
        {
            @Override
            public void onFailure(@NonNull Exception e)
            {
                Toast.makeText(Task_complete_employee.this, getResources().getString(R.string.Uploading_Failed), Toast.LENGTH_SHORT).show();
                progressDialog.dismiss();

            }
        }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>()
        {
            @Override
            public void onProgress(@NonNull UploadTask.TaskSnapshot taskSnapshot)
            {
                int current_progress = (int) (100*taskSnapshot.getBytesTransferred()/taskSnapshot.getTotalByteCount());
                progressDialog.setProgress(current_progress);

            }
        });
    }

    @Override
    public void onRequestPermissionsResult(final int requestCode, @NonNull final String[] permissions, @NonNull final int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 9) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted.
                selectpdf();
            } else
            {
                Toast.makeText(this, getResources().getString(R.string.Permission_Not_Granted), Toast.LENGTH_SHORT).show();
            }

        }
    }



    private void selectpdf()

    {
        Intent intent = new Intent();
        intent.setType("*/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, 86);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode ==86 && resultCode == RESULT_OK && data != null)
        {
            pdfuri = data.getData();
            file_name =getFileName(pdfuri);
            file.setText(file_name);

        }
        else
        {
            Toast.makeText(this, getResources().getString(R.string.Select_file), Toast.LENGTH_SHORT).show();
        }
    }

    public String getFileName(Uri uri) {
        String result = null;
        if (uri.getScheme().equals("content")) {
            Cursor cursor = getContentResolver().query(uri, null, null, null, null);
            try {
                if (cursor != null && cursor.moveToFirst()) {
                    result = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                }
            } finally {
                cursor.close();
            }
        }
        if (result == null) {
            result = uri.getPath();
            int cut = result.lastIndexOf('/');
            if (cut != -1) {
                result = result.substring(cut + 1);
            }
        }
        return result;
    }


    public void insert_database(String downloadurl)
    {
        HashMap<String,Object> map = new HashMap<>();
        //

        map.put("Complete_Description",complete_description);
        map.put("Delivery_File",file_name);
        map.put("Delivery_File_url",downloadurl);
        map.put("Delivery_Date",delivery_date);
        map.put("Status","Completed");
//
//        db.child("Employee_Task").child(task_key).child("Complete_Description").setValue(complete_description);
//        db.child("Employee_Task").child(task_key).child("Delivery_File").setValue(downloadurl);
//        db.child("Employee_Task").child(task_key).child("Status").setValue("Completed");
//        db.child("Employee_Task").child(task_key).child("Delivery_Date").setValue(delivery_date);
        DatabaseReference db;
        db = FirebaseDatabase.getInstance().getReference();
        db.child("Employee_Task").child(task_key).updateChildren(map)

                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {

                        if(task.isSuccessful())
                        {
                            Toast.makeText(Task_complete_employee.this, getResources().getString(R.string.Work_Submitted), Toast.LENGTH_SHORT).show();
                            progressDialog.dismiss();
                            sendNotification();
                            Task_complete_employee.super.onBackPressed();
                        }
                        else
                        {
                            Toast.makeText(Task_complete_employee.this, getResources().getString(R.string.Error), Toast.LENGTH_SHORT).show();
                            progressDialog.dismiss();
                        }

                    }
                });
        description.setText("");
        file.setText(getResources().getString(R.string.Select_file));
        pdfuri=null;
    }


    private void sendNotification() {


        mRequestQue = Volley.newRequestQueue(this);
        JSONObject json = new JSONObject();
        try {

            json.put("to", "/topics/" +manager_username );
            // json.put("to", token);
            json.put("content_available", true);

            JSONObject notificationObj = new JSONObject();
            notificationObj.put("title", getResources().getString(R.string.Assigment));
            notificationObj.put("body", employee_name+  getResources().getString(R.string.submittted_his_task) +task_name);
            notificationObj.put("click_action","Assigment_activity");
            JSONObject extraData = new JSONObject();
            extraData.put("sender_username",employee_email);
            extraData.put("target_email",manager_username);

            json.put("notification",notificationObj);
            json.put("data",extraData);


            JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, URL,
                    json,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {

                            Log.d("MUR", "onResponse: ");
                        }
                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Log.d("MUR", "onError: "+error.networkResponse);
                }
            }
            ){
                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                    Map<String,String> header = new HashMap<>();
                    header.put("content-type","application/json");
                    //         header.put("authorization","key=AAAA6f0HMNg:APA91bFdYWHj6IemP5ZqmX3V6irrhL_YOOx1RD_jUVPOQ9exjFfM0ZajH5IoEGrsCEKwuF6l5dSfWLAmnZTq1MyRr6bl2y9ydb49UPQNcpcs5aN1wBlrDnTz0BecsjOdGZgOloZkvab_");
                    header.put("authorization","key="+getResources().getString(R.string.firebase_notification_key));
                    return header;
                }
            };
            mRequestQue.add(request);
        }
        catch (JSONException e)

        {
            e.printStackTrace();
        }
    }


}
