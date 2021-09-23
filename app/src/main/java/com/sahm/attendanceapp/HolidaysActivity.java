package com.sahm.attendanceapp;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.sahm.attendanceapp.Model.Holiday;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class HolidaysActivity extends AppCompatActivity {

    RecyclerView rv;
    TextView addholiday;
    ProgressDialog progressDialog;
    DatabaseReference databaseReference;
    EditText  holiday_name;
    CustomAdapter customAdapter;
    TextView holiday_date;
    Button cancel,submit;
    LinearLayout ll_add;
    ImageView backarrow;
    private List<Holiday> mUploads;
    final Calendar myCalendar = Calendar.getInstance();
    String usertype, Manager_email,manager_username,manager_name,m_username,manager,m_name;
    SharedPreferences sp;
    String picked="no";
    String date_exist="no";
    Dialog customDialog;
    String h_date,h_name,month_name;
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

                Manager_email = sp.getString("email",manager).trim();
                manager_username = sp.getString("username",m_username).trim();
                manager_name = sp.getString("name",m_name).trim();
            }
            else if (v.equals("employee"))
            {
                Intent intent = new Intent(HolidaysActivity.this, Employee_Dashboard.class);
                startActivity(intent);
            }
            else{
                Intent intent = new Intent(HolidaysActivity.this, LoginDashboardActivity.class);
                startActivity(intent);
            }
        }
        setContentView(R.layout.activity_holidays);
        Query query =FirebaseDatabase.getInstance().getReference("Holidays").orderByChild("manager").equalTo(Manager_email);
        addholiday=findViewById(R.id.addholiday);
        mUploads = new ArrayList<>();
        progressDialog=new ProgressDialog(this);
        progressDialog.setMessage(getResources().getString(R.string.Fetching_Data));
        progressDialog.setCancelable(false);
        progressDialog.show();
        backarrow=findViewById(R.id.backarrow);
        ll_add=findViewById(R.id.ll_add);

        rv = findViewById(R.id.rv);
        rv.setLayoutManager(new LinearLayoutManager(this));


        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                mUploads.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Holiday object = new Holiday();
                    object.setPlace(snapshot.child("holiday_name").getValue().toString());
                    object.setDate(snapshot.child("holiday_date").getValue().toString());
                    object.setKey(snapshot.child("key").getValue().toString());
                    mUploads.add(object);
                }
                customAdapter = new CustomAdapter(HolidaysActivity.this,mUploads);
                rv.setAdapter(customAdapter);
                progressDialog.dismiss();


            }
            @Override
            public void onCancelled(DatabaseError databaseError)
            {
                progressDialog.dismiss();
                Toast.makeText(HolidaysActivity.this, getResources().getString(R.string.Database_error)+databaseError, Toast.LENGTH_SHORT).show();

            }
        });

        final DatePickerDialog.OnDateSetListener date = new DatePickerDialog.OnDateSetListener() {

            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear,
                                  int dayOfMonth) {
                // TODO Auto-generated method stub
                myCalendar.set(Calendar.YEAR, year);
                myCalendar.set(Calendar.MONTH, monthOfYear);
                myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

                updateLabel();
            }

        };

        backarrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                HolidaysActivity.super.onBackPressed();
            }
        });

        ll_add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {


                customDialog = new Dialog(HolidaysActivity.this);
                // customDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
                customDialog.setContentView(R.layout.custom_holidays);
                int width = (int) (getResources().getDisplayMetrics().widthPixels * 0.90);
                int height = (int) (getResources().getDisplayMetrics().heightPixels * 0.70);
                customDialog.getWindow().setLayout(width, ViewGroup.LayoutParams.WRAP_CONTENT);

                holiday_name=customDialog.findViewById(R.id.holiday_name);
                holiday_date=customDialog.findViewById(R.id.holiday_date);
                submit=customDialog.findViewById(R.id.submit);
                cancel=customDialog.findViewById(R.id.cancel);Calendar c = Calendar.getInstance();
                SimpleDateFormat month_date = new SimpleDateFormat("MMMM");
                month_name = month_date.format(c.getTime());


                holiday_date.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        // TODO Auto-generated method stub
                        new DatePickerDialog(HolidaysActivity.this, date, myCalendar
                                .get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                                myCalendar.get(Calendar.DAY_OF_MONTH)).show();
                    }
                });

                submit.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v)
                    {
                        h_date=holiday_date.getText().toString();
                        h_name=holiday_name.getText().toString();

                        if(TextUtils.isEmpty(h_name) || TextUtils.isEmpty(h_date))
                        {
                            Toast.makeText(HolidaysActivity.this, R.string.All_fields_are_required, Toast.LENGTH_SHORT).show();
                        }
                        else if(picked.equals("no"))
                        {

                            Toast.makeText(HolidaysActivity.this, R.string.Kindly_select_holiday_date, Toast.LENGTH_SHORT).show();

                        }
                        else
                            {
                                progressDialog.setMessage(getResources().getString(R.string.Processing));
                                progressDialog.setCancelable(false);
                                progressDialog.show();
                                date_exist="no";
                                Query query1 =FirebaseDatabase.getInstance().getReference("Holidays").orderByChild("manager").equalTo(Manager_email);
                                query1.addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot)
                                    {
                                        if (dataSnapshot.exists())
                                        {
                                            for(DataSnapshot snapshot:dataSnapshot.getChildren())
                                            {
                                                String d = snapshot.child("holiday_date").getValue().toString().trim();
                                                if(h_date.equals(d))
                                                {
                                                    date_exist="yes";
                                                    progressDialog.dismiss();
                                                    Toast.makeText(HolidaysActivity.this, R.string.This_date_is_already_exist, Toast.LENGTH_SHORT).show();
                                                }
                                            }
                                            if(date_exist.equals("no"))
                                            {
                                                insertdata();
                                            }
                                        }
                                        else
                                            {
                                                insertdata();
                                            }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) {
                                        Toast.makeText(HolidaysActivity.this, R.string.Error, Toast.LENGTH_SHORT).show();
                                        progressDialog.dismiss();

                                    }
                                });
                            }
                    }
                });
                cancel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        customDialog.dismiss();
                    }
                });
                customDialog.setCancelable(true);
                customDialog.show();
            }
        });
        ItemTouchHelper.SimpleCallback itemTouchHelperCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getAdapterPosition();
                customAdapter.notifyItemRemoved(position);
                String k= mUploads.get(position).getKey();
                FirebaseDatabase.getInstance().getReference("Holidays")
                        .child(k).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot)
                    {
                        if(dataSnapshot.exists())
                        {
                            dataSnapshot.getRef().removeValue();
                            Toast.makeText(HolidaysActivity.this, getResources().getString(R.string.Holiday_Deleted), Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
                mUploads.remove(position);
                // Row is swiped from recycler view
                // remove it from adapter
            }


        };

// attaching the touch helper to recycler view
        new ItemTouchHelper(itemTouchHelperCallback).attachToRecyclerView(rv);


    }

    private void updateLabel() {
        String myFormat = "dd-MM-yyyy"; //In which you need put here
        SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.US);
        picked="yes";
        holiday_date.setText(sdf.format(myCalendar.getTime()));
    }
    private void insertdata()
    {
        HashMap<String,Object> map = new HashMap<>();
        map.put("month",month_name);
        map.put("holiday_name",h_name);
        map.put("holiday_date",h_date);
        map.put("manager",Manager_email);
        map.put("manager_username",manager_username);
        databaseReference = FirebaseDatabase.getInstance().getReference("Holidays") .push();
        String key= databaseReference.getKey();
        map.put("key",key);
        databaseReference.setValue(map).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful())
                {
                    progressDialog.dismiss();
                    Toast.makeText(HolidaysActivity.this, R.string.Holiday_Added, Toast.LENGTH_SHORT).show();
                    customDialog.dismiss();
                }
                else {
                    Toast.makeText(HolidaysActivity.this, R.string.Error, Toast.LENGTH_SHORT).show();
                    progressDialog.dismiss();
                }

            }

        });

    }

    private class CustomAdapter extends RecyclerView.Adapter<CustomAdapter.MyAdapter>{

        private LayoutInflater inflater;
        private Context mContext;
        private List<Holiday> mUploads;

        public CustomAdapter(Context context, List<Holiday> uploads) {
            mContext = context;
            mUploads = uploads;
        }

        @NonNull
        @Override
        public MyAdapter onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            if (inflater==null){
                inflater=LayoutInflater.from(parent.getContext());
            }
            View view = inflater.inflate(R.layout.holidays_custom, parent, false);
            return new MyAdapter(view);
        }

        @Override
        public void onBindViewHolder(@NonNull MyAdapter holder, int position)
        {
            Holiday uploadCurrent = mUploads.get(position);
            holder.showplace.setText(uploadCurrent.getPlace());
            holder.showdate.setText(uploadCurrent.getDate());

        }

        @Override
        public int getItemCount() {
            return mUploads.size();
        }

        public class MyAdapter extends RecyclerView.ViewHolder
        {
            public TextView showplace;
            public TextView showdate;
            public MyAdapter(@NonNull View itemView) {

                super(itemView);

                 showplace=itemView.findViewById(R.id.showplace);
                 showdate=itemView.findViewById(R.id.showdate);
            }
        }
    }

}
