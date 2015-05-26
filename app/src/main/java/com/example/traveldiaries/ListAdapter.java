package com.example.traveldiaries;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by Spandana on 5/22/15.
 */
public class ListAdapter extends BaseAdapter {

    private Context mContext;
    private ArrayList<String> names;
    private ArrayList<String> photos;

    public ListAdapter(Context c,ArrayList<String> names, ArrayList<String> photos) {
        this.mContext = c;
        //this.thumbnails=places;
        this.names=names;
        this.photos=photos;

    }
    @Override
    public int getCount() {
        return names.size();
    }

    @Override
    public Object getItem(int position) {
        return names.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        if(convertView == null) {
            LayoutInflater inflater =  (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.places_list, null);
        }
        TextView PlaceHeader = (TextView) convertView.findViewById(R.id.PlaceHeader);
        TextView  Pics = (TextView) convertView.findViewById(R.id.Pics);
        PlaceHeader.setText(names.get(position));
        Pics.setText(photos.get(position));

        return convertView;
    }
}