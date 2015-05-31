package com.example.traveldiaries;

import android.content.Intent;
import android.graphics.Bitmap;

import android.graphics.BitmapFactory;
import android.location.Location;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.v4.app.FragmentActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.parse.ParseException;

import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class ViewTripActivity extends FragmentActivity {
    //TODO: Add option to view all pics at once

    private GoogleMap mMap; // Might be null if Google Play services APK is not available.
    private ParseObject trip;
    private List<ParseObject> photoNotes;
    private JSONObject placesJSON; //TODO: change this to list of google places;
    private JSONObject route;

    private ArrayList<String> names;
    private ArrayList<String> address;
    private ArrayList<LatLng> latLngs;

    private ArrayList<ArrayList<Bitmap>> photosAtPlaces;
    private ArrayList<Bitmap> allPhotos;
    private ArrayList<ArrayList<String>> notesAtPlaces;
    private ArrayList<String> allNotes;
    private ArrayList<ArrayList<String>> geotagTimestampOfPlaces;
    private ArrayList<String> allGeotagTimestamps;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trip_details);

        if (android.os.Build.VERSION.SDK_INT > 9) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }

        setUpMapIfNeeded();

        names = new ArrayList<String>();
        address = new ArrayList<String>();
        latLngs = new ArrayList<LatLng>();

        String tripID = getIntent().getStringExtra("tripId");
        ParseQuery<ParseObject> tripQuery = ParseQuery.getQuery("Trip");
        try {
            trip = tripQuery.get(tripID);
            String tripname = trip.getString("tripName");
            //String date = String.valueOf(trip.getCreatedAt());
            setTitle(tripname);//+" ("+date+")");

            placesJSON = trip.getJSONObject("places");
            route = trip.getJSONObject("route");
            ParseQuery<ParseObject> picsQuery = ParseQuery.getQuery("TripPhotoNote");
            picsQuery.whereEqualTo("trip", trip.getObjectId());
            photoNotes = picsQuery.find();

            parsePlacesJSON(placesJSON);
            MapHelperClass.drawMarkers(latLngs, names, mMap, null);
            MapHelperClass.drawRoute(route, mMap);
            initialize();

            ListView listView = (ListView) findViewById(R.id.places);

            matchPhotosToPlaces();
            listView.setAdapter(new ListAdapter(this, names, address, getPhotoCount(photosAtPlaces)));
            listView.setVisibility(View.VISIBLE);

            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                    Intent intent;
                    intent = new Intent(ViewTripActivity.this, ViewTripPhotosActivity.class);
                    intent.putParcelableArrayListExtra("photos", photosAtPlaces.get(position));
                    intent.putStringArrayListExtra("notes", notesAtPlaces.get(position));
                    intent.putStringArrayListExtra("geotag_timestamp", geotagTimestampOfPlaces.get(position));
                    startActivity(intent);
                }
            });
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    private void initialize() {
        allPhotos = new ArrayList<Bitmap>();
        allNotes = new ArrayList<String>();
        allGeotagTimestamps = new ArrayList<String>();

        photosAtPlaces = new ArrayList<ArrayList<Bitmap>>(latLngs.size());
        notesAtPlaces = new ArrayList<ArrayList<String>>(latLngs.size());
        geotagTimestampOfPlaces = new ArrayList<ArrayList<String>>(latLngs.size());
        for(int i=0; i<latLngs.size(); i++) {
            photosAtPlaces.add(null);
            notesAtPlaces.add(null);
            geotagTimestampOfPlaces.add(null);
        }

    }

    private void matchPhotosToPlaces() throws ParseException {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = 2;  //TODO: set optimal size;

        for (ParseObject photonote : photoNotes) {
            ParseGeoPoint geoPoint = (ParseGeoPoint) photonote.get("location");


            float minDist = Float.MIN_VALUE;
            int closestPlace=0;
            for(int i=0; i<latLngs.size(); i++) {
                float[] dist = new float[1];
                Location.distanceBetween(geoPoint.getLatitude(), geoPoint.getLongitude()
                        , latLngs.get(i).latitude, latLngs.get(i).longitude, dist);
                if(dist[0] < minDist) {
                    minDist = dist[0];
                    closestPlace = i;
                }
            }

            byte[] data = photonote.getParseFile("photo").getData();
            Bitmap photo = BitmapFactory.decodeByteArray(data, 0, data.length, options);
            String note = photonote.getString("note");
            String geotagTimeStamp = "At "+names.get(closestPlace)+" ("+photonote.getCreatedAt()+")";

            allPhotos.add(photo);
            allNotes.add(note);
            allGeotagTimestamps.add(geotagTimeStamp);

            if(photosAtPlaces.get(closestPlace)==null) {
                ArrayList<Bitmap> pictures = new ArrayList<Bitmap>();
                ArrayList<String> notes = new ArrayList<String>();
                ArrayList<String> geotagTimeStamps = new ArrayList<String>();
                pictures.add(photo);
                notes.add(note);
                geotagTimeStamps.add(geotagTimeStamp);
                photosAtPlaces.add(closestPlace, pictures);
                notesAtPlaces.add(closestPlace, notes);
                geotagTimestampOfPlaces.add(closestPlace, geotagTimeStamps);
            } else {
                photosAtPlaces.get(closestPlace).add(photo);
                notesAtPlaces.get(closestPlace).add(note);
                geotagTimestampOfPlaces.get(closestPlace).add(closestPlace, geotagTimeStamp);
            }
        }
    }

    private ArrayList<Integer> getPhotoCount(ArrayList<ArrayList<Bitmap>> photosAtPlaces) {
        ArrayList<Integer> photoCount = new ArrayList<Integer>();
        for(int i=0; i<names.size(); i++) {
            if(photosAtPlaces.size()<=i || photosAtPlaces.get(i) == null) {
                photoCount.add(0);
            } else {
                photoCount.add(photosAtPlaces.get(i).size());
            }
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

    private void setUpMapIfNeeded() {
        // Do a null check to confirm that we have not already instantiated the map.
        if (mMap == null) {
            // Try to obtain the map from the SupportMapFragment.
            mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map))
                    .getMap();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_view_trip, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_view_all_photos) {
            Intent intent;
            intent = new Intent(ViewTripActivity.this, ViewTripPhotosActivity.class);
            intent.putParcelableArrayListExtra("photos", allPhotos);
            intent.putStringArrayListExtra("notes", allNotes);
            intent.putStringArrayListExtra("geotag_timestamp", allGeotagTimestamps);
            startActivity(intent);
        }

        return super.onOptionsItemSelected(item);
    }

}
