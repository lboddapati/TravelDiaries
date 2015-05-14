package com.example.lasyaboddapati.traveldiaries;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Outline;
import android.os.StrictMode;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewOutlineProvider;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.ImageButton;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

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

import java.io.IOException;
import java.util.ArrayList;

public class MapTripActivity extends FragmentActivity {

    private GoogleMap mMap; // Might be null if Google Play services APK is not available.
    private JSONObject directionsJSONObject;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map_trip);

        if (android.os.Build.VERSION.SDK_INT > 9) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }

        setUpMapIfNeeded();

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
                try {
                    if(directionsJSONObject != null) {
                        displayDirections(directionsJSONObject.getJSONArray("routes").getJSONObject(0));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });

        addPicture.setOutlineProvider(new ViewOutlineProvider() {
            @Override
            public void getOutline(View view, Outline outline) {
                int diameter = getResources().getDimensionPixelSize(R.dimen.round_button_diameter);
                outline.setOval(0, 0, diameter, diameter);
            }
        });
        addPicture.setClipToOutline(true);
        addPicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent pictureIntent = new Intent(MapTripActivity.this, AddPhotoNoteActivity.class);
                startActivity(pictureIntent);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        setUpMapIfNeeded();
    }

    private void setUpMapIfNeeded() {
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
    }

    private void setUpMap() {
        LatLng[] places = new LatLng[5];
        places[0] = new LatLng(37.258881, -122.032913);
        places[1] = new LatLng(37.270220, -122.015403);
        places[2] = new LatLng(37.291732, -122.032398);
        places[3] = new LatLng(37.287362, -121.944679);
        places[4] = new LatLng(37.240092, -121.960987);

        for(int i=0; i< places.length; i++) {
            mMap.addMarker(new MarkerOptions().position(places[i]).title("Place "+i));
        }

        mMap.moveCamera(CameraUpdateFactory.newLatLng(places[0]));

        //TODO: Add support for waypoints > 8
        String waypoints = "waypoints=optimize:true"
                + "%7C" + places[1].latitude + "," + places[1].longitude
                + "%7C" + places[2].latitude + "," + places[2].longitude
                + "%7C" + places[3].latitude + "," + places[3].longitude ;
        String origin = "origin="+places[0].latitude+","+places[0].longitude;
        String dest = "destination="+places[4].latitude+","+places[4].longitude;
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
                //String jsonString = EntityUtils.toString(response.getEntity());
                directionsJSONObject = new JSONObject(EntityUtils.toString(response.getEntity()));
                drawRoute();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    private void drawRoute() {
        try {
            //JSONObject json = new JSONObject(jsonString);
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


            /*PolylineOptions polylineOptions = new PolylineOptions();
            polylineOptions.addAll(points);
            polylineOptions.width(5);
            polylineOptions.color(Color.GREEN);
            polylineOptions.geodesic(true);
            mMap.addPolyline(polylineOptions);*/
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /*private LatLng[] decodePolylines(String encodedPolylines) {
        int[] ascii2int = new int[encodedPolylines.length()];
        for(int i=0; i<encodedPolylines.length(); i++) {
            ascii2int[i] = (int)encodedPolylines.charAt(i) - 63;
            ascii2int[i] &= 0x1f;
        }
        for (int i=0; i<ascii2int.length/2; i++) {
            int temp = ascii2int[i];
            ascii2int[i] = ascii2int[ascii2int.length-i-1];
            ascii2int[ascii2int.length-i-1] = temp;
        }
        for (int x : ascii2int) {
            Log.d("DECODE", Integer.toBinaryString(x)+" ");
        }
        return new LatLng[0];
    }*/

    private ArrayList<LatLng> decodePolylines(String encodedPolylines) {
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
    }

    private void displayDirections(JSONObject route) {
        ExpandableListView directionsListView = (ExpandableListView) findViewById(R.id.directions);
        DirectionsExpandableListAdapter adapter = new DirectionsExpandableListAdapter(getBaseContext(), route);
        directionsListView.setAdapter(adapter);
    }
}
