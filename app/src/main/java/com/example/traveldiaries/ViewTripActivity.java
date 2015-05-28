package com.example.traveldiaries;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
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

    private ArrayList<ArrayList<Bitmap>> photosAtPlaces;
    private ArrayList<ArrayList<String>> notesAtPlaces;

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
        //final ArrayList<ArrayList<ParseObject>> photonotesAtPlaces;

        initialize();


        try {
            matchPhotosToPlaces();
            listView.setAdapter(new ListAdapter(this, names, getPhotoCount(photosAtPlaces)));
            listView.setVisibility(View.VISIBLE);

            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                    Intent intent;
                    intent = new Intent(ViewTripActivity.this, MyPhotos.class);
                    intent.putParcelableArrayListExtra("photos", photosAtPlaces.get(position));
                    intent.putStringArrayListExtra("notes", notesAtPlaces.get(position));
                    startActivity(intent);
                }
            });
        } catch (ParseException e) {
            e.printStackTrace();
        }
        //setUpMapIfNeeded();
    }

    private void initialize() {
        photosAtPlaces = new ArrayList<ArrayList<Bitmap>>(latLngs.size());
        notesAtPlaces = new ArrayList<ArrayList<String>>(latLngs.size());
        for(int i=0; i<latLngs.size(); i++) {
            photosAtPlaces.add(null);
            notesAtPlaces.add(null);
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    private void matchPhotosToPlaces() throws ParseException {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = 2;  //TODO: set optimal size;

        //ArrayList<ArrayList<ParseObject>> photoNotePlaceMap = new ArrayList<ArrayList<ParseObject>>(latLngs.size());
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
            if(photosAtPlaces.get(closestPlace)==null) {
                ArrayList<Bitmap> pictures = new ArrayList<Bitmap>();
                ArrayList<String> notes = new ArrayList<String>();
                pictures.add(photo);
                notes.add(note);
                photosAtPlaces.add(closestPlace, pictures);
                notesAtPlaces.add(closestPlace, notes);
            } else {
                photosAtPlaces.get(closestPlace).add(photo);
                notesAtPlaces.get(closestPlace).add(note);
            }
        }
    }

    private ArrayList<Integer> getPhotoCount(ArrayList<ArrayList<Bitmap>> photoNotePlaceMap) {
        ArrayList<Integer> photoCount = new ArrayList<Integer>();
        for(int i=0; i<names.size(); i++) {
            if(photoNotePlaceMap.get(i) == null) {
                photoCount.add(0);
            } else {
                photoCount.add(photoNotePlaceMap.get(i).size());
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

}
