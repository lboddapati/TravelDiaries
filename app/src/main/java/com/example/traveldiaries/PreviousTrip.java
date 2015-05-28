package com.example.traveldiaries;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.content.Intent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.app.Activity;
import android.widget.Toast;

import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.List;

import java.util.ArrayList;


public class PreviousTrip extends Activity {
    ParseUser user;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        List<ParseObject> trips = new ArrayList<ParseObject>();
        final ArrayList<String> trip_names = new ArrayList<String>();
        final ArrayList<Bitmap> trip_icons = new ArrayList<Bitmap>();

        //Get current user
        user = ParseUser.getCurrentUser();
        //Get all user data
        ParseQuery<ParseObject> tripsQuery = ParseQuery.getQuery("Trip");
        tripsQuery.whereEqualTo("user", user);
        try {
            trips = tripsQuery.find();
        } catch (ParseException e) {
            e.printStackTrace();
        }

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = 2;  //TODO: set optimal size;

        for(ParseObject trip : trips) {
            trip_names.add(trip.getString("tripName"));

            ParseQuery<ParseObject> picsQuery = ParseQuery.getQuery("TripPhotoNote");
            picsQuery.whereEqualTo("trip", trip.getObjectId());
            //ArrayList<ParseObject> photoNotes = null;
            ParseObject photoNote = null;
            Bitmap icon = null;
            try {
                photoNote = picsQuery.getFirst();
                if(photoNote != null) {
                    byte[] data = photoNote.getParseFile("photo").getData();
                    icon = BitmapFactory.decodeByteArray(data, 0, data.length, options);
                }
            } catch (ParseException e) {
                Log.d("PHOTOS", "Error:: fetching photos!!");
                e.printStackTrace();
            } finally {
                if( icon == null) {
                    icon = BitmapFactory.decodeResource(getResources(), R.drawable.defaulticon, options);
                }
                trip_icons.add(icon);
            }
        }
        //Toast.makeText(this, "done fetching trips", Toast.LENGTH_SHORT).show();

        setContentView(R.layout.activity_previous_trip);


        //trip_names.add("sf");
        //trip_names.add("vegas");
        //Bitmap sf = BitmapFactory.decodeResource(getResources(), R.drawable.sf);
        //Bitmap vegas = BitmapFactory.decodeResource(getResources(), R.drawable.vegas);
        //trip_icons.add(sf);
        //trip_icons.add(vegas);

        GridView gridview = (GridView) findViewById(R.id.gridview);
        //Toast.makeText(this, " setting adapter", Toast.LENGTH_SHORT).show();
        gridview.setAdapter(new PicAdapter(this, trip_names, trip_icons));
        final List<ParseObject> finalTrips = trips;
        gridview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                Intent intent;
                intent = new Intent(PreviousTrip.this, ViewTripActivity.class);
                intent.putExtra("tripId", finalTrips.get(position).getObjectId());
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
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_new_trip) {
            //TODO: Change this to build new trip activity
            Intent buildNewTrip = new Intent(PreviousTrip.this, MapTripActivity.class);
            startActivity(buildNewTrip);
        } else if (id == R.id.action_sign_out) {
            //user.logOutInBackground();
            user.logOut();
            //Toast.makeText(this, "Signing out", Toast.LENGTH_SHORT).show();
            Intent loginIntent = new Intent(PreviousTrip.this, LoginActivity.class);
            startActivity(loginIntent);
            //Toast.makeText(this, "Starting login", Toast.LENGTH_SHORT).show();
            finish();
        }

        return super.onOptionsItemSelected(item);
    }
}


