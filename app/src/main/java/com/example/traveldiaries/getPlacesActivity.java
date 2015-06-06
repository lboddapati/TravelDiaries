package com.example.traveldiaries;

/**
 * Created by Tonia on 5/16/15.
 */
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.ColorDrawable;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.v4.app.FragmentActivity;
import android.text.Editable;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.AutoCompleteTextView;
import android.content.Intent;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;

import org.json.JSONException;
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

public class getPlacesActivity extends FragmentActivity {
    private GoogleMap mGoogleMap;
    private String YOUR_API_KEY;

    private AutoCompleteTextView atvPlaces_start;
    private AutoCompleteTextView atvPlaces_end;
    private AutocompletePlacesTask placesTask;
    private AutocompleteTask autoCompleteTask;
    private ImageButton btnStartTrip;
    //private Button btnFind;
    //private CheckBox chk_restaurants;
    //private CheckBox chk_bars;
    //private CheckBox chk_tourist;
    private HorizontalScrollView categories;
    private Button restaurantsBtn;
    private Button nightLifeBtn;
    private Button fuelBtn;
    private Button touristBtn;
    private Button popularBtn;
    private Button barsBtn;

    private JSONObject routesJSON;// = new JSONObject();
    private ArrayList<LatLng> places = new ArrayList<LatLng>();
    private ArrayList<LatLng> points = new ArrayList<LatLng>();
    private ArrayList<Polyline> polylines = new ArrayList<Polyline>();
    private ArrayList<String> selectedPlacesNames = new ArrayList<String>();
    private ArrayList<String> selectedPlacesAddress = new ArrayList<String>();

    private boolean restaurant = false;
    private boolean tourist = false;
    private boolean bars = false;
    private boolean nightLife = false;
    private boolean popular = false;
    private boolean fuel = false;

    private double mLatitude_start = 0;
    private double mLongitude_start = 0;
    private double mLatitude_end = 0;
    private double mLongitude_end = 0;

    private ListView listView;
    private ArrayList<String> listItems;
    private ArrayAdapter<String> adapter;

    /*CompoundButton.OnCheckedChangeListener checkBoxListener = new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            switch (buttonView.getId()) {
                case R.id.chk_bars:
                    bars = isChecked;
                    break;
                case R.id.chk_restaurants:
                    restaurant = isChecked;
                    break;
                case R.id.chk_tourist:
                    tourist = isChecked;
                    break;
                default:
                    break;
            }
        }
    };*/

    View.OnClickListener categoriesClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            boolean selected = false;
            Button buttonView = (Button) v;
            switch (buttonView.getId()) {
                case R.id.popular:
                    //Toast.makeText(getPlacesActivity.this, "popular button clicked", Toast.LENGTH_SHORT).show();
                    popular = !(popular);
                    selected = popular;
                    break;
                case R.id.restaurants:
                    restaurant = !(restaurant);
                    //Toast.makeText(getPlacesActivity.this, "restaurants button clicked:: "+restaurant, Toast.LENGTH_SHORT).show();
                    selected = restaurant;
                    break;
                case R.id.nightLife:
                    //Toast.makeText(getPlacesActivity.this, "nightLife button clicked", Toast.LENGTH_SHORT).show();
                    nightLife = !(nightLife);
                    selected = nightLife;
                    break;
                case R.id.bars:
                    bars = !(bars);
                    //Toast.makeText(getPlacesActivity.this, "bars button clicked:: "+bars, Toast.LENGTH_SHORT).show();
                    selected = bars;
                    break;
                case R.id.tourist:
                    //Toast.makeText(getPlacesActivity.this, "tourist button clicked", Toast.LENGTH_SHORT).show();
                    tourist = !(tourist);
                    selected = tourist;
                    break;
                case R.id.fuelUp:
                    //Toast.makeText(getPlacesActivity.this, "fuelUp button clicked", Toast.LENGTH_SHORT).show();
                    fuel = !(fuel);
                    selected = fuel;
                    break;
                default:
                    break;
            }

            if(selected) {
                //Toast.makeText(getPlacesActivity.this, "button selected", Toast.LENGTH_SHORT).show();
                setSelection(buttonView);
            } else {
                //Toast.makeText(getPlacesActivity.this, "button de selected", Toast.LENGTH_SHORT).show();
                clearSelection(buttonView);
            }

