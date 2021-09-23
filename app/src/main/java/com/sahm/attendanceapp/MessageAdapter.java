package com.sahm.attendanceapp;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;

public class MessageAdapter extends BaseAdapter {
    Context context;
    List<Chat_list> valueList;
    static int position=0;
    String dat;
    String[] a;
    String p;
    Messenger mainActivity;

    public MessageAdapter(List<Chat_list> listValue, Context context,  Messenger mainActivity )
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

        return getCount();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent)
    {
        View2 viewItem = null;
        Em_list_adapter.position=position;

        if(convertView == null)
        {
            viewItem = new View2();

            LayoutInflater layoutInfiater = (LayoutInflater)this.context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);


            if(valueList.get(position).Sender_Email.equals(mainActivity.getLoggedInUserName()))
            {

                convertView = layoutInfiater.inflate(R.layout.item_out_message, null);
            }
            else
            {

                convertView = layoutInfiater.inflate(R.layout.item_in_message, null);
            }



            viewItem.Message = convertView.findViewById(R.id.textmsg);
            viewItem.Time = convertView.findViewById(R.id.texttime);
            viewItem.Message.setText(valueList.get(position).Message);
            viewItem.Time.setText(valueList.get(position).Time);
            convertView.setTag(viewItem);



        }
        else
        {
            viewItem = (View2) convertView.getTag();
        }

        return convertView;
    }
}

class View2 {
    TextView Message;
    TextView Time;

}

