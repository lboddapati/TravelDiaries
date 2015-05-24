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

import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class MapTripActivity extends MapActivity {
    //TODO 1: When back button pressed while trip is in progress, save state.

    private ParseObject trip;
    private ParseUser user;
    //static final int REQUEST_ADD_PHOTONOTE = 0;
    //private ArrayList<String> imageObjectIds;
    String tripname = "SomeTrip"; //TODO: Change to actual trip name;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setLayoutFile(R.layout.activity_map_trip);
        super.onCreate(savedInstanceState);

        user = ParseUser.getCurrentUser();
        //imageObjectIds = new ArrayList<String>();

        final ExpandableListView directionsListView = (ExpandableListView) findViewById(R.id.directions);
        final Button startTrip = (Button) findViewById(R.id.startTrip);
        final Button finishTrip = (Button) findViewById(R.id.finishTrip);
        final ImageButton addPicture = (ImageButton) findViewById(R.id.addPicture);

        startTrip.setOnClickListener(new View.OnClickListener() {
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
        });

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
}
