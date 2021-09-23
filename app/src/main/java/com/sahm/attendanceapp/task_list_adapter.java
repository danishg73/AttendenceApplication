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

import com.google.firebase.storage.FirebaseStorage;

import java.util.List;

public class task_list_adapter extends BaseAdapter {
    Context context;
    List<Task_list> valueList;
    static int position=0;
    task_view mainActivity;
    Employee_task mainActivity2;
    FirebaseStorage firebaseStorage;

    public task_list_adapter(List<Task_list> listValue, Context context, task_view mainActivity )
    {
        this.context = context;
        this.valueList = listValue;
        this.mainActivity=mainActivity;
    }

    public task_list_adapter(List<Task_list> listValue, Context context, Employee_task mainActivity2 )
    {
        this.context = context;
        this.valueList = listValue;
        this.mainActivity2=mainActivity2;
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
        View_task viewItem = null;
        task_list_adapter.position=position;

        if(convertView == null)
        {
            viewItem = new View_task();

            LayoutInflater layoutInfiater = (LayoutInflater)this.context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);

            convertView = layoutInfiater.inflate(R.layout.task_item, null);
            viewItem.taskname = convertView.findViewById(R.id.task_name);
            viewItem.taskdescription = convertView.findViewById(R.id.task_description);
            viewItem.expiraydate = convertView.findViewById(R.id.expiray_date);
            viewItem.attachements = convertView.findViewById(R.id.attachments);
            viewItem.assignedby = convertView.findViewById(R.id.assigned_by);

            viewItem.taskname.setText(valueList.get(position).task_name);
            viewItem.taskdescription.setText(valueList.get(position).task_description);
            viewItem.expiraydate.setText(valueList.get(position).expiray_date);
            viewItem.attachements.setText(valueList.get(position).file_name);
            viewItem.assignedby.setText(valueList.get(position).manager_email);
            convertView.setTag(viewItem);

        }
        else
        {
            viewItem = (View_task) convertView.getTag();
        }
        viewItem.attachements.setOnClickListener(new View.OnClickListener() {
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

class View_task {
    TextView taskname;
    TextView taskdescription;
    TextView expiraydate;
    TextView attachements;
    TextView assignedby;
}

