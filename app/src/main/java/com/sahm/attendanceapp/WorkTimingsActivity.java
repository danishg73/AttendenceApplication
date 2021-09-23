package com.sahm.attendanceapp;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.sahm.attendanceapp.Model.Timings;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class WorkTimingsActivity extends AppCompatActivity implements TimePickerDialog.OnTimeSetListener {

    RecyclerView rv;
    ImageView backarroow;
    TextView starttime, endtime,shift_name;
    String mon="",tues="",wed="",thurs="",fri="",sat="",sun="";
    TextView Monday, Tuesday,Wednesday,Thursday,Friday,Saturday,Sunday;
    ProgressDialog progressDialog;
    DatabaseReference databaseReference;
    Button btntimeadded, cancel;
    LinearLayout txttimeadded;
    String stime,shiftname,shift_day;
    String etime;
    String days;
    final Calendar myCalendar = Calendar.getInstance();
    private static final String TIME_PATTERN = "HH:mm";
    private DateFormat dateFormat;
    private SimpleDateFormat timeFormat;
    private Calendar calendar;
    private List<Timings> mUploads;
    String key,x;

    TimePickerDialog timePickerDialog;
    int currentHour;
    int currentMinute;
    String amPm;
    SharedPreferences sp;
    String usertype,Manager_email,manager;
    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(LocaleHelper.onAttach(base));
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        sp = this.getSharedPreferences("login", MODE_PRIVATE);
        if( sp.contains("user_type")){
            String v=  sp.getString("user_type",usertype).trim();
            if (v.equals("manager"))
            {
                Manager_email = sp.getString("email",manager).trim();

            }
            else if (v.equals("employee")){
                SharedPreferences.Editor e=sp.edit();
                e.clear();
                e.commit();
                Intent intent = new Intent(WorkTimingsActivity.this, LoginDashboardActivity.class);
                startActivity(intent);
                finish();
            }
            else{
                Intent intent = new Intent(WorkTimingsActivity.this, LoginDashboardActivity.class);
                startActivity(intent);
            }
        }

        setContentView(R.layout.activity_work_timings);

        Query query = FirebaseDatabase.getInstance().getReference("Timings").orderByChild("manager").equalTo(Manager_email);
        databaseReference=FirebaseDatabase.getInstance().getReference("Timings");
        mUploads = new ArrayList<>();

        calendar = Calendar.getInstance();
        dateFormat = DateFormat.getDateInstance(DateFormat.LONG, Locale.getDefault());
        timeFormat = new SimpleDateFormat(TIME_PATTERN, Locale.getDefault());

        rv = findViewById(R.id.rv);
        rv.setLayoutManager(new LinearLayoutManager(this));
//        CustomAdapter customAdapter = new CustomAdapter();
//        rv.setAdapter(customAdapter);

        backarroow = findViewById(R.id.backarrow);
        txttimeadded = findViewById(R.id.ll_addtime);
        progressDialog=new ProgressDialog(this);


        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                mUploads.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Timings object = new Timings();
                    object.setStart_time(snapshot.child("start_time").getValue().toString());
                    object.setEnd_time(snapshot.child("end_time").getValue().toString());
                    object.setDays(snapshot.child("days").getValue().toString());
                    object.setShiftname(snapshot.child("shiftname").getValue().toString());
                    mUploads.add(object);
                }
                CustomAdapter customAdapter = new CustomAdapter(WorkTimingsActivity.this,mUploads);
                rv.setAdapter(customAdapter);


            }
            @Override
            public void onCancelled(DatabaseError databaseError)
            {
                Toast.makeText(WorkTimingsActivity.this, getResources().getString(R.string.Database_error)+databaseError, Toast.LENGTH_SHORT).show();

            }
        });

        backarroow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                WorkTimingsActivity.super.onBackPressed();
            }
        });
        final TimePickerDialog.OnTimeSetListener time = new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                myCalendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                myCalendar.set(Calendar.MINUTE, minute);


            }
        };

        txttimeadded.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                final Dialog customDialog = new Dialog(WorkTimingsActivity.this);
                // customDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
                customDialog.setContentView(R.layout.custom_work_timings);
                int width = (int) (getResources().getDisplayMetrics().widthPixels * 0.90);
