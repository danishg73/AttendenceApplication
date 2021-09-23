package com.sahm.attendanceapp;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;
import com.squareup.okhttp.MediaType;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Messenger extends AppCompatActivity {
    Button send;
    EditText input;
    ListView listView;
    TextView title_name;
    String Receiver_email,email,Receiver_name,sender_name,name,Receiver_username;
    SharedPreferences sp;
    private String loggedInUserName = "",sender_username="";
    DatabaseReference databaseReference, databaseReference_send;
    MessageAdapter adapter_main;
    ProgressDialog progressDialog;
    String type="1";
    String sender_user[], receiver_user[];
    String sender_key;
    private RequestQueue mRequestQue;
    private String URL = "https://fcm.googleapis.com/fcm/send";
    ImageView backarrow,msg;
    String Message;
    String token;
    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(LocaleHelper.onAttach(base));
    }


    public static final MediaType JSON
            = MediaType.parse("application/json; charset=utf-8");
    List<Chat_list> chat = new ArrayList<>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_messenger);
        send = findViewById(R.id.send);
        input = findViewById(R.id.input);
        listView = findViewById(R.id.list);
        backarrow=findViewById(R.id.backarrow);
        title_name=findViewById(R.id.title_name);
        sp=getSharedPreferences("login",MODE_PRIVATE);
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage(getResources().getString(R.string.Loading));
        progressDialog.setCancelable(false);
        progressDialog.show();


        Receiver_name= getIntent().getStringExtra("target_name");
        Receiver_email= getIntent().getStringExtra("target_email");
        Receiver_username= getIntent().getStringExtra("target_username");
//        Toast.makeText(this, "Receiver_email:"+Receiver_email+"\nReceiver name: "+Receiver_name+"\nReceiver username:"+Receiver_username, Toast.LENGTH_SHORT).show();
        //receiver_user = Receiver_email.split("\\.");
        title_name.setText(Receiver_name);
        get_token();
        if( sp.contains("user_type"))
        {
            loggedInUserName  =  sp.getString("email",email).trim();
            sender_username  =  sp.getString("username","").trim();
            sender_name  =  sp.getString("name",name).trim();
            sender_user= loggedInUserName.split("\\.");
        }
        else
            {
                Intent intent = new Intent(Messenger.this, LoginDashboardActivity.class);
                startActivity(intent);
            }
            if(type.equals("1"))
            {
                if(Receiver_username == null)
                {

                    Query q1 = FirebaseDatabase.getInstance().getReference("userinfo").orderByChild("email").equalTo(Receiver_email);
                    q1.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if(dataSnapshot.exists())
                            {
                                for (DataSnapshot snapshot: dataSnapshot.getChildren())
                                {


                                    Receiver_name = snapshot.child("name").getValue().toString();
                                    Receiver_username = snapshot.child("username").getValue().toString().trim();

                                    sender_key = sender_username+"~"+Receiver_username;
                                    title_name.setText(Receiver_name);
                                    showAllOldMessages(sender_key);


                                }
                            }

                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError)
                        {
                            progressDialog.dismiss();
                            Toast.makeText(Messenger.this, getResources().getString(R.string.Error), Toast.LENGTH_SHORT).show();


                        }
                    });

                }

                else
                {
                    sender_key = sender_username+"~"+Receiver_username;
                    showAllOldMessages(sender_key);
                }



            }
            else
            {

            }

        mRequestQue = Volley.newRequestQueue(this);
        FirebaseMessaging.getInstance().subscribeToTopic("Employee");

            send.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Message = input.getText().toString();
                    if(TextUtils.isEmpty(Message))
                    {
                        Toast.makeText(Messenger.this, getResources().getString(R.string.Please_enter_some_text), Toast.LENGTH_SHORT).show();
                    }
                    else
                    {

                        try {
                            InputMethodManager imm = (InputMethodManager)getSystemService(getBaseContext().INPUT_METHOD_SERVICE);
                            imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
                        } catch (Exception e) {
                            // TODO: handle exception
                        }

                        databaseReference_send = FirebaseDatabase.getInstance().getReference("Chats");

                        Date currentTime = Calendar.getInstance().getTime();
                        String curr_time=currentTime.toString();
                                HashMap<String,Object> map = new HashMap<>();
                                map.put("Sender_email",loggedInUserName);
                                map.put("Receiver_email",Receiver_email);
                                map.put("Time", curr_time);
                                map.put("Message",Message);
                                sendNotification();

                                FirebaseDatabase.getInstance().getReference("Chats").child(sender_key).push()
                                        .setValue(map)
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {


                                            }
                                        });
                                input.setText("");

                    }
                }
            });
            backarrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                 Messenger.super.onBackPressed();

            }
        });

    }
    private void showAllOldMessages(String key) {
        databaseReference =   FirebaseDatabase.getInstance().getReference("Chats");
        DatabaseReference db1 = databaseReference.child(key);
        db1.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {

                    chat.clear();

                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {

//                        Userlist.add(snapshot.getValue().toString());
                        Chat_list chat_list = new Chat_list();
                        chat_list.Sender_Email = snapshot.child("Sender_email").getValue().toString();
                        chat_list.Message = snapshot.child("Message").getValue().toString();
                        chat_list.Time =snapshot .child("Time").getValue().toString();
                        chat_list.Receiver_Email =snapshot.child("Receiver_email").getValue().toString();
                        chat.add(chat_list);
                        chat_list= null;

                    }
                    adapter_main = new MessageAdapter(chat,getBaseContext(),Messenger.this);
                    listView.setAdapter(adapter_main);
                    adapter_main.notifyDataSetChanged();
                    progressDialog.dismiss();
                }
                else
                {
                    if(type.equals("2"))
                    {
                    //    Toast.makeText(Messenger.this, "No Message Found", Toast.LENGTH_SHORT).show();
                        progressDialog.dismiss();
                    }
                    else
                    {
                        sender_key =Receiver_username+"~"+sender_username;

                        db1.removeEventListener(this);
                        type="2";
                        showAllOldMessages( sender_key);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {


            }
        });
    }

    public String getLoggedInUserName() {
        return loggedInUserName;
    }

    private void sendNotification() {

        JSONObject json = new JSONObject();
        try {
            json.put("to", token);
            json.put("content_available", true);

            JSONObject notificationObj = new JSONObject();
            notificationObj.put("title",sender_name);
            notificationObj.put("body",Message);
            notificationObj.put("click_action","OPEN_ACTIVITY_1");
            JSONObject extraData = new JSONObject();
            extraData.put("sender_username",sender_username);
            extraData.put("target_email",loggedInUserName);

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

    public void get_token()
    {
        Query query4 =FirebaseDatabase.getInstance().getReference("FirebaseTokens").orderByChild("Email").equalTo(Receiver_email);
        query4.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {

                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        token = snapshot.child("token").getValue().toString().trim();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }



}
