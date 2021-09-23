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
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
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


public class WorkAssign extends AppCompatActivity {
    ImageView backarrow;
    EditText task,description;
    Button assign;
    String task_name,task_description,time,Expiray_date,usertype,employee_username,Manager_email,employee_name,manager_name,manager_username;
    Spinner spinner;
    TextView attachfile;
    Uri pdfuri;
    ProgressDialog progressDialog;
    SharedPreferences sp;
    String employee_mail,file_name;
    private RequestQueue mRequestQue;
    private String URL = "https://fcm.googleapis.com/fcm/send";
    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(LocaleHelper.onAttach(base));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_work_assign);

        sp=getSharedPreferences("login",MODE_PRIVATE);
        if( sp.contains("user_type")){

            String v=  sp.getString("user_type",usertype).trim();
            if (v.equals("manager"))
            {
                manager_name = sp.getString("name", "").trim();
                Manager_email = sp.getString("email","").trim();
                manager_username = sp.getString("username", "").trim();
            }
            else if (v.equals("employee")){
                Intent intent = new Intent(WorkAssign.this, Employee_Dashboard.class);
                startActivity(intent);
            }
            else{
                Intent intent = new Intent(WorkAssign.this, LoginDashboardActivity.class);
                startActivity(intent);
            }
        }
        employee_mail = getIntent().getStringExtra("employee_email");
        employee_name = getIntent().getStringExtra("employee_name");
        employee_username = getIntent().getStringExtra("employee_username");

        backarrow = findViewById(R.id.backarrow);
        spinner =  findViewById(R.id.select_time);
        task =  findViewById(R.id.task_name);
        description =  findViewById(R.id.description);
        assign =  findViewById(R.id.assign);
        attachfile =  findViewById(R.id.attach_file);
        progressDialog = new ProgressDialog(this);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progressDialog.setTitle(getResources().getString(R.string.Uploading_File));
        file_name ="No file";


        String[] items = new String[]{"1 Day","2 Day","3 Day" ,"4 Day" ,"5 Day" ,"6 Day" ,"7 Day" ,"8 Day" ,"9 Day" ,"10 Day"  };
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.spinner_item, items);
        spinner.setAdapter(adapter);

        attachfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(ContextCompat.checkSelfPermission(WorkAssign.this, Manifest.permission.READ_EXTERNAL_STORAGE)== PackageManager.PERMISSION_GRANTED)
                {
                    selectpdf();
                }
                else
                {
                    ActivityCompat.requestPermissions(WorkAssign.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                            9);
                }
            }
        });


        assign.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                task_name = task.getText().toString().trim();
                task_description = description.getText().toString().trim();
                time = spinner.getSelectedItem().toString();
                String[] s;
                s=time.split("\\s+");

                Calendar c = Calendar.getInstance();
                SimpleDateFormat df = new SimpleDateFormat("dd-MM-yyyy");// HH:mm:ss");
                String reg_date = df.format(c.getTime());
                c.add(Calendar.DATE, Integer.parseInt(s[0]));  // number of days to add
                Expiray_date = df.format(c.getTime());

                if (TextUtils.isEmpty(task_name) && TextUtils.isEmpty(task_description) ) {
                    Toast.makeText(WorkAssign.this, getResources().getString(R.string.All_fields_are_required), Toast.LENGTH_SHORT).show();
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

                WorkAssign.super.onBackPressed();
            }
        });




    }

    private void uploadfile(Uri pdfuri)
    {
        progressDialog.setProgress(0);
        progressDialog.show();
        file_name =getFileName(pdfuri);

         StorageReference storageReference =  FirebaseStorage.getInstance().getReference();
        storageReference.child("Employee_task").child(getFileName(pdfuri)).putFile(pdfuri)
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
                Toast.makeText(WorkAssign.this, getResources().getString(R.string.Uploading_Failed), Toast.LENGTH_SHORT).show();
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
            file_name=getFileName(pdfuri);
            attachfile.setText(file_name);

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

        map.put("Complete_Description","Not Completed yet");
        map.put("Delivery_File_url","Not Completed yet");
        map.put("Delivery_File","No file");
        map.put("Delivery_Date","24-2-1995");
        map.put("File_url",downloadurl);
        map.put("File_name",file_name);
        map.put("Status","Not Completed");
        map.put("Expiry_Date",Expiray_date);
        map.put("Task_Description",task_description);
        map.put("Task_Name",task_name);
        map.put("Manager",Manager_email);
        map.put("Employee_Name",employee_name);
        map.put("Employee_Email",employee_mail);
        DatabaseReference db;
        db = FirebaseDatabase.getInstance().getReference().child("Employee_Task").push();
        String key= db.getKey();
        map.put("key",key);
//        FirebaseDatabase.getInstance().getReference().child("Employee_Task").push()
                db.setValue(map)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {

                        if(task.isSuccessful())
                        {
                            Toast.makeText(WorkAssign.this, getResources().getString(R.string.Task_Assigned), Toast.LENGTH_SHORT).show();
                            sendNotification();
                            progressDialog.dismiss();
                        }
                        else
                        {
                            Toast.makeText(WorkAssign.this, getResources().getString(R.string.Task_Not_Assigned), Toast.LENGTH_SHORT).show();
                            progressDialog.dismiss();
                        }

                    }
                });
        task.setText("");
        description.setText("");
        attachfile.setText(getResources().getString(R.string.Select_file));
        file_name ="No file";
        pdfuri=null;
    }


    private void sendNotification() {


        mRequestQue = Volley.newRequestQueue(this);
        JSONObject json = new JSONObject();
        try {

            json.put("to", "/topics/" +employee_username );
           // json.put("to", token);
            json.put("content_available", true);

            JSONObject notificationObj = new JSONObject();
            notificationObj.put("title", getResources().getString(R.string.Assigment));
            notificationObj.put("body", getResources().getString(R.string.Your_manager)+ manager_name+ getResources().getString(R.string.assigned_you_a_task)+Expiray_date);
            notificationObj.put("click_action","Assigment_activity");
            JSONObject extraData = new JSONObject();
            extraData.put("sender_username",manager_username);
            extraData.put("target_email",employee_mail);

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
