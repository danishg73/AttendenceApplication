package com.sahm.attendanceapp;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;

import java.util.HashMap;

public class Login_Employee extends AppCompatActivity {

    TextView dontaccount;
    EditText regemail, regpass;
    Button btnreg;
    ProgressDialog progressDialog;
    String  usertype;
    SharedPreferences sp;
    String email;
    String key ;
    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(LocaleHelper.onAttach(base));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login__employee);

        sp = this.getSharedPreferences("login", MODE_PRIVATE);

        if( sp.contains("user_type")){

            String v=  sp.getString("user_type",usertype).trim();
            if (v.equals("manager")){

                Intent intent = new Intent(Login_Employee.this, Manager_Dashboard.class);
                startActivity(intent);
            }
            else if (v.equals("employee")){
                Intent intent = new Intent(Login_Employee.this, Employee_Dashboard.class);
                startActivity(intent);
            }
            else{
                Intent intent = new Intent(Login_Employee.this, LoginDashboardActivity.class);
                startActivity(intent);
            }
        }

        regemail = findViewById(R.id.editTextEmail);
        regpass = findViewById(R.id.editTextPassword);
        btnreg = findViewById(R.id.cirLoginButton);
        dontaccount = findViewById(R.id.donthaveaccount);
        dontaccount.setVisibility(View.INVISIBLE);
        progressDialog = new ProgressDialog( this);
        progressDialog.setMessage(getResources().getString(R.string.Siging_In));
        progressDialog.setCancelable(false);


        btnreg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                email = regemail.getText().toString();
                String pass = regpass.getText().toString();
                if (TextUtils.isEmpty(email) && TextUtils.isEmpty(pass))
                {
                    Toast.makeText(Login_Employee.this, getResources().getString(R.string.All_fields_are_required), Toast.LENGTH_SHORT).show();
                }
                else
                {
                    InputMethodManager imm = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
                    imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);

                    progressDialog.show();
                    Query query = FirebaseDatabase.getInstance().getReference("userinfo")
                            .orderByChild("email").equalTo(email);
                    query.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if (dataSnapshot.exists()) {
                                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {

                                    String get_email = snapshot.child("email").getValue().toString();
                                    String get_password = snapshot.child("password").getValue().toString();
                                    String get_name = snapshot.child("name").getValue().toString();
                                    String get_phone = snapshot.child("phone").getValue().toString();
                                    String get_type = snapshot.child("type").getValue().toString();
                                    String get_username = snapshot.child("username").getValue().toString();
                                    if (get_password.equals(pass))
                                    {
                                        if (get_type.equals("manager"))
                                        {
                                            Toast.makeText(Login_Employee.this, "Managers are not allow to login from here", Toast.LENGTH_SHORT).show();
                                            progressDialog.dismiss();
                                        } else if (get_type.equals("employee")) {

                                            SharedPreferences.Editor edit = sp.edit();
                                            edit.putString("email", email);
                                            edit.putString("name", get_name);
                                            edit.putString("user_type", "employee");
                                            edit.putString("phone_number", get_phone);
                                            edit.putString("username", get_username);
                                            insert_token();
                                            progressDialog.dismiss();
                                            edit.commit();
                                            Intent intent = new Intent(Login_Employee.this, Employee_Dashboard.class);
                                            startActivity(intent);
                                            finish();
                                        } else
                                        {

                                            progressDialog.dismiss();
                                            Toast.makeText(Login_Employee.this, getResources().getString(R.string.Account_doesnot_exist), Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                    else
                                    {
                                        progressDialog.dismiss();
                                        Toast.makeText(Login_Employee.this, getResources().getString(R.string.Wrong_Credentials), Toast.LENGTH_SHORT).show();
                                        progressDialog.dismiss();
                                    }
                                }
                            }
                            else
                            {
                                progressDialog.dismiss();
                                Toast.makeText(Login_Employee.this, getResources().getString(R.string.Wrong_Credentials), Toast.LENGTH_SHORT).show();
                            }
                        }
                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError)
                        {
                            progressDialog.dismiss();
                        }
                    });

                }



            }
        });

    }
    public void insert_token()
    {

        Query query4 =FirebaseDatabase.getInstance().getReference("FirebaseTokens").orderByChild("Email").equalTo(email);

        query4.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists())
                {

                    for (DataSnapshot snapshot : dataSnapshot.getChildren())
                    {
                        key = snapshot.child("key").getValue().toString().trim();
                    }

                    FirebaseInstanceId.getInstance().getInstanceId().addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
                        @Override
                        public void onComplete(@NonNull Task<InstanceIdResult> task) {
                            // Get new Instance ID token
                            String token = task.getResult().getToken();
                            HashMap<String,Object> map = new HashMap<>();
                            map.put("token",token);
                            FirebaseDatabase.getInstance().getReference("FirebaseTokens").child(key).updateChildren(map)

                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {

                                        }
                                    });

                        }
                    });



                }
                else
                {
                    FirebaseInstanceId.getInstance().getInstanceId().addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
                        @Override
                        public void onComplete(@NonNull Task<InstanceIdResult> task) {
                            // Get new Instance ID token
                            DatabaseReference db;
                            db = FirebaseDatabase.getInstance().getReference("FirebaseTokens");
                            String key = db.push().getKey();
                            String token = task.getResult().getToken();
                            HashMap<String,Object> map = new HashMap<>();
                            map.put("Email",email);
                            map.put("token",token);
                            map.put("key",key);
                            db.child(key)
                                    .setValue(map)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {

                                        }
                                    });

                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }
}