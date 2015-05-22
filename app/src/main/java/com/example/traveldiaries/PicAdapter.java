package com.example.traveldiaries;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;

public class PicAdapter extends BaseAdapter {
    private Context mContext;
    //private Hashtable<String,Object> thumbnails;
    private ArrayList<String> names;
    private ArrayList<Bitmap> pics;

    public PicAdapter(Context c,ArrayList<String> names, ArrayList<Bitmap> pics) {
        this.mContext = c;
        //this.thumbnails=places;
        this.names=names;
        this.pics=pics;
    }

    public int getCount() {
        return mThumbIds.length;
    }

    public Object getItem(int position) {
        return pics.get(position);
    }

    public long getItemId(int position) {
        return 0;
    }
    // create a new ImageView for each item referenced by the Adapter
    public View getView(int position, View convertView, ViewGroup parent) {
        ImageView imageView=null;
        TextView textview = null;
        if (convertView == null) {
            // if it's not recycled, initialize some attributes
            LayoutInflater inflater =  (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.trip_thumbnails,null);
        }
        //else {
        //    imageView = (ImageView) convertView;
        //    textview = (TextView) convertView;
        //}
        textview=(TextView) convertView.findViewById(R.id.trip_name);
        imageView = (ImageView) convertView.findViewById(R.id.Trip_icon);
        //imageView.setLayoutParams(new GridView.LayoutParams(85, 85));
        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        imageView.setPadding(8, 8, 8, 8);

        imageView.setImageBitmap((Bitmap) getItem(position));
        //imageView.setImageResource(pics[position]);
        textview.setText(names.get(position));
        return convertView;

    }

    //references to our images
    private int[] mThumbIds = {
            R.drawable.sf,R.drawable.vegas
    };
}
