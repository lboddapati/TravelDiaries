package com.example.traveldiaries;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

//public class ViewTripActivity extends FragmentActivity {
public class ViewTripActivity extends MapActivity {
    //TODO: Add option to view all pics at once

    private GoogleMap mMap; // Might be null if Google Play services APK is not available.
    private ParseObject trip;
    private List<ParseObject> photoNotes;
    private JSONObject placesJSON; //TODO: change this to list of google places;
    private JSONObject route;

    ArrayList<String> names;
    ArrayList<String> address;
    ArrayList<LatLng> latLngs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setLayoutFile(R.layout.activity_trip_details);
        super.onCreate(savedInstanceState);

        names = new ArrayList<String>();
        address = new ArrayList<String>();
        latLngs = new ArrayList<LatLng>();

        String tripID = getIntent().getStringExtra("tripId");
        ParseQuery<ParseObject> tripQuery = ParseQuery.getQuery("Trip");
        try {
            trip = tripQuery.get(tripID);
            placesJSON = trip.getJSONObject("places");
            route = trip.getJSONObject("route");
            ParseQuery<ParseObject> picsQuery = ParseQuery.getQuery("TripPhotoNote");
            picsQuery.whereEqualTo("trip", trip.getObjectId());
            photoNotes = picsQuery.find();
        } catch (ParseException e) {
            e.printStackTrace();
        }

        parsePlacesJSON(placesJSON);
        drawMarkers(latLngs);
        drawRoute(route);

        /*ArrayList<String> trip_places= new ArrayList<String>();
        trip_places.add("Golden Gate Bridge");
        trip_places.add("Pier 39");
        ArrayList<String> trip_photos= new ArrayList<String>();
        trip_photos.add("5 photos");
        trip_photos.add("7 photos");*/

        ListView listView = (ListView) findViewById(R.id.places);
        listView.setAdapter(new ListAdapter(this, names, matchPhotosToPlaces()));
        listView.setVisibility(View.VISIBLE);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                Intent intent;
                intent = new Intent(ViewTripActivity.this, MyPhotos.class);
                startActivity(intent);
            }
        });
        //setUpMapIfNeeded();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    //TODO: Match photos to closest place;
    private ArrayList<Integer> matchPhotosToPlaces() {
        //HashMap<String, ArrayList<Bitmap>> photoNotePlaceMap = new HashMap<String, ArrayList<Bitmap>>();
        /*for(ParseObject photonote : photoNotes) {
            photonote.get("location");
        }*/
        ArrayList<Integer> photoCount = new ArrayList<Integer>();
        for(int i=0; i<names.size(); i++) {
            photoCount.add(i);
        }
        return photoCount;
    }

    private ArrayList<LatLng> getPlacesLatLng(JSONObject placesJSON) {
        ArrayList<LatLng> places = new ArrayList<LatLng>();
        double lat;
        double lng;

        try {
            JSONArray placesArray = placesJSON.getJSONArray("places");
            for(int i=0; i< placesArray.length(); i++) {
                lat = placesArray.getJSONObject(i).getDouble("latitude");
                lng = placesArray.getJSONObject(i).getDouble("longitude");
                places.add(new LatLng(lat, lng));
            }
        } catch (JSONException e) {
            e.printStackTrace();
            places = null;
        }

        return places;
    }

    private void parsePlacesJSON(JSONObject placesJSON) {
        double lat;
        double lng;
        JSONObject place;

        try {
            JSONArray placesArray = placesJSON.getJSONArray("places");
            for(int i=0; i< placesArray.length(); i++) {
                place = placesArray.getJSONObject(i);
                names.add(place.getString("name"));
                address.add(place.getString("address"));
                lat = place.getDouble("latitude");
                lng = place.getDouble("longitude");
                latLngs.add(new LatLng(lat, lng));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

}
