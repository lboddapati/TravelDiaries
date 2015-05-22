package com.example.traveldiaries;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.content.Intent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.app.Activity;

import java.util.ArrayList;


public class PreviousTrip extends Activity {

    private Context context;
    //ArrayList<HashMap> photos=new ArrayList<>();
    //HashMap<String,Object> map = new HashMap<String, Object>();


    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_previous_trip);
        //Hashtable<String,Object> map = new Hashtable<String, Object>();
        //map.put("sf",R.drawable.sf);
        //map.put("vegas",R.drawable.vegas);
        ArrayList<String> trip_names= new ArrayList<String>();
        trip_names.add("sf");
        trip_names.add("vegas");
        ArrayList<Bitmap> trip_icons=new ArrayList<Bitmap>();
        Bitmap sf = BitmapFactory.decodeResource(getResources(), R.drawable.sf);
        Bitmap vegas = BitmapFactory.decodeResource(getResources(), R.drawable.vegas);
        trip_icons.add(sf);
        trip_icons.add(vegas);

        GridView gridview = (GridView) findViewById(R.id.gridview);

        gridview.setAdapter(new PicAdapter(this, trip_names, trip_icons));
        gridview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                Intent intent;
                intent = new Intent(PreviousTrip.this, MapsActivity.class);
                startActivity(intent);
            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_previous_trip, menu);
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


