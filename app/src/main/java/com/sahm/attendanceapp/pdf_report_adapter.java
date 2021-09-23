
package com.sahm.attendanceapp;

        import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;

public class pdf_report_adapter extends BaseAdapter {
    Context context;
    List<Em_list> valueList;
    pdf_report mainActivity3;
    ViewEM2 finalViewItem1;




    public pdf_report_adapter(List<Em_list> listValue, Context context, pdf_report mainActivity3 ) {
        this.context = context;
        this.valueList = listValue;
        this.mainActivity3 = mainActivity3;
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
        ViewEM2 viewItem = null;
        Em_list_adapter.position=position;

        if(convertView == null)
        {
            viewItem = new ViewEM2();

            LayoutInflater layoutInfiater = (LayoutInflater)this.context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);

            convertView = layoutInfiater.inflate(R.layout.custom_pdf_report, null);
            viewItem.name = convertView.findViewById(R.id.showemployee);
            viewItem.attendance = convertView.findViewById(R.id.attendance);
            viewItem.task = convertView.findViewById(R.id.task);
            convertView.setTag(viewItem);
            viewItem.name.setText(valueList.get(position).name);
            finalViewItem1 = viewItem;


            ViewEM2 finalViewItem = viewItem;
            viewItem.attendance.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view)
                {


                    Intent intent = new Intent(mainActivity3, salary_calculate.class);
                    intent.putExtra("target_email", valueList.get(position).email.toString());
                    intent.putExtra("target_name",valueList.get(position).name.toString());
                    intent.putExtra("target_username",valueList.get(position).username.toString());
                    intent.putExtra("salary",valueList.get(position).salary.toString());
                    intent.putExtra("currency","nan");
                    intent.putExtra("duration",valueList.get(position).duration.toString());
                    mainActivity3.startActivity(intent);

                }
            });

            viewItem.task.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view)
                {


                    Intent intent = new Intent(mainActivity3, EmployeeProfileActivity.class);
                    intent.putExtra("target_email", valueList.get(position).email.toString());
                    intent.putExtra("target_name",valueList.get(position).name.toString());
                    intent.putExtra("target_username",valueList.get(position).username.toString());
                    intent.putExtra("salary",valueList.get(position).salary.toString());
                    intent.putExtra("currency","nan");
                    intent.putExtra("duration",valueList.get(position).duration.toString());
                    mainActivity3.startActivity(intent);

                }
            });




        }
        else
        {
            viewItem = (ViewEM2) convertView.getTag();
        }

        return convertView;
    }








}

class ViewEM2 {
    TextView attendance;
    TextView task;
    TextView name;
}


