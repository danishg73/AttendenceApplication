package com.sahm.attendanceapp;

import android.app.Activity;
import android.content.Context;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

public class Em_list_adapter extends BaseAdapter {
    Context context;
    List<Em_list> valueList;
    static int position = 0;
    Userlist_message mainActivity2;
    Manager_Dashboard mainActivity3;
    pdf_report mainActivity4;
    String show="", manager_username;
    String today_date_time, x[], current_time;
    Calendar c = Calendar.getInstance();
    ViewEM finalViewItem1;






    public Em_list_adapter(List<Em_list> listValue, Context context, Manager_Dashboard mainActivity3, String show, String manager_username) {
        this.context = context;
        this.valueList = listValue;
        this.mainActivity3 = mainActivity3;
        this.show = show;
        this.manager_username = manager_username;
    }

    public Em_list_adapter(List<Em_list> listValue, Context context, Userlist_message mainActivity2) {
        this.context = context;
        this.valueList = listValue;
        this.mainActivity2 = mainActivity2;
    }
    public Em_list_adapter(List<Em_list> listValue, Context context, pdf_report mainActivity4 ) {
        this.context = context;
        this.valueList = listValue;
        this.mainActivity4 = mainActivity4;
        this.show = show;
        this.manager_username = manager_username;
    }


    @Override
    public int getCount() {
        return this.valueList.size();
    }

    @Override
    public Object getItem(int position) {
        return this.valueList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemViewType(int position) {

        return position;
    }

    @Override
    public int getViewTypeCount() {

        if (getCount() > 0) {
            return getCount();
        } else {
            return super.getViewTypeCount();
        }
    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent)
    {
        ViewEM viewItem = null;
        Em_list_adapter.position=position;

        if(convertView == null)
        {
            viewItem = new ViewEM();

            LayoutInflater layoutInfiater = (LayoutInflater)this.context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);

            convertView = layoutInfiater.inflate(R.layout.custom_add_employee, null);
            viewItem.name = convertView.findViewById(R.id.showemployee);
            viewItem.button = convertView.findViewById(R.id.punch);

            convertView.setTag(viewItem);
            viewItem.name.setText(valueList.get(position).name);

            if(show.equals("all"))
            {
                viewItem.button.setVisibility(View.INVISIBLE);
            }
            else if(show.equals("punch"))
            {
               viewItem.button.setBackgroundResource(R.drawable.buttondesign_red);
               viewItem.button.setText(R.string.Un_punched);
               viewItem.button.setVisibility(View.VISIBLE);

            }
            else if(show.equals("unpunch"))
            {
                viewItem.button.setVisibility(View.VISIBLE);
                viewItem.button.setBackgroundResource(R.drawable.buttondesign);
                viewItem.button.setText(R.string.Punch);

            }

            else
                {
                viewItem.button.setVisibility(View.INVISIBLE);
                }


              finalViewItem1 = viewItem;


            ViewEM finalViewItem = viewItem;
            viewItem.button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view)
                {
                    if(show.equals("punch"))
                    {
                        unmarkattendance(valueList.get(position).username.toString(),finalViewItem.button);
                    }
                    else if(show.equals("unpunch"))
                    {
                        String u_name =valueList.get(position).username.toString().trim();
                        String u_email =valueList.get(position).email.toString().trim();
                        markattendance(u_name,u_email,finalViewItem.button);


                    }
                }
            });





        }
        else
        {
            viewItem = (ViewEM) convertView.getTag();
        }

        return convertView;
    }

    private void markattendance(String u_name, String u_email, TextView t)

    {
        Date date = new Date();
        String Day = DateFormat.format("EEEE", date.getTime()).toString();
        Calendar c2 = Calendar.getInstance();
        SimpleDateFormat month_date = new SimpleDateFormat("MMMM");
        String month_name = month_date.format(c2.getTime());
        SimpleDateFormat df = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");// HH:mm:ss");
        today_date_time = df.format(c.getTime());
        x = today_date_time.split("\\s+");
        current_time =x[1];
        HashMap<String,Object> map = new HashMap<>();
        map.put("day",Day);
        map.put("date",x[0]);
        map.put("month",month_name);
        map.put("last_update","not in use");
        map.put("Punch_out_time","not added");
        map.put("Punched","yes");
        map.put("punch_in_time",current_time);
        map.put("employee_email",u_email);
        DatabaseReference db;
        db = FirebaseDatabase.getInstance().getReference("Attendance").child(manager_username).child(u_name).push();
        String key= db.getKey();
        map.put("key",key);
