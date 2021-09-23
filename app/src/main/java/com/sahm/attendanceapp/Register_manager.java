package com.sahm.attendanceapp;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.UserInfo;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.sahm.attendanceapp.Model.userinfo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Register_manager extends AppCompatActivity {
    TextView already_account;
    EditText regphone, regemail, regpass,fullname, uname;
    Button btnreg;
    ProgressDialog progressDialog;
    FirebaseAuth firebaseAuth;
    List<UserInfo> userInfos;
    DatabaseReference databaseReference;
    RadioGroup radioGroup;
    RadioButton manager, employee;
    String emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+";
    String mainKey,pass,name,phone;
    String type = "manager";
    String email,username;

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(LocaleHelper.onAttach(base));
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_manager);
        databaseReference = FirebaseDatabase.getInstance().getReference("userinfo");
        firebaseAuth = FirebaseAuth.getInstance();
        userInfos = new ArrayList<>();
        progressDialog = new ProgressDialog(this);
        regphone = findViewById(R.id.registerphonenumber);
        regemail = findViewById(R.id.registeremailaddress);
        fullname = findViewById(R.id.registerusername);
        uname = findViewById(R.id.username);
        regpass = findViewById(R.id.registerpassword);
        btnreg = findViewById(R.id.register);
        already_account = findViewById(R.id.alreadyhaveaccount);
        already_account.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Register_manager.this, Login_manager.class);
                startActivity(intent);
            }
        });

        btnreg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                phone = regphone.getText().toString();
                email = regemail.getText().toString();
                pass = regpass.getText().toString();
                name = fullname.getText().toString();
                username =uname.getText().toString();

                if (TextUtils.isEmpty(username) && TextUtils.isEmpty(phone) && TextUtils.isEmpty(email) && TextUtils.isEmpty(pass) && TextUtils.isEmpty(name) )
                {
                    Toast.makeText(Register_manager.this, getResources().getString(R.string.All_fields_are_required), Toast.LENGTH_SHORT).show();
                }
                else {

                    InputMethodManager imm = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
                    imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);

                    if(  email.matches(emailPattern) )
                    {

                        progressDialog.setTitle(getResources().getString(R.string.Please_wait));
                        progressDialog.setMessage(getResources().getString(R.string.Creating_Your_Account));
                        progressDialog.show();

                        firebaseAuth.createUserWithEmailAndPassword(email, pass)
                                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                    @Override
                                    public void onComplete(@NonNull Task<AuthResult> task) {
                                        if (task.isSuccessful()) {
                                            mainKey = databaseReference.push().getKey();
                                            userinfo userinfo = new userinfo(mainKey,username, phone, email, pass, type, "marked", name);
                                            databaseReference.child(mainKey).setValue(userinfo, new DatabaseReference.CompletionListener() {
                                                @Override
                                                public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {

                                                    HashMap<String,Object> map = new HashMap<>();
                                                    map.put("Currency","USD $");
                                                    map.put("manager_email",email);
                                                    map.put("manager_username",username);
                                                    FirebaseDatabase.getInstance().getReference("Currency").child(username)
                                                            .setValue(map).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                        @Override
                                                        public void onComplete(@NonNull Task<Void> task) {
                                                            if(task.isSuccessful())
                                                            {
                                                                lastactive();
                                                                setpaid();
                                                                progressDialog.dismiss();
                                                                Toast.makeText(Register_manager.this, getResources().getString(R.string.Register_Successfully), Toast.LENGTH_SHORT).show();
                                                                Intent intent = new Intent(Register_manager.this, Login_manager.class);
                                                                startActivity(intent);
                                                            }
                                                            else
                                                            {
                                                                progressDialog.dismiss();
                                                                Toast.makeText(Register_manager.this, getResources().getString(R.string.Error), Toast.LENGTH_SHORT).show();
                                                                Intent intent = new Intent(Register_manager.this, Login_manager.class);
                                                                startActivity(intent);
                                                            }
                                                        }
                                                    });

                                                }
                                            });


                                        } else
                                            {
                                            progressDialog.dismiss();
                                            Toast.makeText(Register_manager.this,  getResources().getString(R.string.Error), Toast.LENGTH_SHORT).show();
                                        }
                                    }

                                })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.d("test",e+"");
                                Toast.makeText(getBaseContext(), e+"", Toast.LENGTH_SHORT).show();
                            }
                        })
                        ;
                    }
                    else
                    {
                        Toast.makeText(Register_manager.this,  getResources().getString(R.string.Wrong_Email_Pattren), Toast.LENGTH_SHORT).show();
                    }

                }




            }
        });
    }
    public void  lastactive()
    {
//        LocationManager lm = (LocationManager)getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
//        Geocoder geocoder = new Geocoder(getApplicationContext());
//        for(String provider: lm.getAllProviders()) {
//            @SuppressWarnings("ResourceType") Location location = lm.getLastKnownLocation(provider);
//            if(location!=null) {
//                try {
//                    List<Address> addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
//                    if(addresses != null && addresses.size() > 0) {
//                        country_name = addresses.get(0).getCountryName();
//                        city_name = addresses.get(0).getLocality();
//                        break;
//                    }
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//            }
//        }



        HashMap<String,Object> map1 = new HashMap<>();
        map1.put("last_active","no");
        map1.put("country_name","NotUpdated ");
        map1.put("city_name","NotUpdated");

        Query q1 = FirebaseDatabase.getInstance().getReference("userinfo").orderByChild("username").equalTo(username);
        q1.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists())
                {
                    for (DataSnapshot snapshot: dataSnapshot.getChildren())
                    {
                        snapshot.getRef().updateChildren(map1);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
    public void setpaid()
    {
        HashMap<String,Object> map1 = new HashMap<>();
        map1.put("name",name);
        map1.put("email",email);
        map1.put("phone",phone);
        map1.put("username",username);
        map1.put("type","unpaid");
        map1.put("valid_till","unpaid");
        FirebaseDatabase.getInstance().getReference().child("paid").push().setValue(map1)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {

                        if(task.isSuccessful())
                        {
//                            Toast.makeText(RegisterActivity.this, "Work Submitted", Toast.LENGTH_SHORT).show();
                            progressDialog.dismiss();
                        }
                        else
                        {
//                            Toast.makeText(RegisterActivity.this, "Work Not Submitted", Toast.LENGTH_SHORT).show();
                            progressDialog.dismiss();
                        }

                    }
                });
    }
}