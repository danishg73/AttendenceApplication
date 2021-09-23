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

public class change_password extends AppCompatActivity {
    EditText old_password,new_password,confirm_password;
    String old_code, new_code,confirm_code;
    Button submit;
    ProgressDialog progressDialog;
    String usertype, email;
    SharedPreferences sp;
    ImageView backarrow;
    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(LocaleHelper.onAttach(base));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password);



        sp = getSharedPreferences("login", MODE_PRIVATE);

        if (sp.contains("user_type")) {

            String v = sp.getString("user_type", usertype).trim();
            if (v.equals("manager")) {

                email = sp.getString("email", "").trim();


            } else if (v.equals("employee")) {
                email = sp.getString("email", "").trim();
            } else {
                Intent intent = new Intent(change_password.this, LoginDashboardActivity.class);
                startActivity(intent);
            }
        }
        old_password= findViewById(R.id.old_password);
        new_password= findViewById(R.id.new_password);
        confirm_password= findViewById(R.id.confirm_password);
        submit= findViewById(R.id.Submit);
        backarrow=findViewById(R.id.backarrow);
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage(getResources().getString(R.string.Loading));
        progressDialog.setCancelable(false);

        backarrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                change_password.super.onBackPressed();
            }
        });


        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                old_code="";
                new_code="";
                confirm_code="";
                old_code = old_password.getText().toString();
                new_code = new_password.getText().toString();
                confirm_code = confirm_password.getText().toString();

                if (TextUtils.isEmpty(old_code) || TextUtils.isEmpty(new_code) || TextUtils.isEmpty(confirm_code))
                {
                    Toast.makeText( change_password.this, getResources().getString(R.string.All_fields_are_required), Toast.LENGTH_SHORT).show();
                }
                else if ( !new_code.equals(confirm_code))
                {
                    Toast.makeText(change_password.this, getResources().getString(R.string.All_fields_are_required), Toast.LENGTH_SHORT).show();

                }
                else
                {
                    progressDialog.show();
                    Query q1 = FirebaseDatabase.getInstance().getReference("userinfo").orderByChild("email").equalTo(email);
                    q1.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if(dataSnapshot.exists())
                            {
                                for (DataSnapshot snapshot: dataSnapshot.getChildren())
                                {
                                    String get_email = snapshot.child("email").getValue().toString();
                                    String get_password = snapshot.child("password").getValue().toString();
                                    if(get_password.equals(old_code))
                                    {
                                        HashMap<String,Object> map1 = new HashMap<>();

                                        map1.put("password",new_code);

                                        snapshot.getRef().updateChildren(map1);
                                        Toast.makeText(change_password.this, getResources().getString(R.string.password_has_been_changed_sucessfully), Toast.LENGTH_SHORT).show();
                                        progressDialog.dismiss();
                                    }
                                    else
                                    {
                                        Toast.makeText(change_password.this, getResources().getString(R.string.Wrong_old_Password), Toast.LENGTH_SHORT).show();

                                        progressDialog.dismiss();
                                    }

                                }
                            }
                            old_password.setText("");
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