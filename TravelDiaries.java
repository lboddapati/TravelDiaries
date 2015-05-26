package com.example.tonia.projecttraveldiaries;

/**
 * Created by Tonia on 5/16/15.
 */
import android.app.Dialog;
import android.app.FragmentManager;
import android.content.Context;
import android.graphics.Color;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.AutoCompleteTextView;
import android.content.Intent;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class TravelDiaries extends FragmentActivity {
    GoogleMap mGoogleMap;

    AutoCompleteTextView atvPlaces_start;
    AutoCompleteTextView atvPlaces_end;
    AutocompletePlacesTask placesTask;
    AutocompleteTask autoCompleteTask;
    LinearLayout linearLayout;
    Button btn_getRoute;
    ArrayList<LatLng> waypoints;

    boolean restaurant = false;
    boolean tourist = false;
    boolean bars = false;

    double mLatitude_start = 0;
    double mLongitude_start = 0;
    double mLatitude_end = 0;
    double mLongitude_end = 0;

    ListView listView;
    ArrayList<String> listItems;
    ArrayAdapter<String> adapter;

    public void onCheckboxClicked(View v) {
        boolean checked = ((CheckBox) v).isChecked();

        switch (v.getId()) {
            case R.id.chk_restaurants:
                if (checked)
                    restaurant = true;
                break;
            case R.id.chk_bars:
                if (checked)
                    bars = true;
                break;
            case R.id.chk_tourist:
                if (checked)
                    tourist = true;
                break;
            default:
                break;

        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        btn_getRoute = (Button) findViewById(R.id.btn_getRoute);
        atvPlaces_start = (AutoCompleteTextView) findViewById(R.id.autoCompleteTextView_start);
        atvPlaces_end = (AutoCompleteTextView) findViewById(R.id.autoCompleteTextView_end);
        btn_getRoute.setVisibility(View.INVISIBLE);

        // Add textview 1
        TextView textView1 = new TextView(this);
        textView1.setText("programmatically created TextView1");
        textView1.setTextSize(100);

         atvPlaces_start.addTextChangedListener(new TextWatcher() {

             @Override
             public void onTextChanged(CharSequence s, int start, int before, int count) {
                 placesTask = new AutocompletePlacesTask();
                 placesTask.execute(s.toString());
             }

             @Override
             public void beforeTextChanged(CharSequence s, int start, int count,
                                           int after) {
                 // TODO Auto-generated method stub
             }

             @Override
             public void afterTextChanged(Editable s) {
                 // TODO Auto-generated method stub
             }


         });
        atvPlaces_start.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int index, long id) {

                SimpleAdapter adapter = (SimpleAdapter) arg0.getAdapter();

                HashMap<String, String> hm = (HashMap<String, String>) adapter.getItem(index);
                System.out.println(hm.get("place_id") + "Place");
                FunctionLatLong(hm.get("place_id"));
            }
        });

        atvPlaces_end.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                placesTask = new AutocompletePlacesTask();
                placesTask.execute(s.toString());
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count,
                                          int after) {
                // TODO Auto-generated method stub
            }

            @Override
            public void afterTextChanged(Editable s) {
                // TODO Auto-generated method stub
            }
        });

        atvPlaces_end.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int index, long id) {

                SimpleAdapter adapter = (SimpleAdapter) arg0.getAdapter();

                HashMap<String, String> hm = (HashMap<String, String>) adapter.getItem(index);
                System.out.println(hm.get("place_id") + "Place");
                FunctionLatLong(hm.get("place_id"));
            }
        });

        listView = (ListView) findViewById(R.id.listView2);
        listItems = new ArrayList<String>();
        adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, listItems);
        listView.setAdapter(adapter);

        final Button btnFind;
        // Getting reference to Find Button
        btnFind = (Button) findViewById(R.id.btn_find);

        // Getting Google Play availability status
        int status = GooglePlayServicesUtil.isGooglePlayServicesAvailable(getBaseContext());

        if (status != ConnectionResult.SUCCESS) { // Google Play Services are not available

            int requestCode = 10;
            Dialog dialog = GooglePlayServicesUtil.getErrorDialog(status, this, requestCode);
            dialog.show();

        } else {

            // Google Play Services are available

            // Getting reference to the SupportMapFragment

            final SupportMapFragment fragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
            System.out.println("Fragment Visibility"+fragment.isVisible());

            // Getting Google Map
            mGoogleMap = fragment.getMap();
            fragment.getView().setVisibility(View.INVISIBLE);

          //  locationManager.requestLocationUpdates(provider, 20000, 0, this);

            // Setting click event lister for the find button
            btnFind.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {

                    btn_getRoute.setVisibility(View.VISIBLE);
                    btnFind.setVisibility(View.INVISIBLE);
                    fragment.getView().setVisibility(v.VISIBLE);

                    StringBuilder type = new StringBuilder("");
                    if (restaurant)
                        type.append("&types=restaurant");
                    if (bars)
                        type.append("&types=bar");
                    if (tourist)
                        type.append("&types=zoo");
                    final String YOUR_API_KEY = "AIzaSyCgPTOVtLLdvJYWro4NJWeuUcEtQLwKc2w";

                    StringBuilder sb = new StringBuilder("https://maps.googleapis.com/maps/api/place/nearbysearch/json?");
                    sb.append("location=" + mLatitude_start + "," + mLongitude_start);
                    sb.append("&radius=5000");
                    sb.append("&type=" + type);
                    sb.append("&sensor=true");
                    sb.append("&key=" + YOUR_API_KEY);

                    // Creating a new non-ui thread task to download json data
                    PlacesTask placesTask = new PlacesTask();

                    // Invokes the "doInBackground()" method of the class PlaceTask
                    placesTask.execute(sb.toString());

                }
            });


            // Enabling MyLocation in Google Map
            mGoogleMap.setMyLocationEnabled(true);

            // Getting LocationManager object from System Service LOCATION_SERVICE
            LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

            // Creating a criteria object to retrieve provider
            Criteria criteria = new Criteria();

            // Getting the name of the best provider
            String provider = locationManager.getBestProvider(criteria, true);

            // Getting Current Location From GPS
            Location location = locationManager.getLastKnownLocation(provider);

            if (location != null) {
                onLocationChanged(location);
            }

        }
        btn_getRoute.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), routeActivity.class);
                intent.putExtra("latitude_start",mLatitude_start);
                /*intent.putExtra("longitude_start",mLongitude_start);
                intent.putExtra("latitude_end",mLatitude_end);
                intent.putExtra("longitude_end",mLongitude_end);
                intent.putExtra("waypointsList",waypoints);*/
                startActivity(intent);

            }

        });

    }

    /**
     *A method to get the Latitude and Logitude from Place ID
     */
    public void FunctionLatLong(String PlaceID){
        final String YOUR_API_KEY = "AIzaSyCgPTOVtLLdvJYWro4NJWeuUcEtQLwKc2w";
        StringBuilder sb = new StringBuilder("https://maps.googleapis.com/maps/api/place/details/json?");
        sb.append("&placeid="+PlaceID);
        sb.append("&key=" + YOUR_API_KEY);

        getLatLongTask latLongTask = new getLatLongTask();

        // Invokes the "doInBackground()" method of the class PlaceTask
        latLongTask.execute(sb.toString());


    }

    private class getLatLongTask extends AsyncTask<String, Integer, String> {

        String data = null;

        // Invoked by execute() method of this object
        @Override
        protected String doInBackground(String... url) {
            try {
                data = downloadUrl(url[0]);
            } catch (Exception e) {
                Log.d("Background Task", e.toString());
            }
            return data;
        }

        // Executed after the complete execution of doInBackground() method
        @Override
        protected void onPostExecute(String result) {
            parserLatLongTask LatLongparser = new parserLatLongTask();

            // Start parsing the Google places in JSON format
            // Invokes the "doInBackground()" method of the class ParseTask
            LatLongparser.execute(result);
        }

    }
    private class parserLatLongTask extends AsyncTask<String, Integer, List<HashMap<String, Double>>> {

        JSONObject jObject;

        // Invoked by execute() method of this object
        @Override
        protected List<HashMap<String, Double>> doInBackground(String... jsonData) {

            List<HashMap<String, Double>> latLong = null;
            PlaceJSONParser placeJsonParser = new PlaceJSONParser();

            try {
                jObject = new JSONObject(jsonData[0]);
                System.out.println("Inside parse task");
                /** Getting the parsed data as a List construct */
                latLong = placeJsonParser.parseLatLong(jObject);

            } catch (Exception e) {
                Log.d("Exception", e.toString());
            }
            System.out.println(latLong);
            return latLong;
        }

        @Override
        protected void onPostExecute(List<HashMap<String, Double>> result) {

            mLatitude_start = result.get(0).get("latitude");
            mLongitude_start = result.get(0).get("longitude");

        }
    }


    /**
     * A method to download json data from url
     */
    private String downloadUrl(String strUrl) throws IOException {
        String data = "";
        InputStream iStream = null;
        HttpURLConnection urlConnection = null;
        try {
            URL url = new URL(strUrl);

            // Creating an http connection to communicate with url
            urlConnection = (HttpURLConnection) url.openConnection();

            // Connecting to url
            urlConnection.connect();

            // Reading data from url
            iStream = urlConnection.getInputStream();

            BufferedReader br = new BufferedReader(new InputStreamReader(iStream));

            StringBuffer sb = new StringBuffer();

            String line = "";
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }

            data = sb.toString();

            br.close();

        } catch (Exception e) {
            Log.d("Exception downloading", e.toString());
        } finally {
            iStream.close();
            urlConnection.disconnect();
        }

        return data;
    }

    // Fetches all places from GooglePlaces AutoComplete Web Service
    private class AutocompletePlacesTask extends AsyncTask<String, Void, String>{

        @Override
        protected String doInBackground(String... place) {
            // For storing data from web service
            String data = "";

            // Obtain browser key from https://code.google.com/apis/console
            String key = "key=AIzaSyCgPTOVtLLdvJYWro4NJWeuUcEtQLwKc2w";

            String input="";

            try {
                input = "input=" + URLEncoder.encode(place[0], "utf-8");
            } catch (UnsupportedEncodingException e1) {
                e1.printStackTrace();
            }

            // place type to be searched
            String types = "types=geocode";

            // Sensor enabled
            String sensor = "sensor=false";

            // Building the parameters to the web service
            String parameters = input+"&"+types+"&"+sensor+"&"+key;

            // Output format
            String output = "json";

            // Building the url to the web service
            String url = "https://maps.googleapis.com/maps/api/place/autocomplete/"+output+"?"+parameters;

            try{
                // Fetching the data from we service
                data = downloadUrl(url);
            }catch(Exception e){
                Log.d("Background Task",e.toString());
            }
            return data;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            // Creating ParserTask
            autoCompleteTask = new AutocompleteTask();

            // Starting Parsing the JSON string returned by Web Service
            autoCompleteTask.execute(result);
        }
    }

    /** A class to parse the Google Places in JSON format */
    private class AutocompleteTask extends AsyncTask<String, Integer, List<HashMap<String,String>>>{

        JSONObject jObject;

        @Override
        protected List<HashMap<String, String>> doInBackground(String... jsonData) {

            List<HashMap<String, String>> places = null;

            PlaceJSONParser placeJsonParser = new PlaceJSONParser();

            try{
                jObject = new JSONObject(jsonData[0]);

                // Getting the parsed data as a List construct
                places = placeJsonParser.parseAutoComplete(jObject);

            }catch(Exception e){
                Log.d("Exception",e.toString());
            }
            return places;
        }

        @Override
        protected void onPostExecute(List<HashMap<String, String>> result) {

            System.out.println(result);
            String[] from = new String[] { "description","place_id"};
            int[] to = new int[] { android.R.id.text1 };
            // Creating a SimpleAdapter for the AutoCompleteTextView
            SimpleAdapter adapter = new SimpleAdapter(getBaseContext(), result, android.R.layout.simple_list_item_1, from, to);

            // Setting the adapter
            atvPlaces_start.setAdapter(adapter);
            // Setting the adapter
            atvPlaces_end.setAdapter(adapter);

        }

    }

    /**
     * A class, to download Google Places
     */
    private class PlacesTask extends AsyncTask<String, Integer, String> {

        String data = null;

        // Invoked by execute() method of this object
        @Override
        protected String doInBackground(String... url) {
            try {
                data = downloadUrl(url[0]);
            } catch (Exception e) {
                Log.d("Background Task", e.toString());
            }
            return data;
        }

        // Executed after the complete execution of doInBackground() method
        @Override
        protected void onPostExecute(String result) {
            ParserTask parserTask = new ParserTask();

            // Start parsing the Google places in JSON format
            // Invokes the "doInBackground()" method of the class ParseTask
            parserTask.execute(result);
        }

    }

    /**
     * A class to parse the Google Places in JSON format
     */
    private class ParserTask extends AsyncTask<String, Integer, List<HashMap<String, String>>> {

        JSONObject jObject;

        // Invoked by execute() method of this object
        @Override
        protected List<HashMap<String, String>> doInBackground(String... jsonData) {

            List<HashMap<String, String>> places = null;
            PlaceJSONParser placeJsonParser = new PlaceJSONParser();

            try {
                jObject = new JSONObject(jsonData[0]);
                System.out.println("Inside parse task");
                /** Getting the parsed data as a List construct */
                places = placeJsonParser.parse(jObject);

            } catch (Exception e) {
                Log.d("Exception", e.toString());
            }

            return places;
        }

        // Executed after the complete execution of doInBackground() method
        protected void onPostExecute(List<HashMap<String, String>> list) {


            mGoogleMap.clear();
            for(int i=0;i<list.size();i++){
                // Clears all the existing markers


                // Creating a marker
                MarkerOptions markerOptions = new MarkerOptions();

                // Getting a place from the places list
                HashMap<String, String> hmPlace = list.get(i);

                // Getting latitude of the place
                double lat = Double.parseDouble(hmPlace.get("lat"));

                // Getting longitude of the place
                double lng = Double.parseDouble(hmPlace.get("lng"));

                // Getting name
                final String name = hmPlace.get("place_name");

                // Getting vicinity
                final String vicinity = hmPlace.get("vicinity");

                LatLng latLng = new LatLng(lat, lng);

                // Setting the position for the marker
                markerOptions.position(latLng);

                // Setting the title for the marker.
                //This will be displayed on taping the marker
                markerOptions.title(name + " : " + vicinity);

                // Placing a marker on the touched position
                mGoogleMap.addMarker(markerOptions);
                System.out.println("name," + name + ",Vicinity" + vicinity);


            }
            mGoogleMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
                @Override
                public void onInfoWindowClick(Marker marker) {
                    System.out.println("Adding a text");
                        /*Intent intent = new Intent(MapActivity.this, OtherActivity.class);
                        startActivity(intent);*/
                    waypoints= new ArrayList<LatLng>();
                    listItems.add(marker.getTitle());
                    System.out.println(marker.getPosition());

                    LatLng point = marker.getPosition();
                    System.out.println(point);
                    waypoints.add(point);

                    adapter.notifyDataSetChanged();

                }
            });
        }

    }



    public void onLocationChanged(Location location) {
        mLatitude_start = location.getLatitude();
        mLongitude_start = location.getLongitude();
        LatLng latLng = new LatLng(mLatitude_start, mLongitude_start);

        mGoogleMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        mGoogleMap.animateCamera(CameraUpdateFactory.zoomTo(12));

    }




/*
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }
    */
}
