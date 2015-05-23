package com.example.traveldiaries;

import android.content.Intent;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.example.traveldiaries.R;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

//public class MapsActivity extends FragmentActivity {
public class MapsActivity extends MapActivity {

    private GoogleMap mMap; // Might be null if Google Play services APK is not available.
    private ParseObject trip;
    private List<ParseObject> photoNotes;
    private JSONObject places; //TODO: change this to list of google places;
    private JSONObject route;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setLayoutFile(R.layout.activity_trip_details);
        super.onCreate(savedInstanceState);

        String tripID = getIntent().getStringExtra("tripId");
        ParseQuery<ParseObject> tripQuery = ParseQuery.getQuery("Trip");
        //tripQuery.whereEqualTo("objectId", tripID);
        try {
            trip = tripQuery.get(tripID);
            places = trip.getJSONObject("places");
            route = trip.getJSONObject("route");
            ParseQuery<ParseObject> picsQuery = ParseQuery.getQuery("TripPhotoNote");
            picsQuery.whereEqualTo("trip", trip.getObjectId());
            photoNotes = picsQuery.find();
        } catch (ParseException e) {
            e.printStackTrace();
        }

        //setContentView(R.layout.activity_trip_details);

        ArrayList<String> trip_places= new ArrayList<String>();
        trip_places.add("Golden Gate Bridge");
        trip_places.add("Pier 39");
        ArrayList<String> trip_photos= new ArrayList<String>();
        trip_photos.add("5 photos");
        trip_photos.add("7 photos");
        ListView listView = (ListView) findViewById(R.id.places);
        listView.setAdapter(new ListAdapter(this, trip_places, trip_photos));
        listView.setVisibility(View.VISIBLE);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                Intent intent;
                intent = new Intent(MapsActivity.this, MyPhotos.class);
                startActivity(intent);
            }
        });
        //setUpMapIfNeeded();
    }

}
