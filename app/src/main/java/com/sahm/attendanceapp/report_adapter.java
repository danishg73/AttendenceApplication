package com.sahm.attendanceapp;

import android.app.Activity;
import android.app.DownloadManager;
import android.content.Context;
import android.net.Uri;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

public class report_adapter extends BaseAdapter {
    Context context;
    List<Task_list> valueList;
    static int position=0;
    Employee_reportview mainActivity;

    public report_adapter(List<Task_list> listValue, Context context, Employee_reportview mainActivity )
    {
        this.context = context;
        this.valueList = listValue;
        this.mainActivity=mainActivity;
    }


    @Override
    public int getCount()
    {
        return this.valueList.size();
    }

    @Override
    public Object getItem(int position)
    {
        return this.valueList.get(position);
    }

    @Override
    public long getItemId(int position)
    {
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
        View_report viewItem = null;
        task_list_adapter.position=position;

        if(convertView == null)
        {
            viewItem = new View_report();

            LayoutInflater layoutInfiater = (LayoutInflater)this.context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);

            convertView = layoutInfiater.inflate(R.layout.report_viewitem, null);
            viewItem.report_description = convertView.findViewById(R.id.report_description);
            viewItem.submitted_date = convertView.findViewById(R.id.submitted_on);
            viewItem.period = convertView.findViewById(R.id.report_period);
            viewItem.file_name = convertView.findViewById(R.id.attachment);

            viewItem.report_description.setText(valueList.get(position).task_description);
            viewItem.submitted_date.setText(valueList.get(position).submitted_date);
            viewItem.file_name.setText(valueList.get(position).file_name);
            viewItem.period.setText(valueList.get(position).period);
            convertView.setTag(viewItem);

        }
        else
        {
            viewItem = (View_report) convertView.getTag();
        }
        viewItem.file_name.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if( valueList.get(position).file_url.equals("No Attachments found!"))
                {
                    Toast.makeText(context, context.getResources().getString(R.string.NO_data_found), Toast.LENGTH_SHORT).show();
                }
                else {
                    DownloadManager downloadmanager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
                    Uri uri = Uri.parse(valueList.get(position).file_url);

                    DownloadManager.Request request = new DownloadManager.Request(uri);
                    request.setTitle(valueList.get(position).file_name);
                    request.setDescription(context.getResources().getString(R.string.Downloading));
                    request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
                    request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS,valueList.get(position).file_name);
                    downloadmanager.enqueue(request);
                }


            }
        });







        return convertView;
    }



}

class View_report {
    TextView report_description;
    TextView submitted_date;
    TextView file_name;
    TextView period;
}