            getPlaces();
        }
    };

    private void clearSelection(Button buttonView) {
        int textColor = buttonView.getCurrentTextColor();
        buttonView.setBackgroundColor(textColor);
        buttonView.setTextColor(Color.WHITE);
        buttonView.getCompoundDrawables()[1].clearColorFilter();
    }

    private void setSelection(Button buttonView) {
        int buttonColor = ((ColorDrawable) buttonView.getBackground()).getColor();
        buttonView.getCompoundDrawables()[1].setColorFilter(buttonColor, PorterDuff.Mode.MULTIPLY);
        buttonView.setTextColor(buttonColor);
        buttonView.setBackgroundColor(Color.GRAY);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        if (!isNetworkAvailable()) {
            setContentView(R.layout.no_network_error);
            Button refreshButton = (Button) findViewById(R.id.refresh);
            refreshButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    getPlacesActivity.this.recreate();
                }
            });

        } else {
            setContentView(R.layout.activity_get_places);

            //final int STATUS_BAR_COLOR = getResources().getColor(R.color.Blue);
            //getWindow().setStatusBarColor(STATUS_BAR_COLOR);

            YOUR_API_KEY = getResources().getString(R.string.google_places_key);
            Log.d("API_KEY", YOUR_API_KEY);

            if (android.os.Build.VERSION.SDK_INT > 9) {
                StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
                StrictMode.setThreadPolicy(policy);
            }

            btnStartTrip = (ImageButton) findViewById(R.id.btn_StartTrip);
            btnStartTrip.setVisibility(View.GONE);
            categories = (HorizontalScrollView) findViewById(R.id.categories);
            categories.setVisibility(View.GONE);
            listView = (ListView) findViewById(R.id.listView2);
            listView.setVisibility(View.GONE);
            //btnFind = (Button) findViewById(R.id.btn_find);
            //btnFind.setEnabled(false);
            atvPlaces_start = (AutoCompleteTextView) findViewById(R.id.autoCompleteTextView_start);
            atvPlaces_end = (AutoCompleteTextView) findViewById(R.id.autoCompleteTextView_end);
            //chk_tourist = (CheckBox) findViewById(R.id.chk_tourist);
            //chk_restaurants = (CheckBox) findViewById(R.id.chk_restaurants);
            //chk_bars = (CheckBox) findViewById(R.id.chk_bars);
            restaurantsBtn = (Button) findViewById(R.id.restaurants);
            touristBtn = (Button) findViewById(R.id.tourist);
            fuelBtn = (Button) findViewById(R.id.fuelUp);
            popularBtn = (Button) findViewById(R.id.popular);
            nightLifeBtn = (Button) findViewById(R.id.nightLife);
            barsBtn = (Button) findViewById(R.id.bars);

            atvPlaces_start.addTextChangedListener(new TextWatcher() {
                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    placesTask = new AutocompletePlacesTask();
                    placesTask.execute(s.toString());
                }

                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                    // TODO Auto-generated method stub
                }

                @Override
                public void afterTextChanged(Editable s) {
                    if (atvPlaces_start.getText().toString().isEmpty()) {
                        //btnFind.setEnabled(false);
                        //categories.setVisibility(View.GONE);
                        setVisibility(categories, View.GONE);
                        btnStartTrip.setVisibility(View.GONE);
                        clearPlaces();
                        clearAllSelections();
                    } else {
                        if (atvPlaces_start.getText().toString() == atvPlaces_end.getText().toString()) {
                            Toast toast = Toast.makeText(getBaseContext(), "Both origin and destination are same. Please change your selection!", Toast.LENGTH_SHORT);
                            toast.show();
                        }
                    }
                }
            });

            atvPlaces_start.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> arg0, View arg1, int index, long id) {

                    SimpleAdapter adapter = (SimpleAdapter) arg0.getAdapter();

                    HashMap<String, String> hm = (HashMap<String, String>) adapter.getItem(index);
                    System.out.println(hm.get("place_id") + "Place");

                    if (atvPlaces_end.getText().toString().isEmpty()) {
                        //btnFind.setEnabled(false);
                        //categories.setVisibility(View.GONE);
                        setVisibility(categories, View.GONE);
                        btnStartTrip.setVisibility(View.GONE);
                        clearPlaces();
                        clearAllSelections();
                    } else {
                        if (atvPlaces_start.getText().toString().equalsIgnoreCase(atvPlaces_end.getText().toString())) {
                            Toast toast = Toast.makeText(getPlacesActivity.this, "Both origin and destination are same. Please change your selection!", Toast.LENGTH_SHORT);
                            toast.show();
                        }
                        else {
                            FunctionLatLong(hm.get("place_id"));
                            //btnFind.setEnabled(true);
                            //categories.setVisibility(View.VISIBLE);
                            setVisibility(categories, View.VISIBLE);
                            getPlaces();
                        }

                    }
                }
            });

            atvPlaces_end.addTextChangedListener(new TextWatcher() {
                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    placesTask = new AutocompletePlacesTask();
                    placesTask.execute(s.toString());
                }

                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                    // TODO Auto-generated method stub
                }

                @Override
                public void afterTextChanged(Editable s) {
                    if (atvPlaces_end.getText().toString().isEmpty()) {
                        //btnFind.setEnabled(false);
                        //categories.setVisibility(View.GONE);
                        setVisibility(categories, View.GONE);
                        btnStartTrip.setVisibility(View.GONE);
                        clearPlaces();
                        clearAllSelections();
                    }

                }
            });

            atvPlaces_end.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> arg0, View arg1, int index, long id) {

                    SimpleAdapter adapter = (SimpleAdapter) arg0.getAdapter();

                    HashMap<String, String> hm = (HashMap<String, String>) adapter.getItem(index);
                    System.out.println(hm.get("place_id") + "Place");
                    if (atvPlaces_start.getText().toString().isEmpty()) {
                        //btnFind.setEnabled(false);
                        //categories.setVisibility(View.GONE);
                        setVisibility(categories, View.GONE);
                        btnStartTrip.setVisibility(View.GONE);
                        clearPlaces();
                        clearAllSelections();
                    } else {
                        //Toast.makeText(getPlacesActivity.this,"Destination",Toast.LENGTH_SHORT).show();
                        if (atvPlaces_end.getText().toString().equalsIgnoreCase(atvPlaces_start.getText().toString())) {
                            Toast toast = Toast.makeText(getPlacesActivity.this, "Both origin and destination are same. Please change your selection!", Toast.LENGTH_SHORT);
                            toast.show();
                        }
                        else {
                            FunctionLatLong_end(hm.get("place_id"));

                            //btnFind.setEnabled(true);
                            //categories.setVisibility(View.VISIBLE);
                            setVisibility(categories, View.VISIBLE);
                            getPlaces();
                        }
                    }
                }
            });

            listItems = new ArrayList<String>();
            adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, listItems);
            listView.setAdapter(adapter);

            // set up the map
            setUpMapIfNeeded();

            //chk_bars.setOnCheckedChangeListener(checkBoxListener);
            //chk_restaurants.setOnCheckedChangeListener(checkBoxListener);
            //chk_tourist.setOnCheckedChangeListener(checkBoxListener);


        btnStartTrip.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {


                View view = View.inflate(getPlacesActivity.this, R.layout.dialogbox, null);

                final AlertDialog.Builder dialogBox = new AlertDialog.Builder(getPlacesActivity.this);
                dialogBox.setTitle("Trip Name");
                dialogBox.setView(view);

                final EditText input = (EditText) view.findViewById(R.id.dialogboxText);
                dialogBox.setPositiveButton("Start Trip", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int id) {
                        String tripName = input.getText().toString();

                        if ((tripName.isEmpty()) || (tripName.matches("^\\s*$"))) {
                            input.setError("Trip Name cannot be empty. Please enter some text");
                            Toast toast = Toast.makeText(getBaseContext(), "Trip name cannot be empty!", Toast.LENGTH_SHORT);
                            toast.show();

                        } else {
                            Intent intent = new Intent(getApplicationContext(), StartTripActivity.class);
                            intent.putParcelableArrayListExtra("latLngs", places);
                            intent.putStringArrayListExtra("names", selectedPlacesNames);
                            intent.putStringArrayListExtra("address", selectedPlacesAddress);
                            intent.putExtra("route", routesJSON.toString());
                            intent.putExtra("tripName", tripName);
                            startActivity(intent);
                            finish();
                        }

                    }
                });
                dialogBox.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialogInterface, int id) {
                        dialogInterface.cancel();
                    }

                });
                dialogBox.create().show();
            }

        });

        //chk_bars.setOnCheckedChangeListener(checkBoxListener);
        //chk_restaurants.setOnCheckedChangeListener(checkBoxListener);
        //chk_tourist.setOnCheckedChangeListener(checkBoxListener);

        restaurantsBtn.setOnClickListener(categoriesClickListener);
        popularBtn.setOnClickListener(categoriesClickListener);
        fuelBtn.setOnClickListener(categoriesClickListener);
        nightLifeBtn.setOnClickListener(categoriesClickListener);
        touristBtn.setOnClickListener(categoriesClickListener);
        barsBtn.setOnClickListener(categoriesClickListener);
    }
    }

    private void clearAllSelections() {
        if(popular) {
            popular = false;
            clearSelection(popularBtn);
        }
        if(restaurant) {
            restaurant = false;
            clearSelection(restaurantsBtn);
        }
        if(nightLife) {
            nightLife = false;
            clearSelection(nightLifeBtn);
        }
        if(bars) {
            bars = false;
            clearSelection(barsBtn);
        }
        if(fuel) {
            fuel = false;
            clearSelection(fuelBtn);
        }
        if(tourist) {
            tourist = false;
            clearSelection(touristBtn);
        }
    }

    private void clearPlaces() {
        mGoogleMap.clear();
        listItems.clear();
        places.clear();
        selectedPlacesNames.clear();
        selectedPlacesAddress.clear();
        adapter.notifyDataSetChanged();
        setVisibility(listView, View.GONE);
    }

    private void setVisibility(View view, int visibility) {
        if (view.getVisibility() != visibility) {
            Animation animation;
            if(visibility == View.VISIBLE) {
                animation = AnimationUtils.loadAnimation(getPlacesActivity.this, R.anim.swipe_top_to_bottom);
                view.startAnimation(animation);
                view.setVisibility(visibility);
            } else if (visibility == View.GONE) {
                animation = AnimationUtils.loadAnimation(getPlacesActivity.this, R.anim.swipe_bottom_to_top);
                view.startAnimation(animation);
                view.setVisibility(visibility);
            }
        }
    }

    private void getPlaces() {
        clearPlaces();

        setParameters(mLatitude_start, mLongitude_start, mLatitude_end, mLongitude_end);

        mGoogleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(places.get(0), 11.0f));
        // Differentiate start and end location markers with different colors. Start = Green, End = Red.
        mGoogleMap.addMarker(new MarkerOptions().position(places.get(0))
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));
        mGoogleMap.addMarker(new MarkerOptions().position(places.get(1))
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));

        routesJSON = MapHelperClass.getRoute(places);
        if(routesJSON == null) {
            btnStartTrip.setVisibility(View.GONE);
            //Toast.makeText(this, "No route found", Toast.LENGTH_LONG).show();
        } else {
            btnStartTrip.setVisibility(View.VISIBLE);

            polylines = MapHelperClass.drawRoute(routesJSON, mGoogleMap);


            JSONObject overviewPolyline = null;
            try {
                overviewPolyline = routesJSON.getJSONObject("overview_polyline");
                String encodedPolylines = overviewPolyline.getString("points");
                points = MapHelperClass.decodePolylines(encodedPolylines);
                searchPlaceTypes(points);

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

    }

    public void searchPlaceTypes(ArrayList<LatLng> points){
        StringBuilder type = new StringBuilder("&types=");
        if (restaurant)
            type.append("restaurant%7Ccafe%7C");
        if (bars)
            type.append("bar%7C");
        //if (tourist)
        //    type.append("zoo%7Cmuseum%7Cpark%7Cplace_of_worship%7C");
        if (nightLife)
            type.append("night_club%7Ccasino%7C");
        if(fuel)
            type.append("gas_station%7C");

        StringBuilder keyword = new StringBuilder("&keyword=");
        if (tourist)
            keyword.append("tourist%7C");
        if (popular)
            keyword.append("popular%7Clocal+attractions%7C");

        StringBuilder url = new StringBuilder("https://maps.googleapis.com/maps/api/place/nearbysearch/json?");
        url.append("radius=3000");
        url.append("&sensor=true");
        url.append("&key="+YOUR_API_KEY);
        if(restaurant || bars || nightLife || fuel)
            url.append(type.substring(0,type.lastIndexOf("%7C")));
        if(tourist || popular)
            url.append(keyword.substring(0, keyword.lastIndexOf("%7C")));
        url.append("&opennow=true");

        int size = points.size()/20;
        int i=0;
        while(i<points.size()){
            LatLng src = points.get(i);
            StringBuilder sb = new StringBuilder(url);
            sb.append("&location=" + src.latitude + "," + src.longitude);

            // Creating a new non-ui thread task to download json data
            PlacesTask placesTask = new PlacesTask();

            // Invokes the "doInBackground()" method of the class PlaceTask
            placesTask.execute(sb.toString());
            i+=size;

        }
        // to get the last point if skipped.
        if((i-size)<points.size()-1 ){
            LatLng src = points.get(points.size()-1);
            StringBuilder sb = new StringBuilder(url);
            sb.append("&location=" + src.latitude + "," + src.longitude);

            // Creating a new non-ui thread task to download json data
            PlacesTask placesTask = new PlacesTask();

            // Invokes the "doInBackground()" method of the class PlaceTask
            placesTask.execute(sb.toString());

        }
    }

    public void setParameters(Double lat_start, Double long_start, Double lat_end, Double long_end) {
        System.out.println("In set");

        places.add(new LatLng(lat_start, long_start));
        selectedPlacesNames.add(atvPlaces_start.getText().toString().split(",")[0]);
        selectedPlacesAddress.add(atvPlaces_start.getText().toString());

        places.add(new LatLng(lat_end, long_end));
        selectedPlacesNames.add(atvPlaces_end.getText().toString().split(",")[0]);
        selectedPlacesAddress.add(atvPlaces_end.getText().toString());

        System.out.println(places);
    }

    /**
     *A method to get the Latitude and Longitude of place from Place ID
     */
    public void FunctionLatLong(String PlaceID){
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
            if(result != null && result.size()>0) {
                mLatitude_start = result.get(0).get("latitude");
                mLongitude_start = result.get(0).get("longitude");
            }
        }
    }

    /**
     *A method to get the Latitude and Logitude of detination from Place ID
     */
    public void FunctionLatLong_end(String PlaceID){
        StringBuilder sb = new StringBuilder("https://maps.googleapis.com/maps/api/place/details/json?");
        sb.append("&placeid="+PlaceID);
        sb.append("&key=" + YOUR_API_KEY);

        getLatLongTask_end latLongTask = new getLatLongTask_end();

        // Invokes the "doInBackground()" method of the class PlaceTask
        latLongTask.execute(sb.toString());

    }

    private class getLatLongTask_end extends AsyncTask<String, Integer, String> {

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
            parserLatLongTask_end LatLongparser = new parserLatLongTask_end();

            // Start parsing the Google places in JSON format
            // Invokes the "doInBackground()" method of the class ParseTask
            LatLongparser.execute(result);
        }

    }
    private class parserLatLongTask_end extends AsyncTask<String, Integer, List<HashMap<String, Double>>> {

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
            System.out.println("End place"+latLong);
            return latLong;
        }

        @Override
        protected void onPostExecute(List<HashMap<String, Double>> result) {

            if(result != null && result.size()>0) {
                mLatitude_end = result.get(0).get("latitude");
                mLongitude_end = result.get(0).get("longitude");
            }

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
            Log.d("In downloadUrl", url.toString());


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

            //Obtain browser key from https://code.google.com/apis/console
            String key = "key="+YOUR_API_KEY;

            String input="";

            try {
                input = "input=" + URLEncoder.encode(place[0], "utf-8").replace(" ","%20");
                System.out.println("URL ENCODED INPUT = "+ input);
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
            System.out.println("AutocompletePlacesTask onPostExecute "+result);


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

            System.out.println("AutocompleteTask onPostExecute "+result);
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

        /*View view = View.inflate(getPlacesActivity.this, R.layout.activity_get_places, null);
        ProgressBar spinner = new ProgressBar(getPlacesActivity.this);
        ProgressBar spinner = (ProgressBar)findViewById(R.id.progressBar);*/
        JSONObject jObject;

        // Invoked by execute() method of this object
        @Override
        protected List<HashMap<String, String>> doInBackground(String... jsonData) {

            //spinner.setVisibility(view.VISIBLE);
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
            //spinner.setVisibility(view.GONE);

            if (list != null) {

                // Creating a marker
                MarkerOptions markerOptions = new MarkerOptions();
                markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW));

                for (int i = 0; i < list.size(); i++) {
                    // Clears all the existing markers

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

                    //Get ratings
                    final String rating = hmPlace.get("rating");

                    //Get price level
                    final String pricing = hmPlace.get("pricing");

                    LatLng latLng = new LatLng(lat, lng);

                    // Setting the position for the marker
                    markerOptions.position(latLng);
                    String title = name + " : " + vicinity + "\n rating:" +rating+"    " + pricing ;

                    // Setting the title for the marker.
                    //This will be displayed on taping the marker
                    markerOptions.title(title);

                    // Placing a marker on the touched position
                    mGoogleMap.addMarker(markerOptions);
                    System.out.println("name," + name + ",Vicinity" + vicinity);
                }

                mGoogleMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
                    @Override
                    public void onInfoWindowClick(Marker marker) {
                        System.out.println("Adding a text");
                        LatLng point = marker.getPosition();
                        String title = marker.getTitle();

                        // Add place to list when info window is clicked
                        if (!listItems.contains(marker.getTitle())) {
                            if (canAddMorePlaces()) {
                                marker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_VIOLET));
                                listItems.add(marker.getTitle());
                                places.add(1, point);
                                selectedPlacesNames.add(1, title.split(":")[0]);
                                selectedPlacesAddress.add(1, (title.split(":")[1]).split("\n")[0]);

                            } else {
                                Toast.makeText(getPlacesActivity.this, "Only 8 waypoints allowed!", Toast.LENGTH_SHORT).show();
                            }
                        }
                        // If place is already in list and info window is clicked again, remove place from list
                        else {
                            marker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW));
                            listItems.remove(marker.getTitle());
                            places.remove(point);
                            selectedPlacesNames.remove(title.split(":")[0]);
                            selectedPlacesAddress.remove(title.split(":")[1]);
                        }

                        adapter.notifyDataSetChanged();

                        if (listItems.isEmpty()) {
                            setVisibility(listView, View.GONE);
                        } else {
                            setVisibility(listView, View.VISIBLE);
                        }

                        //Clear old route
                        for (Polyline line : polylines)
                            line.remove();

                        routesJSON = MapHelperClass.getRoute(places);
                        polylines = MapHelperClass.drawRoute(routesJSON, mGoogleMap);
                    }
                });
            }
        }

    }

    private boolean canAddMorePlaces() {
        return (places.size() < 10);
    }


    public void onLocationChanged(Location location) {
        mLatitude_start = location.getLatitude();
        mLongitude_start = location.getLongitude();
        LatLng latLng = new LatLng(mLatitude_start, mLongitude_start);

        mGoogleMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        mGoogleMap.animateCamera(CameraUpdateFactory.zoomTo(12));

    }


    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    /**
     * Set up the map object.
     */
    private void setUpMapIfNeeded() {
        if (mGoogleMap == null) {
            // Getting Google Play availability status
            int status = GooglePlayServicesUtil.isGooglePlayServicesAvailable(getBaseContext());

            if (status != ConnectionResult.SUCCESS) { // Google Play Services are not available
                int requestCode = 10;
                Dialog dialog = GooglePlayServicesUtil.getErrorDialog(status, this, requestCode);
                dialog.show();
            } else { // Google Play Services are available

                // Getting reference to the SupportMapFragment
                final SupportMapFragment fragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);

                // Getting Google Map
                mGoogleMap = fragment.getMap();

                // Enabling MyLocation in Google Map
                mGoogleMap.setMyLocationEnabled(true);

                // Getting LocationManager object from System Service LOCATION_SERVICE
                LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

                // Creating a criteria object to retrieve provider
                Criteria criteria = new Criteria();

                // Getting the name of the best provider
                //String provider = locationManager.getBestProvider(criteria, true);
                String provider = null;
                if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                    provider = LocationManager.GPS_PROVIDER;
                } else if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                    provider = LocationManager.NETWORK_PROVIDER;
                }

                if (provider != null) {
                    // Getting Current Location From GPS
                    Location location = locationManager.getLastKnownLocation(provider);

                    if (location != null) {
                        onLocationChanged(location);
                    }
                }

            }
        }
    }

}
