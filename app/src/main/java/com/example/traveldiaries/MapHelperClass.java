package com.example.traveldiaries;

import android.graphics.Color;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by lasyaboddapati on 5/28/15.
 */

/**
 * Helper Class for performing Map related functions like:
 * - Get optimal route.
 * - Draw the route on map.
 * - Draw markers on map.
 * - Decode polylines.
 */
public class MapHelperClass {

    /**
     * Method to get the optimal route for a given list of places. The method takes a List of LatLng's
     * of the places. The first & last place in the list are considered the start and destination places
     * respectively. All other points are considered waypoints. The method performs an Http Request to Google
     * maps' directions api and returns a JSONObject which contains the details of the route. This method only
     * returns the first route from the resultant JSON response. Please refer to Google maps' directions response
     * object for a detailed description of the JSONObject.
     *
     * NOTE: Currently only upto 8 waypoints are supported. This is because Google maps allows only a maximum of
     * 8 waypoints.
     *
     * @param places The list of places to visit.
     * @return The route as a JSONObject.
     */
    public static JSONObject getRoute(List<LatLng> places) {
        String origin = "origin="+places.get(0).latitude+","+places.get(0).longitude;
        String dest = "destination="+places.get(places.size()-1).latitude+","+places.get(places.size()-1).longitude;
        String sensor = "sensor=false";
        String units = "units=metric";
        String output = "json";
        String mode = "mode=driving";
        String params = origin+ "&"+ dest+ "&"+ sensor +"&"+ units +"&"+ mode ;

        //TODO: Add support for waypoints > 8
        if(places.size()>2) {
            String waypoints = "waypoints=optimize:true";
            for (int i = 1; i < places.size() - 1; i++) {
                waypoints = waypoints + "%7C" + places.get(i).latitude + "," + places.get(i).longitude;
            }
            params+="&"+ waypoints;
        }
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
                return result.getJSONArray("routes").getJSONObject(0);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;

    }

    /**
     * Method to draw the route on the map. The method extracts the overview polyline from the input route
     * JSONObject and decodes it to obtain the individual polylines to plot on the map.
     *
     * @param route The JSONObject which contains the details of the route.
     * @param mMap  The map object on which the route is to be plotted.
     * @return The list of decoded polylines.
     */
    public static ArrayList<Polyline> drawRoute(JSONObject route, GoogleMap mMap) {
        ArrayList<Polyline> polylines = new ArrayList<Polyline>();

        try {
            // Extract the encoded overview polyline.
            JSONObject overviewPolyline = route.getJSONObject("overview_polyline");
            String encodedPolylines = overviewPolyline.getString("points");

            // Get the list of points corresponding to the encoded polyline.
            ArrayList<LatLng> points = decodePolylines(encodedPolylines);

            // Draw polylines between all the points.
            for(int i = 0; i<points.size()-1;i++){
                LatLng src= points.get(i);
                LatLng dest= points.get(i+1);
                Polyline line = mMap.addPolyline(new PolylineOptions()
                        .add(new LatLng(src.latitude, src.longitude), new LatLng(dest.latitude, dest.longitude))
                        .width(10)
                        .color(Color.parseColor("#33CCFF"))
                        .geodesic(true));
                polylines.add(line);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        // Return the list of drawn polylines.
        return polylines;
    }

    /**
     * Method that takes in a List of LatLngs and draw markers on the map at those locations.
     *
     * @param points The List of points where markers must be drawn.
     * @param title The text to be displayed in the info window of the marked points.
     * @param mMap The map object on which the markers must be plotted.
     * @param color The color of the markers. If it is null, default color (RED) will be used.
     */
    public static void drawMarkers(List<LatLng> points, List<String> title, GoogleMap mMap, Float color) {
        if(points!= null && points.size()>0) {
            MarkerOptions options = new MarkerOptions();
            if(color == null) {
                options.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
            } else {
                options.icon(BitmapDescriptorFactory.defaultMarker(color));
            }
            for (int i = 0; i < points.size(); i++) {
                mMap.addMarker(options.position(points.get(i)).title(title.get(i)));
            }
        }
    }

    /**
     * Method that takes in a List of LatLngs and draw markers on the map at those locations.
     *
     * @param points The List of points where markers must be drawn.
     * @param mMap The map object on which the markers must be plotted.
     * @param color The color of the markers. If it is null, default color (RED) will be used.
     */
    public static void drawMarkers(List<LatLng> points, GoogleMap mMap, Float color) {
        if(points!= null && points.size()>0) {
            MarkerOptions options = new MarkerOptions();
            if(color == null) {
                options.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
            } else {
                options.icon(BitmapDescriptorFactory.defaultMarker(color));
            }
            for (int i = 0; i < points.size(); i++) {
                mMap.addMarker(options.position(points.get(i)));
            }
        }
    }

    /**
     * Method to decode the encoded overview Polyline. Please check Google for details on how
     * encoding and decoding is performed.
     * @param encodedPolylines The encoded polyline.
     * @return A list of LatLngs which correspond to the polyline points.
     */
    public static ArrayList<LatLng> decodePolylines(String encodedPolylines) {
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
}