//        FirebaseDatabase.getInstance().getReference().child("Employee_Task").push()
        db.setValue(map)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {

                        if(task.isSuccessful())
                        {
                            Toast.makeText(context,  context.getResources().getString(R.string.Attendance_Marked), Toast.LENGTH_SHORT).show();
                            t.setBackgroundResource(R.drawable.buttondesign_red);
                            t.setText(R.string.Un_punched);
//                            show="punch";
//                            valueList.remove(position);
                            notifyDataSetChanged();


                        }
                        else
                        {
                            Toast.makeText(context,  context.getResources().getString(R.string.Error), Toast.LENGTH_SHORT).show();
                        }

                    }
                });
        HashMap<String,Object> map2 = new HashMap<>();
        map2.put("Punched","yes");
        map2.put("punch_in_time",current_time);
        Query query = FirebaseDatabase.getInstance().getReference().child("Employee_data").orderByChild("username").equalTo(u_name);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists())
                {
                    for (DataSnapshot snapshot: dataSnapshot.getChildren())
                    {
                        snapshot.getRef().updateChildren(map2);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });



    }


    private void unmarkattendance(String u_name,TextView t)
    {


//        progressDialog2.show();
        SimpleDateFormat df = new SimpleDateFormat("HH:mm:ss");// HH:mm:ss");
        current_time = df.format(c.getTime());
//        x = today_date_time.split("\\s+");
//        current_time =x[1];
        HashMap<String,Object> map = new HashMap<>();
        map.put("Punch_out_time",current_time);
        map.put("Punched","no");
        Query query = FirebaseDatabase.getInstance().getReference("Attendance")
                .child(manager_username).child(u_name).orderByChild("Punched").equalTo("yes");

//        FirebaseDatabase.getInstance().getReference().child("Employee_Task").push()
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            {
                if(dataSnapshot.exists())
                {
                    for (DataSnapshot dataSnapshot1: dataSnapshot.getChildren())
                    {
                        dataSnapshot1.getRef().updateChildren(map);
//                        show="unpunch";
//                        valueList.remove(position);
//                        notifyDataSetChanged();
//                        progressDialog2.dismiss();
                        delete_data(u_name,t);
                    }

                }
                else
                {
                    Toast.makeText(context, R.string.No_Marked_Attendance_found, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        HashMap<String,Object> map2 = new HashMap<>();
        map2.put("Punched","no");
        Query query2 = FirebaseDatabase.getInstance().getReference().child("Employee_data").orderByChild("username").equalTo(u_name);
        query2.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists())
                {
                    for (DataSnapshot snapshot: dataSnapshot.getChildren())
                    {
                        snapshot.getRef().updateChildren(map2);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }




    public void delete_data(String u_name, TextView t)
    {
        Query query1;
        query1 = FirebaseDatabase.getInstance().getReference("Tracking").orderByChild("employee_username").equalTo(u_name);
        query1.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists())
                {
                    t.setBackgroundResource(R.drawable.buttondesign);
                    t.setText(R.string.Punch);
//                    show="unpunch";
                    dataSnapshot.getRef().removeValue();
                    Toast.makeText( context, R.string.Tracking_Stopped, Toast.LENGTH_SHORT).show();

                }
                else
                {
//                    Toast.makeText( context, R.string.No_Tracking_Found, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }
}

class ViewEM {
    TextView name;
    TextView button;
    TextView email;
    TextView phone;
    TextView shift;
    ImageView profilepic;
}

