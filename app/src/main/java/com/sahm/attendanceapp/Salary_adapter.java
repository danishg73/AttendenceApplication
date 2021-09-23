package com.sahm.attendanceapp;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.List;

public class Salary_adapter extends BaseAdapter {
    Context context;
    List<Salary_list> valueList;
    static int position = 0;
    salary_calculate mainActivity;
    ViewEM finalViewItem1;
    int a= 0;



    public Salary_adapter(List<Salary_list> listValue, Context context, salary_calculate mainActivity) {
        this.context = context;
        this.valueList = listValue;
        this.mainActivity = mainActivity;
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
        Viewsalary viewItem = null;
        Em_list_adapter.position=position;

        if(convertView == null)
        {
            viewItem = new Viewsalary();

            LayoutInflater layoutInfiater = (LayoutInflater)this.context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);

            convertView = layoutInfiater.inflate(R.layout.item_salary, null);
            viewItem.date = convertView.findViewById(R.id.date);
            viewItem.punch_in = convertView.findViewById(R.id.checkin);
            viewItem.punch_out = convertView.findViewById(R.id.checkout);
            viewItem.working_hours = convertView.findViewById(R.id.total_time);
            viewItem.earning = convertView.findViewById(R.id.earning);
            viewItem.ll_main = convertView.findViewById(R.id.ll_main);
            if (a==0)
            {
                viewItem.ll_main.setBackgroundResource(R.drawable.salary_item_design);
                a=1;
            }
            else if(a==1)
            {

                viewItem.ll_main.setBackgroundResource(R.drawable.salary_item_design2);
                a=0;
            }

            convertView.setTag(viewItem);
            viewItem.date.setText(valueList.get(position).date);
            viewItem.punch_in.setText(valueList.get(position).punch_in);
            viewItem.punch_out.setText(valueList.get(position).punch_out);
            viewItem.working_hours.setText(valueList.get(position).working_hours);
            viewItem.earning.setText(valueList.get(position).earning);








        }
        else
        {
            viewItem = (Viewsalary) convertView.getTag();
        }

        return convertView;
    }



}

class Viewsalary {
    TextView date;
    TextView punch_in;
    TextView punch_out;
    TextView working_hours;
    TextView earning;
    LinearLayout ll_main;
}

