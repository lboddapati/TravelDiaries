package com.example.traveldiaries;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;
import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONStringer;

import java.util.ArrayList;
import java.util.List;

public class MapTripActivity extends MapActivity {
    //TODO 1: When back button pressed while trip is in progress, save state.

    private ParseObject trip;
    private ParseUser user;
    //static final int REQUEST_ADD_PHOTONOTE = 0;
    //private ArrayList<String> imageObjectIds;
    String tripname = "SomeTrip"; //TODO: Change to actual trip name;

    ArrayList<String> names = new ArrayList<String>();
    ArrayList<String> address = new ArrayList<String>();
    ArrayList<LatLng> latLngs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setLayoutFile(R.layout.activity_map_trip);
        super.onCreate(savedInstanceState);

        user = ParseUser.getCurrentUser();

        //TODO: Replace with actual places.
        latLngs = new ArrayList<LatLng>();
        latLngs.add(new LatLng(37.258881, -122.032913));
        latLngs.add(new LatLng(37.270220, -122.015403));
        latLngs.add(new LatLng(37.291732, -122.032398));
        latLngs.add(new LatLng(37.287362, -121.944679));
        latLngs.add(new LatLng(37.240092, -121.960987));
        latLngs.add(new LatLng(37.281086, -122.026335));
        latLngs.add(new LatLng(37.260458, -122.029596));
        latLngs.add(new LatLng(37.296109, -122.029596));
        latLngs.add(new LatLng(37.325054, -121.867891));
        latLngs.add(new LatLng(37.285183, -121.939989));

        names = new ArrayList<String>();
        names.add("origin");
        names.add("waypoint 1");
        names.add("waypoint 2");
        names.add("waypoint 3");
        names.add("waypoint 4");
        names.add("waypoint 5");
        names.add("waypoint 6");
        names.add("waypoint 7");
        names.add("waypoint 8");
        names.add("destination");

        address = new ArrayList<String>();
        address.add("origin address");
        address.add("waypoint address 1");
        address.add("waypoint address 2");
        address.add("waypoint address 3");
        address.add("waypoint address 4");
        address.add("waypoint address 5");
        address.add("waypoint address 6");
        address.add("waypoint address 7");
        address.add("waypoint address 8");
        address.add("destination address");

        final ExpandableListView directionsListView = (ExpandableListView) findViewById(R.id.directions);
        //final Button startTrip = (Button) findViewById(R.id.startTrip);
        final Button finishTrip = (Button) findViewById(R.id.finishTrip);
        final ImageButton addPicture = (ImageButton) findViewById(R.id.addPicture);

        /*startTrip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startTrip.setVisibility(View.GONE);
                finishTrip.setVisibility(View.VISIBLE);
                addPicture.setVisibility(View.VISIBLE);
                directionsListView.setVisibility(View.VISIBLE);

                JSONObject route;
                try {
                    if(directionsJSONObject != null) {
                        route = directionsJSONObject.getJSONArray("routes").getJSONObject(0);
                        trip = new ParseObject("Trip");
                        trip.put("tripName", tripname);
                        trip.put("places", placesJSON);
                        trip.put("route", route);
                        trip.put("user", user);
                        //trip.saveInBackground();
                        trip.pinInBackground();
                        displayDirections(route);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        });*/

        trip = new ParseObject("Trip");
        trip.put("user", user);
        trip.put("tripName", tripname);
        try {
            trip.put("places", getPlacesJSON(names, address, latLngs));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        JSONObject route = getRoute(latLngs);
        trip.put("route", route);
        //trip.saveInBackground();
        trip.pinInBackground();
        drawRoute(route);
        displayDirections(route, directionsListView);

        addPicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent pictureIntent = new Intent(MapTripActivity.this, AddPhotoNoteActivity.class);
                //pictureIntent.putExtra("tripId", trip.getObjectId());
                pictureIntent.putExtra("tripname", tripname);
                //startActivityForResult(pictureIntent, REQUEST_ADD_PHOTONOTE, null);
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
                Intent prevTrips = new Intent(MapTripActivity.this, PreviousTrip.class);
                startActivity(prevTrips);
                finish();
            }
        });
    }

    /*@Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_ADD_PHOTONOTE && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            imageObjectIds.addAll(extras.getStringArrayList("imageObjectIds"));
            Log.d("RESULT", "Total "+imageObjectIds.size()+" photos");
        }
    }*/

    private void uploadImagesToCloud() {
        ParseQuery<ParseObject> query = ParseQuery.getQuery("TripPhotoNote");
        query.fromPin(tripname);
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> parseObjects, ParseException e) {
                if(e == null) {
                    Toast.makeText(MapTripActivity.this, "UPLOADING IMAGES TO CLOUD", Toast.LENGTH_SHORT).show();
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
            Intent prevTrips = new Intent(MapTripActivity.this, PreviousTrip.class);
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
}
