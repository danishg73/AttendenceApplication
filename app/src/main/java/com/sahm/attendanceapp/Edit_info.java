package com.sahm.attendanceapp;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;

public class Edit_info extends AppCompatActivity {

    EditText new_password,confirm_password;
    String  new_code,confirm_code;
    String Employee_email, Employee_name,Employee_username,Manager_email;
    Button submit;
    ProgressDialog progressDialog;
    SharedPreferences sp;
    ImageView backarrow;
    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(LocaleHelper.onAttach(base));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_info);

        sp = getSharedPreferences("login", MODE_PRIVATE);

        if (sp.contains("user_type")) {

            String v = sp.getString("user_type", "").trim();
            if (v.equals("manager"))
            {

                Manager_email = sp.getString("email", "").trim();


            } else if (v.equals("employee"))
            {
                Intent intent = new Intent(Edit_info.this, LoginDashboardActivity.class);
                startActivity(intent);
            }
            else
                {
                Intent intent = new Intent(Edit_info.this, LoginDashboardActivity.class);
                startActivity(intent);
            }
        }

        Employee_email= getIntent().getStringExtra("target_email");
        Employee_name= getIntent().getStringExtra("target_name");
        Employee_username= getIntent().getStringExtra("target_username");

        new_password= findViewById(R.id.new_password);
        confirm_password= findViewById(R.id.confirm_password);
        submit= findViewById(R.id.Submit);
        backarrow=findViewById(R.id.backarrow);
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage( getResources().getString(R.string.Loading));
        progressDialog.setCancelable(false);

        backarrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Edit_info.super.onBackPressed();
            }
        });



        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                new_code="";
                confirm_code="";
                new_code = new_password.getText().toString();
                confirm_code = confirm_password.getText().toString();

                if ( TextUtils.isEmpty(new_code) || TextUtils.isEmpty(confirm_code))
                {
                    Toast.makeText( Edit_info.this,  getResources().getString(R.string.All_fields_are_required), Toast.LENGTH_SHORT).show();
                }
                else if (!new_code.equals(confirm_code))
                {
                    Toast.makeText(Edit_info.this,  getResources().getString(R.string.New_password_are_not_same), Toast.LENGTH_SHORT).show();

                }
                else
                {
                    progressDialog.show();
                    Query q1 = FirebaseDatabase.getInstance().getReference("userinfo").orderByChild("email").equalTo(Employee_email);
                    q1.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if(dataSnapshot.exists())
                            {
                                for (DataSnapshot snapshot: dataSnapshot.getChildren())
                                {

                                        HashMap<String,Object> map1 = new HashMap<>();
                                        map1.put("password",new_code);
                                        snapshot.getRef().updateChildren(map1);
                                        Toast.makeText(Edit_info.this,  getResources().getString(R.string.password_has_been_changed_sucessfully), Toast.LENGTH_SHORT).show();
                                        progressDialog.dismiss();



                                }
                            }
                            new_password.setText("");
                            confirm_password.setText("");

                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                            progressDialog.dismiss();

                        }
                    });
                }
            }
        });




    }
}