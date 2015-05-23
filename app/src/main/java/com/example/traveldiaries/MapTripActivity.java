package com.example.traveldiaries;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Outline;
import android.location.Location;
import android.location.LocationManager;
import android.os.StrictMode;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewOutlineProvider;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;

//public class MapTripActivity extends FragmentActivity {
public class MapTripActivity extends MapActivity {


    /*private GoogleMap mMap; // Might be null if Google Play services APK is not available.
    private JSONObject directionsJSONObject;
    private ArrayList<LatLng> places;
    JSONObject placesJSON;*/

    private ParseObject trip;
    private ParseUser user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //setContentView(R.layout.activity_map_trip);
        setLayoutFile(R.layout.activity_map_trip);
        super.onCreate(savedInstanceState);

        /*placesJSON = new JSONObject();  //TODO: Replace with actual places JSON object;
        places = new ArrayList<LatLng>();
        places.add(new LatLng(37.258881, -122.032913));
        places.add(new LatLng(37.270220, -122.015403));
        places.add(new LatLng(37.291732, -122.032398));
        places.add(new LatLng(37.287362, -121.944679));
        places.add(new LatLng(37.240092, -121.960987));*/

        user = ParseUser.getCurrentUser();


        /*if (android.os.Build.VERSION.SDK_INT > 9) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }

        setUpMapIfNeeded();*/

        final ExpandableListView directionsListView = (ExpandableListView) findViewById(R.id.directions);
        final Button startTrip = (Button) findViewById(R.id.startTrip);
        final ImageButton addPicture = (ImageButton) findViewById(R.id.addPicture);

        startTrip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Log.d("START TRIP", "Starting trip");
                startTrip.setVisibility(View.GONE);
                addPicture.setVisibility(View.VISIBLE);
                directionsListView.setVisibility(View.VISIBLE);

                JSONObject route;
                try {
                    if(directionsJSONObject != null) {
                        route = directionsJSONObject.getJSONArray("routes").getJSONObject(0);
                        trip = new ParseObject("Trip");
                        trip.put("tripName", "SomeTrip");
                        trip.put("places", placesJSON);
                        trip.put("route", route);
                        trip.put("user", user);
                        trip.saveInBackground();
                        displayDirections(route);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        });

        /*addPicture.setOutlineProvider(new ViewOutlineProvider() {
            @Override
            public void getOutline(View view, Outline outline) {
                int diameter = getResources().getDimensionPixelSize(R.dimen.round_button_diameter);
                outline.setOval(0, 0, diameter, diameter);
            }
        });
        addPicture.setClipToOutline(true);*/

        addPicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent pictureIntent = new Intent(MapTripActivity.this, AddPhotoNoteActivity.class);
                pictureIntent.putExtra("tripId", trip.getObjectId());
                startActivity(pictureIntent);
                //startActivityForResult(pictureIntent, REQUEST_TAKE_PHOTONOTES);
            }
        });
    }

    /*@Override
    protected void onResume() {
        super.onResume();
        setUpMapIfNeeded();
    }*/

    /*private void setUpMapIfNeeded() {
        // Do a null check to confirm that we have not already instantiated the map.
        if (mMap == null) {
            // Try to obtain the map from the SupportMapFragment.
            mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map))
                    .getMap();
            // Check if we were successful in obtaining the map.
            if (mMap != null) {
                setUpMap();
            }
        }
    }*/

    /*private void setUpMap() {

        for(int i=0; i< places.size(); i++) {
            mMap.addMarker(new MarkerOptions().position(places.get(i)).title("Place "+i));
        }

        mMap.moveCamera(CameraUpdateFactory.newLatLng(places.get(0)));

        //TODO: Add support for waypoints > 8
        String waypoints = "waypoints=optimize:true";
        for(int i=1; i<places.size()-1; i++) {
            waypoints = waypoints + "%7C" + places.get(i).latitude + "," + places.get(i).longitude;
        }
        String origin = "origin="+places.get(0).latitude+","+places.get(0).longitude;
        String dest = "destination="+places.get(places.size()-1).latitude+","+places.get(places.size()-1).longitude;
        String sensor = "sensor=false";
        String units = "units=metric";
        String output = "json";
        String mode = "mode=driving";
        String params = origin+ "&"+ dest+ "&"+ sensor +"&"+ units +"&"+ mode +"&"+ waypoints;
        String url = "https://maps.googleapis.com/maps/api/directions/"
                + output + "?" + params;

        try {
            HttpClient httpClient = new DefaultHttpClient();
            HttpContext localContext = new BasicHttpContext();
            HttpPost postRequest = new HttpPost(url);
            HttpResponse response = httpClient.execute(postRequest, localContext);

            if(response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
                throw new RuntimeException("Failed : HTTP error code : "
                        + response.getStatusLine().getStatusCode());
            } else {
                directionsJSONObject = new JSONObject(EntityUtils.toString(response.getEntity()));
                drawRoute();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }*/

    /*private void drawRoute() {
        try {
            JSONArray routes = directionsJSONObject.getJSONArray("routes");
            JSONObject route = routes.getJSONObject(0);
            JSONObject overviewPolyline = route.getJSONObject("overview_polyline");
            String encodedPolylines = overviewPolyline.getString("points");
            ArrayList<LatLng> points = decodePolylines(encodedPolylines);

            for(int i = 0; i<points.size()-1;i++){
                LatLng src= points.get(i);
                LatLng dest= points.get(i+1);
                mMap.addPolyline(new PolylineOptions()
                        .add(new LatLng(src.latitude, src.longitude), new LatLng(dest.latitude, dest.longitude))
                        .width(5)
                        .color(Color.BLUE)
                        .geodesic(true));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }*/

    /*private ArrayList<LatLng> decodePolylines(String encodedPolylines) {
        int N = encodedPolylines.length();
        int lat = 0, lng = 0;
        ArrayList<LatLng> polylinePoints = new ArrayList<LatLng>();

        for (int i=0; i< N;) {
            int asciicode;
            int shift = 0;
            int result = 0;
            do {
                asciicode = encodedPolylines.charAt(i++) - 63;
                result |= (asciicode & 0x1f) << shift;
                shift += 5;
            } while (asciicode >= 0x20);
            int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lat += dlat;

            shift = 0;
            result = 0;
            do {
                asciicode = encodedPolylines.charAt(i++) - 63;
                result |= (asciicode & 0x1f) << shift;
                shift += 5;
            } while (asciicode >= 0x20);
            int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lng += dlng;

            LatLng point = new LatLng( (((double) lat / 1E5)), (((double) lng / 1E5) ));
            polylinePoints.add(point);
        }

        return polylinePoints;
    }*/

    /*private void displayDirections(JSONObject route) {
        ExpandableListView directionsListView = (ExpandableListView) findViewById(R.id.directions);
        DirectionsExpandableListAdapter adapter = new DirectionsExpandableListAdapter(getBaseContext(), route);
        directionsListView.setAdapter(adapter);
    }*/

    /*@Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_TAKE_PHOTONOTES && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            ArrayList<Bitmap> photos = extras.getParcelableArrayList("photos");
            ArrayList<String> notes = extras.getStringArrayList("notes");
            //ParseGeoPoint geoPoint = getCurrentLocation();
            ParseGeoPoint geoPoint = new ParseGeoPoint(37.269382, -122.005476); //TODO: Remove this
            if(uploadImagesToCloud(photos, notes, geoPoint)) {
                Toast.makeText(MapTripActivity.this, photos.size() + " Photos Added!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(MapTripActivity.this, "Error:: Photos Upload Failed!", Toast.LENGTH_SHORT).show();
            }
        }
    }*/




}
