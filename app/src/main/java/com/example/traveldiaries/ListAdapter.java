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
    private ArrayList<Integer> photosCount;
    private ArrayList<String> address;

    public ListAdapter(Context c, ArrayList<String> names, ArrayList<String> address, ArrayList<Integer> photosCount) {
        this.mContext = c;
        this.names=names;
        this.address=address;
        this.photosCount=photosCount;

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
        TextView PlaceAddress = (TextView) convertView.findViewById(R.id.PlaceAddress);
        TextView  PicsCount = (TextView) convertView.findViewById(R.id.PicsCount);
        PlaceHeader.setText(names.get(position));
        PlaceAddress.setText(address.get(position));
        int count = photosCount.get(position);
        if(count > 0) {
            PicsCount.setText(photosCount.get(position) + " Photos");
        } else {
            PicsCount.setText("");
        }

        return convertView;
    }

    @Override
    public boolean areAllItemsEnabled() {
        return false;
    }

    @Override
    public boolean isEnabled(int position) {
        return photosCount.get(position)!=0;
    }
}
