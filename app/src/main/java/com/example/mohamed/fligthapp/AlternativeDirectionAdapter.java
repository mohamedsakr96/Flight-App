package com.example.mohamed.fligthapp;

import android.content.Context;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;

/**
 * Created by mohamed on 1/18/17.
 */

public class AlternativeDirectionAdapter extends BaseAdapter {

    List<AlternativeDirectionsClass> list_ad ;
    Context context;


    public AlternativeDirectionAdapter(Context context, List<AlternativeDirectionsClass> list) {
        this.context = context;
        list_ad = list;

    }

    @Override
    public int getCount() {
        return list_ad.size();
    }

    @Nullable
    @Override
    public Object getItem(int position) {
        return list_ad.get(position);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View row = convertView;
        DataHandler handler;

        if(convertView == null)
        {
            LayoutInflater inflater= (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            row = inflater.inflate(R.layout.row,parent,false);
            handler = new DataHandler(row);
            row.setTag(handler);
        }
        else {
            handler = (DataHandler) row.getTag();
        }
        handler.from.setText("Origin: " + list_ad.get(position).getOrigin());
        handler.to.setText("Destination: " + list_ad.get(position).getDestination());

        return  row;

    }
    class DataHandler
    {
        TextView from;
        TextView to;
        public DataHandler(View view) {
            from = (TextView)view.findViewById(R.id.from_textView);
            to = (TextView)view.findViewById(R.id.to_textView);
        }
    }
}
