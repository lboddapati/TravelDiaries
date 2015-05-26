package com.example.traveldiaries;

import android.content.Intent;
import android.graphics.Color;
import android.os.StrictMode;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.widget.ExpandableListView;

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
import java.util.HashMap;

public abstract class MapActivity extends FragmentActivity {

    protected GoogleMap mMap; // Might be null if Google Play services APK is not available.
    //protected JSONObject directionsJSONObject;
    //protected ArrayList<LatLng> places;
    //protected JSONObject placesJSON;
    protected int layoutResID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //placesJSON = new JSONObject();
        //places = new ArrayList<LatLng>();   //TODO: Replace with actual places.
        /*places.add(new LatLng(37.258881, -122.032913));
        places.add(new LatLng(37.270220, -122.015403));
        places.add(new LatLng(37.291732, -122.032398));
        places.add(new LatLng(37.287362, -121.944679));
        places.add(new LatLng(37.240092, -121.960987));
        places.add(new LatLng(37.281086, -122.026335));
        places.add(new LatLng(37.260458, -122.029596));
        places.add(new LatLng(37.296109, -122.029596));
        places.add(new LatLng(37.325054, -121.867891));
        places.add(new LatLng(37.285183, -121.939989));*/


        setContentView(layoutResID);

        if (android.os.Build.VERSION.SDK_INT > 9) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }

        setUpMapIfNeeded();
    }

    @Override
    protected void onResume() {
        super.onResume();
        setUpMapIfNeeded();
    }

    /**
     * The Activities which extend this Activity MUST call this method (instead of setContentView(..))
     * in the onCreate method BEFORE calling super.onCreate(...).
     * @param layoutResID The layout file to be set.
     */
    protected void setLayoutFile(int layoutResID) {
        this.layoutResID = layoutResID;
    }

    protected void setUpMapIfNeeded() {
        // Do a null check to confirm that we have not already instantiated the map.
        if (mMap == null) {
            // Try to obtain the map from the SupportMapFragment.
            mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map))
                    .getMap();
            /*// Check if we were successful in obtaining the map.
            if (mMap != null) {
                setUpMap();
            }*/
        }
    }

    /*protected void setUpMap() {
    }*/

    protected JSONObject getRoute(ArrayList<LatLng> places) {
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
        String url = "https://maps.googleapis.com/maps/api/directions/" + output + "?" + params;

        try {
            HttpClient httpClient = new DefaultHttpClient();
            HttpContext localContext = new BasicHttpContext();
            HttpPost postRequest = new HttpPost(url);
            HttpResponse response = httpClient.execute(postRequest, localContext);

            if(response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
                throw new RuntimeException("Failed : HTTP error code : "
                        + response.getStatusLine().getStatusCode());
            } else {
                JSONObject result = new JSONObject(EntityUtils.toString(response.getEntity()));
                //drawRoute(result.getJSONArray("routes").getJSONObject(0));
                return result.getJSONArray("routes").getJSONObject(0);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;

    }

    protected void drawRoute(JSONObject route) {
        try {
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
    }

    protected ArrayList<LatLng> decodePolylines(String encodedPolylines) {
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

    protected void displayDirections(JSONObject route) {
        ExpandableListView directionsListView = (ExpandableListView) findViewById(R.id.directions);
        DirectionsExpandableListAdapter adapter = new DirectionsExpandableListAdapter(getBaseContext(), route);
        directionsListView.setAdapter(adapter);
    }

}
