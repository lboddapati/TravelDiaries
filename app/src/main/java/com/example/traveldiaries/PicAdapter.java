package com.example.traveldiaries;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.support.annotation.DimenRes;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import java.util.ArrayList;

public class PicAdapter extends BaseAdapter {
    private Context mContext;
    private ArrayList<String> names;
    private ArrayList<Bitmap> pics;

    public PicAdapter(Context c,ArrayList<String> names, ArrayList<Bitmap> pics) {
        this.mContext = c;
        this.names=names;
        this.pics=pics;
    }

    public int getCount() {
        return names.size();
    }

    public Object getItem(int position) {
        return pics.get(position);
    }

    public long getItemId(int position) {
        return position;
    }
    // create a new ImageView for each item referenced by the Adapter
    public View getView(int position, View convertView, ViewGroup parent) {
        ImageView imageView=null;
        TextView textview = null;
        if (convertView == null) {
            LayoutInflater inflater =  (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.trip_thumbnails,null);
        }
        textview=(TextView) convertView.findViewById(R.id.trip_name);
        imageView = (ImageView) convertView.findViewById(R.id.Trip_icon);
        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        imageView.setPadding(8, 8, 8, 8);

        imageView.setImageBitmap((Bitmap) getItem(position));
        textview.setText(names.get(position));
        return convertView;

    }

    public int getImageWidth() {
        Resources resource = mContext.getResources();
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 150, resource.getDisplayMetrics());
    }

    public int getImageHeight() {
        Resources resource = mContext.getResources();
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 150, resource.getDisplayMetrics());    }
}
