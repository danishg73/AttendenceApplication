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

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;


public class Work_report extends AppCompatActivity {
    ImageView backarrow;
    EditText work_detail;
    Button submit;
    String Today_date;
    String  work_description,time,usertype,employee,name;
    Spinner spinner;
    TextView attachfile;
    Uri pdfuri;
    ProgressDialog progressDialog;
    SharedPreferences sp;
    String employee_mail,employee_name,file_name;
    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(LocaleHelper.onAttach(base));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_work_report);

        sp=getSharedPreferences("login",MODE_PRIVATE);
        if( sp.contains("user_type")){

            String v=  sp.getString("user_type",usertype).trim();
            if (v.equals("manager"))
            {
                Intent intent = new Intent(Work_report.this, LoginDashboardActivity.class);
                startActivity(intent);

            }
            else if (v.equals("employee")){

                employee_mail = sp.getString("email",employee).trim();
                employee_name = sp.getString("name",name).trim();
            }
            else{
                Intent intent = new Intent(Work_report.this, LoginDashboardActivity.class);
                startActivity(intent);
            }
        }

        backarrow = findViewById(R.id.backarrow);
        spinner =  findViewById(R.id.select_time);
        work_detail =  findViewById(R.id.work_detail);
        submit =  findViewById(R.id.submit_work);
        attachfile =  findViewById(R.id.attach_file);
        progressDialog = new ProgressDialog(this);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progressDialog.setTitle(getResources().getString(R.string.Uploading_File));
        file_name ="No file";


        String[] items = new String[]{"Select period","Today","Last 7 Days" ,"Last 30 Days"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.spinner_item, items);
        spinner.setAdapter(adapter);

        attachfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(ContextCompat.checkSelfPermission(Work_report.this, Manifest.permission.READ_EXTERNAL_STORAGE)== PackageManager.PERMISSION_GRANTED)
                {
                    selectpdf();
                }
                else
                {
                    ActivityCompat.requestPermissions(Work_report.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                            9);
                }
            }
        });


        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                work_description = work_detail.getText().toString().trim();
                time = spinner.getSelectedItem().toString();

                Calendar c = Calendar.getInstance();
                SimpleDateFormat df = new SimpleDateFormat("dd-MM-yyyy");// HH:mm:ss");
                 Today_date = df.format(c.getTime());
                 if(time.equals("Select period"))
                 {
                     Toast.makeText(Work_report.this, getResources().getString(R.string.Select_Report_Period), Toast.LENGTH_SHORT).show();
                 }
                 else
                 {


                if ( TextUtils.isEmpty(work_description) ) {
                    Toast.makeText(Work_report.this, getResources().getString(R.string.All_fields_are_required), Toast.LENGTH_SHORT).show();
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

            }
        });

        backarrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Work_report.super.onBackPressed();
            }
        });

    }

    private void uploadfile(Uri pdfuri)
    {
        progressDialog.setProgress(0);
        progressDialog.show();
        file_name =getFileName(pdfuri);

        StorageReference storageReference =  FirebaseStorage.getInstance().getReference();
        storageReference.child("Work_report").child(getFileName(pdfuri)).putFile(pdfuri)
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
                Toast.makeText(Work_report.this, getResources().getString(R.string.Uploading_Failed), Toast.LENGTH_SHORT).show();
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

        map.put("File_url",downloadurl);
        map.put("File_name",file_name);
        map.put("Period",time);
        map.put("Submitted_date",Today_date);
        map.put("work_description",work_description);
        map.put("Employee_name",employee_name);
        map.put("Employee_email",employee_mail);
        DatabaseReference db;
        db = FirebaseDatabase.getInstance().getReference().child("Employee_work_report").push();
        String key= db.getKey();
        map.put("key",key);
//        FirebaseDatabase.getInstance().getReference().child("Employee_Task").push()
        db.setValue(map)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {

                        if(task.isSuccessful())
                        {
                            Toast.makeText(Work_report.this, getResources().getString(R.string.Work_Submitted), Toast.LENGTH_SHORT).show();
                            progressDialog.dismiss();
                        }
                        else
                        {
                            Toast.makeText(Work_report.this, getResources().getString(R.string.Error), Toast.LENGTH_SHORT).show();
                            progressDialog.dismiss();
                        }

                    }
                });
        work_detail.setText("");
        attachfile.setText(getResources().getString(R.string.Select_file));
        file_name ="No file";
        pdfuri=null;
    }



}
