package com.example.traveldiaries;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

import java.util.ArrayList;

/**
 * Created by lasyaboddapati on 5/13/15.
 */
public class ImageAdapter extends BaseAdapter {
    private Context context;
    private ArrayList<Bitmap> thumbnails;

    public ImageAdapter(Context context, ArrayList<Bitmap> list) {
        this.context = context;
        this.thumbnails = list;
    }

    @Override
    public int getCount() {
        return thumbnails.size();
    }

    @Override
    public Object getItem(int position) {
        return thumbnails.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ImageView imageView;
        if(convertView == null) {
            imageView = new ImageView(context);
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            imageView.setLayoutParams(new GridView.LayoutParams(150, 150));
        } else {
            imageView =(ImageView) convertView;
        }

        //Bitmap image = ImageProcessingHelperClass.getResizedBitmap((Bitmap) getItem(position), getImageWidth(), getImageHeight());
        imageView.setImageBitmap((Bitmap) getItem(position));
        return imageView;
    }

    public int getImageWidth() {
        return 150;
    }

    public int getImageHeight() {
        return 150;
    }
}
