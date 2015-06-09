package com.example.traveldiaries;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
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
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.List;


/**
 * Activity that tracks the trip while it is being taken (i.e once it is started).
 */
public class StartTripActivity extends FragmentActivity implements LocationListener {
    private GoogleMap mMap;
    private Fragment mapFragment;

    private ParseObject trip;
    private ParseUser user;
    private String tripname;

    private ArrayList<String> names;
    private ArrayList<String> address;
    private ArrayList<LatLng> latLngs;
    private JSONObject route;

    private Location currentLocation;
    private LocationManager locationManager;
    private ExpandableListView directionsListView;
    private ImageButton addPicture;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start_trip);

        if (android.os.Build.VERSION.SDK_INT > 9) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }

        //get the Google Map
        setUpMapIfNeeded();
        setUpLocationListener();

        user = ParseUser.getCurrentUser();

        // Get the details of places selected which were passed through intent from getPlacesActivity.
        Intent intent = getIntent();
        latLngs = intent.getParcelableArrayListExtra("latLngs");
        names = intent.getStringArrayListExtra("names");
        address = intent.getStringArrayListExtra("address");
        tripname = intent.getStringExtra("tripName");
        setTitle(tripname);
        try {
            route = new JSONObject(intent.getStringExtra("route"));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        // Create a 'Trip' parse object and pin it for saving later.
        trip = new ParseObject("Trip");
        trip.put("user", user);
        trip.put("tripName", tripname);
        trip.put("route", route);
        try {
            JSONArray waypointOrder = route.getJSONArray("waypoint_order");
            reorderByOptimizedWaypoints(waypointOrder);
            trip.put("places", getPlacesJSON());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        trip.pinInBackground();

        // Draw markers and route on the map.
        MapHelperClass.drawMarkers(latLngs.subList(1, latLngs.size() - 1), names.subList(1, names.size()-1)
                         ,address.subList(1, address.size() - 1), mMap, BitmapDescriptorFactory.HUE_VIOLET);
        MapHelperClass.drawRoute(route, mMap);
        mMap.addMarker(new MarkerOptions().position(latLngs.get(0))
                .title(names.get(0))
                .snippet(address.get(0))
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)))
                .showInfoWindow();
        mMap.addMarker(new MarkerOptions().position(latLngs.get(latLngs.size() - 1))
                .title(names.get(names.size()-1))
                .snippet(address.get(address.size() - 1))
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLngs.get(0), 13.0f));


        directionsListView = (ExpandableListView) findViewById(R.id.directions);
        addPicture = (ImageButton) findViewById(R.id.addPicture);

        // Display the place to place directions.
        DirectionsExpandableListAdapter adapter = new DirectionsExpandableListAdapter(getBaseContext(), route, names);
        directionsListView.setAdapter(adapter);

        // When this button is clicked, launch the AddPhotoNoteActivity to add photo notes to the trip.
        addPicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent pictureIntent = new Intent(StartTripActivity.this, AddPhotoNoteActivity.class);
                pictureIntent.putExtra("tripname", tripname);
                pictureIntent.putExtra("location", currentLocation);
                startActivity(pictureIntent);
            }
        });
    }

    /**
     * Method that reorders the list of places according to the optimized waypoint order.
     * @param waypointOrder The optimized waypoint order.
     * @throws JSONException
     */
    private void reorderByOptimizedWaypoints(JSONArray waypointOrder) throws JSONException {
        ArrayList<LatLng> reorderedLatLngs = new ArrayList<LatLng>();
        ArrayList<String> reorderedNames = new ArrayList<String>();
        ArrayList<String> reorderedAddress = new ArrayList<String>();

        reorderedLatLngs.add(latLngs.get(0));
        reorderedNames.add(names.get(0));
        reorderedAddress.add(address.get(0));

        // Reorder the waypoints.
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

    /**
     * Upload the photo notes to Parse database.
     */
    private void uploadImagesToCloud() {
        ParseQuery<ParseObject> query = ParseQuery.getQuery("TripPhotoNote");
        query.fromPin(tripname);
        List<ParseObject> parseObjects = null;
        try {
            parseObjects = query.find();

            if(parseObjects !=null && parseObjects.size()>0) {
                ProgressDialog progressDialog = new ProgressDialog(StartTripActivity.this);
                progressDialog.setTitle("Saving Trip");
                progressDialog.setMessage("Uploading images");
                progressDialog.setCancelable(false);
                progressDialog.show();

                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inPreferredConfig = Bitmap.Config.ARGB_8888;
                options.inSampleSize = 2;

                Log.d("UPLOADING IMAGES", tripname + " - FOUND :: " + parseObjects.size());
                byte[] data;
                Bitmap b;
                ByteArrayOutputStream byteStream;
                String imageFilePath;
                ParseFile parseImageFile;
                String filename;

                for (ParseObject obj : parseObjects) {
                    imageFilePath = obj.getString("imageFilePath");
                    b = ImageProcessingHelperClass.decodeSampledBitmapFromFile(imageFilePath, 500000);
                    Log.d("AFTER decodeSampledBitmapFromFile", "Size of image is "+b.getByteCount());
                    if(b != null) {
                        byteStream = new ByteArrayOutputStream();
                        b.compress(Bitmap.CompressFormat.JPEG, 100, byteStream);
                        data = byteStream.toByteArray();

                        int separatorIndex = imageFilePath.lastIndexOf(File.separator);
                        filename = (separatorIndex < 0) ? imageFilePath : imageFilePath.substring(separatorIndex + 1, imageFilePath.length());
                        filename = filename.replace("TravelDiaries", trip.getObjectId());
                        Log.d("UPLOADING IMAGES", "parse file name : "+filename);
                        parseImageFile = new ParseFile(filename, data);
                        parseImageFile.save();
                        obj.remove("imageFilePath");
                        obj.put("photo", parseImageFile);
                        obj.put("trip", trip.getObjectId());
                        obj.save();
                        //progressDialog.incrementProgressBy(100 / parseObjects.size());
                        obj.unpinInBackground(tripname);
                    } else {
                        Log.d("UPLOADING IMAGES",imageFilePath +"  : bit map is null");
                    }
                }
                progressDialog.dismiss();
            }

        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_start_new_trip, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        // When cancel trip is selected, cancel the trip and delete all cached trip details.
        if (id == R.id.action_cancel_trip) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("Are you sure you want to quit the trip? You will loose all trip information");
            builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    trip.unpinInBackground();
                    ParseObject.unpinAllInBackground(tripname);
                    finish();
                }
            });
            builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            builder.create().show();
        }
        // When Finish Trip is clicked, finish the trip and upload the trip details to Parse database.
        else if (id==R.id.action_finish_trip){
            AlertDialog.Builder builder = new AlertDialog.Builder(StartTripActivity.this);
            builder.setMessage("Finish trip?");
            builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    try {
                        trip.save();
                        dialog.dismiss();
                        Toast.makeText(StartTripActivity.this, "Please wait... uploading images", Toast.LENGTH_LONG).show();
                        uploadImagesToCloud();
                        Toast.makeText(StartTripActivity.this, "Trip saved!", Toast.LENGTH_SHORT).show();
                        trip.unpinInBackground();
                        Intent prevTrips = new Intent(StartTripActivity.this, PreviousTrip.class);
                        prevTrips.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(prevTrips);
                        finish();
                    } catch (ParseException e) {
                        e.printStackTrace();
                        Toast.makeText(StartTripActivity.this, "Error saving trip! Try again", Toast.LENGTH_LONG).show();
                    }
                }
            });
            builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            builder.create().show();
        }
        else if (id == R.id.action_view_directions) {
            Animation animation;
            if (directionsListView.getVisibility() == View.GONE) {
                animation = AnimationUtils.loadAnimation(StartTripActivity.this, R.anim.swipe_right_to_left);
                mapFragment.getView().setVisibility(View.GONE);
                addPicture.setVisibility(View.GONE);
                directionsListView.startAnimation(animation);
                directionsListView.setVisibility(View.VISIBLE);
                item.setIcon(R.drawable.ic_map_white);

            } else if (directionsListView.getVisibility() == View.VISIBLE) {
                animation = AnimationUtils.loadAnimation(StartTripActivity.this, R.anim.swipe_left_to_right);

                directionsListView.startAnimation(animation);
                directionsListView.setVisibility(View.GONE);
                mapFragment.getView().startAnimation(animation);
                addPicture.setVisibility(View.VISIBLE);
                mapFragment.getView().setVisibility(View.VISIBLE);
                item.setIcon(R.drawable.ic_action_navigation_menu);
            }
        }


        return super.onOptionsItemSelected(item);
    }

    /**
     * Take care of popping the fragment back stack or finishing the activity as appropriate.
     */
    @Override
    public void onBackPressed() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Are you sure you want to quit the trip? You will loose all trip information");
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                trip.unpinInBackground();
                ParseObject.unpinAllInBackground(tripname);
                StartTripActivity.super.onBackPressed();
            }
        });
        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.create().show();
    }

    /**
     * Create a JSONObject of the places visited to store in the Parse database.
     * @return A JSONObject which contains the details of the places visited in the trip.
     * @throws JSONException
     */
    //private JSONObject getPlacesJSON(ArrayList<String> names, ArrayList<String> address, ArrayList<LatLng> latLngs) throws JSONException {
    private JSONObject getPlacesJSON() throws JSONException {
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

    /**
     * Set up the map object.
     */
    private void setUpMapIfNeeded() {
        // Do a null check to confirm that we have not already instantiated the map.
        if (mMap == null) {
            // Try to obtain the map from the SupportMapFragment
            mapFragment = getSupportFragmentManager().findFragmentById(R.id.map);
            mMap = ((SupportMapFragment) mapFragment).getMap();
            // Enabling MyLocation Layer of Google Map
            mMap.setMyLocationEnabled(true);
        }
    }

    private void setUpLocationListener() {
        ProgressDialog dialog = new ProgressDialog(StartTripActivity.this);
        dialog.setMessage("Getting Coordinates");
        dialog.setCancelable(false);
        dialog.show();
        /********** get Gps location service LocationManager object ***********/
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 2000, 1, this);
        } else if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 2000,1, this);
        }
        else {
            Toast.makeText(getApplicationContext(), "Enable Location Services", Toast.LENGTH_LONG).show();
        }
        dialog.dismiss();
    }

    /**
     * Called when the location has changed.
     * <p/>
     * <p> There are no restrictions on the use of the supplied Location object.
     *
     * @param location The new location, as a Location object.
     */
    @Override
    public void onLocationChanged(Location location) {
        currentLocation = location;

        // Getting latitude of the current location
        double latitude = location.getLatitude();

        // Getting longitude of the current location
        double longitude = location.getLongitude();

        // Creating a LatLng object for the current location
        LatLng latLng = new LatLng(latitude, longitude);

        // Showing the current location in Google Map
        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));

        // Zoom in the Google Map
        mMap.animateCamera(CameraUpdateFactory.zoomTo(15));    }

    /**
     * Called when the provider status changes. This method is called when
     * a provider is unable to fetch a location or if the provider has recently
     * become available after a period of unavailability.
     *
     * @param provider the name of the location provider associated with this
     *                 update.
     * @param status   {@link LocationProvider#OUT_OF_SERVICE} if the
     *                 provider is out of service, and this is not expected to change in the
     *                 near future; {@link LocationProvider#TEMPORARILY_UNAVAILABLE} if
     *                 the provider is temporarily unavailable but is expected to be available
     *                 shortly; and {@link LocationProvider#AVAILABLE} if the
     *                 provider is currently available.
     * @param extras   an optional Bundle which will contain provider specific
     *                 status variables.
     *                 <p/>
     *                 <p> A number of common key/value pairs for the extras Bundle are listed
     *                 below. Providers that use any of the keys on this list must
     *                 provide the corresponding value as described below.
     *                 <p/>
     *                 <ul>
     *                 <li> satellites - the number of satellites used to derive the fix
     */
    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        if(status == LocationProvider.OUT_OF_SERVICE) {
            Toast.makeText(StartTripActivity.this, provider+" is Out of service", Toast.LENGTH_LONG).show();
        } else if (status == LocationProvider.TEMPORARILY_UNAVAILABLE) {
            Toast.makeText(StartTripActivity.this, provider+" is Temporarily Unavailable", Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Called when the provider is enabled by the user.
     *
     * @param provider the name of the location provider associated with this
     *                 update.
     */
    @Override
    public void onProviderEnabled(String provider) {
        Toast.makeText(StartTripActivity.this, provider+" Enabled", Toast.LENGTH_SHORT).show();
    }

    /**
     * Called when the provider is disabled by the user. If requestLocationUpdates
     * is called on an already disabled provider, this method is called
     * immediately.
     *
     * @param provider the name of the location provider associated with this
     *                 update.
     */
    @Override
    public void onProviderDisabled(String provider) {
        Toast.makeText(StartTripActivity.this, provider+" Disabled", Toast.LENGTH_LONG).show();
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }
}