//                int height = (int) (getResources().getDisplayMetrics().heightPixels * 0.46);

                customDialog.getWindow().setLayout(width, ViewGroup.LayoutParams.WRAP_CONTENT);
//                customDialog.getWindow().setLayout(width, height);
                customDialog.setCancelable(false);

                starttime=customDialog.findViewById(R.id.starttime);
                endtime=customDialog.findViewById(R.id.endtime);
                Monday=customDialog.findViewById(R.id.monday);
                Tuesday=customDialog.findViewById(R.id.tuesday);
                Wednesday=customDialog.findViewById(R.id.wednesday);
                Thursday=customDialog.findViewById(R.id.thursday);
                Friday=customDialog.findViewById(R.id.friday);
                Saturday=customDialog.findViewById(R.id.saturday);
                Sunday=customDialog.findViewById(R.id.sunday);
                shift_name=customDialog.findViewById(R.id.shift_name);
                btntimeadded=customDialog.findViewById(R.id.timeadded);
                cancel=customDialog.findViewById(R.id.cancel);

                Monday.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v)
                    {
                        if(mon.equals("Monday"))
                        {
                            mon="";
                            Monday.setBackgroundResource(R.drawable.days);
                        }
                        else
                        {
                            Monday.setBackgroundResource(R.drawable.days_selected);
                            mon="Monday";
                        }

                    }
                });
                Tuesday.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v)
                    {

                        if(tues.equals("Tuesday"))
                        {
                            tues="";
                            Tuesday.setBackgroundResource(R.drawable.days);
                        }
                        else
                        {
                            Tuesday.setBackgroundResource(R.drawable.days_selected);
                            tues="Tuesday";
                        }
                    }
                });
                Wednesday.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v)
                    {

                        if(wed.equals("Wednesday"))
                        {
                            wed="";
                            Wednesday.setBackgroundResource(R.drawable.days);
                        }
                        else
                        {
                            Wednesday.setBackgroundResource(R.drawable.days_selected);
                            wed="Wednesday";
                        }
                    }
                });
                Thursday.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v)
                    {

                        if(thurs.equals("Thursday"))
                        {
                            thurs="";
                            Thursday.setBackgroundResource(R.drawable.days);
                        }
                        else
                        {
                            Thursday.setBackgroundResource(R.drawable.days_selected);
                            thurs="Thursday";
                        }
                    }
                });
                Friday.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v)
                    {

                        if(fri.equals("Friday"))
                        {
                            fri="";
                            Friday.setBackgroundResource(R.drawable.days);
                        }
                        else
                        {
                            Friday.setBackgroundResource(R.drawable.days_selected);
                            fri="Friday";
                        }
                    }
                });
                Saturday.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v)
                    {

                        if(sat.equals("Saturday"))
                        {
                            sat="";
                            Saturday.setBackgroundResource(R.drawable.days);
                        }
                        else
                        {
                            Saturday.setBackgroundResource(R.drawable.days_selected);
                            sat="Saturday";
                        }
                    }
                });
                Sunday.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v)
                    {

                        if(sun.equals("Sunday"))
                        {
                            sun="";
                            Sunday.setBackgroundResource(R.drawable.days);
                        }
                        else
                        {
                            Sunday.setBackgroundResource(R.drawable.days_selected);
                            sun="Sunday";
                        }
                    }
                });


                starttime.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v)
                    {

                        calendar = Calendar.getInstance();
                        currentHour = calendar.get(Calendar.HOUR_OF_DAY);
                        currentMinute = calendar.get(Calendar.MINUTE);

                        timePickerDialog = new TimePickerDialog(WorkTimingsActivity.this, new TimePickerDialog.OnTimeSetListener() {
                            @Override
                            public void onTimeSet(TimePicker timePicker, int hourOfDay, int minutes) {
                                if (hourOfDay >= 12) {
                                    if(hourOfDay>12)
                                    {
                                        hourOfDay = hourOfDay-12;
                                    }
                                    amPm = " pm";
                                } else {
                                    amPm = " am";
                                }
                                starttime.setText(String.format("%02d:%02d", hourOfDay, minutes) + amPm);
                            }
                        }, currentHour, currentMinute, false);

                        timePickerDialog.show();


                       // new TimePickerDialog(WorkTimingsActivity.this, WorkTimingsActivity.this, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true).show();
                    }
                });
                endtime.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v)
                    {
                        calendar = Calendar.getInstance();
                        currentHour = calendar.get(Calendar.HOUR_OF_DAY);
                        currentMinute = calendar.get(Calendar.MINUTE);

                        timePickerDialog = new TimePickerDialog(WorkTimingsActivity.this, new TimePickerDialog.OnTimeSetListener() {
                            @Override
                            public void onTimeSet(TimePicker timePicker, int hourOfDay, int minutes) {
                                if (hourOfDay >= 12) {
                                    if(hourOfDay>12)
                                    {
                                        hourOfDay = hourOfDay-12;
                                    }
                                    amPm = " pm";
                                } else {
                                    amPm = " am";
                                }
                                endtime.setText(String.format("%02d:%02d", hourOfDay, minutes) + amPm);
                            }
                        }, currentHour, currentMinute, false);

                        timePickerDialog.show();



                        //  new TimePickerDialog(WorkTimingsActivity.this, WorkTimingsActivity.this, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true).show();
                    }
                });
                cancel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        customDialog.dismiss();
                    }
                });


                btntimeadded.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v)
                    {
                        days="";
                        stime = starttime.getText().toString();
                        etime = endtime.getText().toString();

                        try {
                             x= time_diff(stime,etime);
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                        shiftname = shift_name.getText().toString();
                        if(!mon.equals(""))
                        {
                            days =mon+", ";
                        }
                        if(!tues.equals(""))
                        {
                            days = days+tues+", ";
                        }
                        if(!wed.equals(""))
                        {
                            days = days+wed+", ";
                        }
                        if(!thurs.equals(""))
                        {
                            days = days+thurs+", ";
                        }
                        if(!fri.equals(""))
                        {
                            days = days+fri+", ";
                        }
                        if(!sat.equals(""))
                        {
                            days = days+sat+", ";
                        }
                        if(!sun.equals(""))
                        {
                            days = days+sun;
                        }
                        if (TextUtils.isEmpty(stime) && TextUtils.isEmpty(etime) && TextUtils.isEmpty(days)&& TextUtils.isEmpty(shiftname)) {
                            Toast.makeText(WorkTimingsActivity.this, getResources().getString(R.string.All_fields_are_required), Toast.LENGTH_SHORT).show();
                        }

                        else
                            {

                                progressDialog.setTitle(getResources().getString(R.string.Please_wait));
                                progressDialog.show();
                                key=databaseReference.push().getKey();
                                Timings addEmployees=new Timings(key,shiftname,stime,etime,days,Manager_email);
                                databaseReference.child(key).setValue(addEmployees, new DatabaseReference.CompletionListener() {
                                    @Override
                                    public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference)
                                    {
                                        progressDialog.dismiss();
                                        Toast.makeText(WorkTimingsActivity.this,
                                                getResources().getString(R.string.Time_Added)+"\n"+
                                                getResources().getString(R.string.Total_Duty_Time)+x, Toast.LENGTH_SHORT).show();
                                        Toast.makeText(WorkTimingsActivity.this, "Time Added", Toast.LENGTH_SHORT).show();
                                        customDialog.dismiss();
                                        starttime.setText("");
                                        endtime.setText("");

                                    }
                                });
                            }

                    }
                });


                customDialog.setCancelable(true);
                customDialog.show();

            }
        });

    }

    @Override
    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
        calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
        calendar.set(Calendar.MINUTE, minute);
            starttime.setText(timeFormat.format(calendar.getTime()));
            endtime.setText(timeFormat.format(calendar.getTime()));


    }

    private class CustomAdapter extends RecyclerView.Adapter<CustomAdapter.MyAdapter> {

        private LayoutInflater inflater;
        private Context mContext;
        private List<Timings> mUploads;

        public CustomAdapter(Context context, List<Timings> uploads) {
            mContext = context;
            mUploads = uploads;
        }

        @NonNull
        @Override
        public CustomAdapter.MyAdapter onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            if (inflater == null) {
                inflater = LayoutInflater.from(parent.getContext());
            }
            View view = inflater.inflate(R.layout.work_timings, parent, false);
            return new CustomAdapter.MyAdapter(view);
        }

        @Override
        public void onBindViewHolder(@NonNull MyAdapter holder, int position)
        {
            Timings uploadCurrent = mUploads.get(position);
            holder.intime.setText(uploadCurrent.getStart_time());
            holder.outtime.setText(uploadCurrent.getEnd_time());
            shift_day =uploadCurrent.getDays().trim();
            if(shift_day.contains("Monday"))
            {
                holder.mon.setBackgroundResource(R.drawable.days_selected);
            }
            if(shift_day.contains("Tuesday"))
            {
                holder.tues.setBackgroundResource(R.drawable.days_selected);
            }
            if(shift_day.contains("Wednesday"))
            {

                holder.wed.setBackgroundResource(R.drawable.days_selected);
            }
            if(shift_day.contains("Thursday"))
            {

                holder.thurs.setBackgroundResource(R.drawable.days_selected);
            }
            if(shift_day.contains("Friday"))
            {

                holder.fri.setBackgroundResource(R.drawable.days_selected);
            }
            if(shift_day.contains("Saturday"))
            {
                holder.sat.setBackgroundResource(R.drawable.days_selected);
            }
            if(shift_day.contains("Sunday"))
            {

                holder.sun.setBackgroundResource(R.drawable.days_selected);
            }
            holder.teamname.setText(uploadCurrent.getShiftname());

        }

        @Override
        public int getItemCount() {
           return mUploads.size();
        }

        public class MyAdapter extends RecyclerView.ViewHolder
        {
            public TextView intime,outtime,teamname;
            public TextView mon,tues,wed,thurs,fri,sat,sun;

            public MyAdapter(@NonNull View itemView) {
                super(itemView);

                intime=itemView.findViewById(R.id.entertime);
                outtime=itemView.findViewById(R.id.outtime);
                teamname=itemView.findViewById(R.id.team_name);
                mon=itemView.findViewById(R.id.monday);
                tues=itemView.findViewById(R.id.tuesday);
                wed=itemView.findViewById(R.id.wednesday);
                thurs=itemView.findViewById(R.id.thursday);
                fri=itemView.findViewById(R.id.friday);
                sat=itemView.findViewById(R.id.saturday);
                sun=itemView.findViewById(R.id.sunday);
            }
        }
    }
    public String time_diff(String start_time, String end_time) throws ParseException {

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("hh:mm a");
        DateFormat df = DateFormat.getInstance();
        Date date1 = simpleDateFormat.parse(start_time);
        Date date2 = simpleDateFormat.parse(end_time);
        long difference = date2.getTime() - date1.getTime();


        int day = (int) (difference / (1000*60*60*24));
        int hours = (int) ((difference - (1000*60*60*24*day)) / (1000*60*60));
        int min = (int) (difference - (1000*60*60*24*day) - (1000*60*60*hours)) / (1000*60);
        if(hours<0 )
        {
            hours = hours+24;
        }
        if(min<0)
        {
            min=min+60;
            if(hours==0)
            {
                hours= hours+23;
            }
        }

      return " "+hours+" Hr :"+min+" min";
    }


}
