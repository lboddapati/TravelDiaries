package com.example.traveldiaries;

import android.content.Intent;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class StartTripActivity extends FragmentActivity {
    //TODO 1: When back button pressed while trip is in progress, save state.

    private GoogleMap mMap;
    private ParseObject trip;
    private ParseUser user;
    String tripname = "SomeTrip"; //TODO: Change to actual trip name;

    ArrayList<String> names;
    ArrayList<String> address;
    ArrayList<LatLng> latLngs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start_trip);

        if (android.os.Build.VERSION.SDK_INT > 9) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }

        setUpMapIfNeeded();

        user = ParseUser.getCurrentUser();

        Intent intent = getIntent();
        latLngs = intent.getParcelableArrayListExtra("latLngs");
        names = intent.getStringArrayListExtra("names");
        address = intent.getStringArrayListExtra("address");
        System.out.println("IN MAP TRIP ACTIVITY :: "+latLngs.size()+"::"+names.size()+"::"+address.size());

        final ExpandableListView directionsListView = (ExpandableListView) findViewById(R.id.directions);
        final Button finishTrip = (Button) findViewById(R.id.finishTrip);
        final ImageButton addPicture = (ImageButton) findViewById(R.id.addPicture);

        trip = new ParseObject("Trip");
        trip.put("user", user);
        trip.put("tripName", tripname);
        JSONObject route = MapHelperClass.getRoute(latLngs);
        trip.put("route", route);
        try {
            JSONArray waypointOrder = route.getJSONArray("waypoint_order");
            reorderByOptimizedWaypoints(waypointOrder);
            trip.put("places", getPlacesJSON(names, address, latLngs));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        trip.pinInBackground();

        MapHelperClass.drawMarkers(latLngs.subList(1, latLngs.size()-1), address.subList(1, latLngs.size()-1), mMap, BitmapDescriptorFactory.HUE_VIOLET);
        MapHelperClass.drawRoute(route, mMap);
        mMap.addMarker(new MarkerOptions().position(latLngs.get(0))
                .title(address.get(0))
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));
        mMap.addMarker(new MarkerOptions().position(latLngs.get(latLngs.size() - 1))
                .title(address.get(latLngs.size()-1))
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLngs.get(0), 13.0f));

        DirectionsExpandableListAdapter adapter = new DirectionsExpandableListAdapter(getBaseContext(), route, names);
        directionsListView.setAdapter(adapter);

        addPicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent pictureIntent = new Intent(StartTripActivity.this, AddPhotoNoteActivity.class);
                pictureIntent.putExtra("tripname", tripname);
                startActivity(pictureIntent);
            }
        });

        finishTrip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    trip.save();
                    trip.unpinInBackground();
                    uploadImagesToCloud();
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                Intent prevTrips = new Intent(StartTripActivity.this, PreviousTrip.class);
                startActivity(prevTrips);
                finish();
            }
        });
    }

    private void reorderByOptimizedWaypoints(JSONArray waypointOrder) throws JSONException {
        ArrayList<LatLng> reorderedLatLngs = new ArrayList<LatLng>();
        ArrayList<String> reorderedNames = new ArrayList<String>();
        ArrayList<String> reorderedAddress = new ArrayList<String>();

        reorderedLatLngs.add(latLngs.get(0));
        reorderedNames.add(names.get(0));
        reorderedAddress.add(address.get(0));

        for (int i=0; i<waypointOrder.length(); i++) {
            int waypoint = waypointOrder.getInt(i)+1;
            reorderedLatLngs.add(latLngs.get(waypoint));
            reorderedNames.add(names.get(waypoint));
            reorderedAddress.add(address.get(waypoint));
        }

        int num_of_places = latLngs.size();
        reorderedLatLngs.add(latLngs.get(num_of_places-1));
        reorderedNames.add(names.get(num_of_places-1));
        reorderedAddress.add(address.get(num_of_places-1));

        latLngs = reorderedLatLngs;
        names = reorderedNames;
        address = reorderedAddress;
    }

    private void uploadImagesToCloud() {
        ParseQuery<ParseObject> query = ParseQuery.getQuery("TripPhotoNote");
        query.fromPin(tripname);
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> parseObjects, ParseException e) {
                if(e == null) {
                    //Toast.makeText(StartTripActivity.this, "UPLOADING IMAGES TO CLOUD", Toast.LENGTH_SHORT).show();
                    Log.d("UPLOADING IMAGES TO CLOUD", tripname+" - FOUND :: "+parseObjects.size());
                    for(ParseObject obj : parseObjects) {
                        obj.put("trip", trip.getObjectId());
                        obj.saveInBackground();
                        obj.unpinInBackground(tripname);
                    }
                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_start_new_trip, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_cancel_trip) {
            trip.unpinInBackground();
            ParseObject.unpinAllInBackground(tripname);
            Intent prevTrips = new Intent(StartTripActivity.this, PreviousTrip.class);
            startActivity(prevTrips);
        }
        finish();

        return super.onOptionsItemSelected(item);
    }

    private JSONObject getPlacesJSON(ArrayList<String> names, ArrayList<String> address, ArrayList<LatLng> latLngs) throws JSONException {
        JSONObject placesJSON = new JSONObject();
        JSONArray placesArray = new JSONArray();
        for (int i=0; i<names.size(); i++) {
            JSONObject placeDetails = new JSONObject();
            placeDetails.put("name", names.get(i));
            placeDetails.put("address", address.get(i));
            placeDetails.put("latitude", latLngs.get(i).latitude);
            placeDetails.put("longitude", latLngs.get(i).longitude);
            placesArray.put(placeDetails);
        }
        placesJSON.put("places", placesArray);

        return placesJSON;
    }

    private void setUpMapIfNeeded() {
        // Do a null check to confirm that we have not already instantiated the map.
        if (mMap == null) {
            // Try to obtain the map from the SupportMapFragment.
            mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map))
                    .getMap();
        }
    }
}
