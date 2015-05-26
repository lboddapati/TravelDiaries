package com.example.traveldiaries;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.traveldiaries.R;

import java.util.ArrayList;

public class MyPhotos extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_photos);

        final ArrayList<Bitmap> pics_icons = new ArrayList<Bitmap>();
        Bitmap sf = BitmapFactory.decodeResource(getResources(), R.drawable.sf);
        Bitmap vegas = BitmapFactory.decodeResource(getResources(), R.drawable.vegas);
        Bitmap sunlight = BitmapFactory.decodeResource(getResources(), R.drawable.sunlight);
        final Bitmap image1 = BitmapFactory.decodeResource(getResources(), R.drawable.image1);
        pics_icons.add(sf);
        pics_icons.add(vegas);
        pics_icons.add(sunlight);
        pics_icons.add(image1);
        final ArrayList<String> pic_notes=new ArrayList<String>();
        pic_notes.add("IN SF");
        pic_notes.add("IN VEGAS");
        pic_notes.add("IT'S HOT");
        pic_notes.add("KITTY CAT");
        GridView gridview = (GridView) findViewById(R.id.gridview);
        gridview.setAdapter(new ImageAdapter(this, pics_icons));
        gridview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
       //         Intent intent;
        //        intent = new Intent(MyPhotos.this, DisplayPhoto.class);
        //        startActivity(intent);
                //Dialog image_dialog= new Dialog(MyPhotos.this, android.R.style.Theme_Black_NoTitleBar_Fullscreen);
                LayoutInflater inflater =  (LayoutInflater) MyPhotos.this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                Dialog image_dialog= new Dialog(MyPhotos.this);
                image_dialog.setContentView(R.layout.places_pics);
                //View view1 = inflater.inflate(R.layout.places_pics,null);
                ImageView image1= (ImageView) image_dialog.findViewById(R.id.Trip_icon);
                image1.setImageBitmap((Bitmap) pics_icons.get(position));
                TextView text1=(TextView) image_dialog.findViewById(R.id.trip_name);
                text1.setText(pic_notes.get(position));
                //image1.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
                text1.setElevation(12);
                //text1.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                //image_dialog.setContentView(image1);
                image_dialog.show();
            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_my_photos, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
