package com.sahm.attendanceapp;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class Userlist_message extends AppCompatActivity {

    ImageView backarrow;
    TextView manager_name;
    String m_name,m_username;
    LinearLayout manager_ll;
    Em_list_adapter adapter_main;
    List<Em_list> array_employeelist = new ArrayList<>();
    ListView listBar;
    ProgressDialog pd1;
    SharedPreferences sp;
    String usertype,manager_email;

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(LocaleHelper.onAttach(base));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sp=getSharedPreferences("login",MODE_PRIVATE);

        if( sp.contains("user_type")){

            String v=  sp.getString("user_type",usertype).trim();
            if (v.equals("manager"))
            {
                Intent intent = new Intent(Userlist_message.this, LoginDashboardActivity.class);
                startActivity(intent);
                finish();

            }
            else if (v.equals("employee"))
            {


            }
            else{
                Intent intent = new Intent(Userlist_message.this, LoginDashboardActivity.class);
                startActivity(intent);
                finish();
            }
        }

        setContentView(R.layout.activity_userlist_message);
        backarrow = findViewById(R.id.backarrow);
        listBar = findViewById(R.id.listview);
        manager_ll = findViewById(R.id.manager_ll);
        manager_name = findViewById(R.id.manager_name);
        pd1 = new ProgressDialog(this);
        pd1.setMessage(getResources().getString(R.string.Fetching_Data));
        pd1.setCancelable(false);

        manager_email = getIntent().getStringExtra("manager_email");
        pd1.show();
        Query query1 = FirebaseDatabase.getInstance().getReference("userinfo").orderByChild("email").equalTo(manager_email);
        query1.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {

                        m_name = snapshot.child("name").getValue().toString();
                        m_username =snapshot.child("username").getValue().toString();
                        manager_name.setText(m_name);
                        get_employee_data();

                    }

                }
                else
                {
                    Toast.makeText(Userlist_message.this,  getResources().getString(R.string.Error), Toast.LENGTH_SHORT).show();
                    get_employee_data();
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


        manager_ll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {

                Intent intent = new Intent(Userlist_message.this, Messenger.class);
                intent.putExtra("target_email",manager_email);
                intent.putExtra("target_name",m_name);
                intent.putExtra("target_username",m_username);
                startActivity(intent);


            }
        });


        listBar.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                //like this:

                    Intent intent = new Intent(Userlist_message.this, Messenger.class);
                    intent.putExtra("target_email",array_employeelist.get(position).email);
                    intent.putExtra("target_name",array_employeelist.get(position).name);
                    intent.putExtra("target_username",array_employeelist.get(position).username);
                    startActivity(intent);



            }
        });
        backarrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                Userlist_message.super.onBackPressed();

            }
        });

    }
    public void get_employee_data()
    {

        Query query = FirebaseDatabase.getInstance().getReference("Employee_data").orderByChild("manager").equalTo(manager_email);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {

                        Em_list em_list = new Em_list();
                        em_list.name = snapshot.child("name").getValue().toString();
                        em_list.email = snapshot.child("email").getValue().toString();
                        em_list.phone =snapshot.child("phone").getValue().toString();
                        em_list.username =snapshot.child("username").getValue().toString();
                        array_employeelist.add(em_list);
                        em_list= null;
                    }
                    adapter_main = new Em_list_adapter(array_employeelist,getBaseContext(),Userlist_message.this);
                    listBar.setAdapter(adapter_main);
                    pd1.dismiss();

                }
                else
                {
                    Toast.makeText(Userlist_message.this,  getResources().getString(R.string.NO_data_found), Toast.LENGTH_SHORT).show();
                    pd1.dismiss();
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }
}
