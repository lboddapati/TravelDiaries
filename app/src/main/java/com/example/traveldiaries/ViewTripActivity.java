package com.example.traveldiaries;

import android.content.Intent;

import android.location.Location;
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
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.parse.ParseException;

import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;


/**
 * Activity to view the details of a (selected) previous trip.
 */
public class ViewTripActivity extends FragmentActivity {
    //TODO: Add option to view all pics at once

    private GoogleMap mMap; // Might be null if Google Play services APK is not available.
    private Fragment mapFragment;
    private ParseObject trip;
    private List<ParseObject> photoNotes;
    private JSONObject placesJSON;
    private JSONObject route;
    private String tripname;

    private ArrayList<String> names;
    private ArrayList<String> address;
    private ArrayList<LatLng> latLngs;
    private ArrayList<Integer> photoCounts;

    private ListView listView;
    private TextView textView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trip_details);

        if (android.os.Build.VERSION.SDK_INT > 9) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }

        setUpMapIfNeeded();

        names = new ArrayList<String>();
        address = new ArrayList<String>();
        latLngs = new ArrayList<LatLng>();

        String tripID = getIntent().getStringExtra("tripId");
        ParseQuery<ParseObject> tripQuery = ParseQuery.getQuery("Trip");
        try {
            trip = tripQuery.get(tripID);
            tripname = trip.getString("tripName");
            setTitle(tripname);

            placesJSON = trip.getJSONObject("places");
            route = trip.getJSONObject("route");
            ParseQuery<ParseObject> picsQuery = ParseQuery.getQuery("TripPhotoNote");
            picsQuery.whereEqualTo("trip", trip.getObjectId());
            photoNotes = picsQuery.find();

            parsePlacesJSON(placesJSON);
            photoCounts = matchPhotosToPlaces();
            drawTripOnMap();
            mMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
                @Override
                public void onInfoWindowClick(Marker marker) {
                    if (marker.getSnippet() != null) {
                        Intent intent;
                        intent = new Intent(ViewTripActivity.this, ViewTripPhotosActivity.class);
                        intent.putExtra("pin", marker.getTitle());
                        startActivity(intent);
                    }
                }
            });

            listView = (ListView) findViewById(R.id.places);
            listView.setVisibility(View.GONE);
            listView.setAdapter(new ListAdapter(this, names, address, photoCounts));
            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                    Intent intent;
                    intent = new Intent(ViewTripActivity.this, ViewTripPhotosActivity.class);
                    intent.putExtra("pin", names.get(position));
                    startActivity(intent);
                }
            });

            textView = (TextView) findViewById(R.id.photoCount);
            textView.setVisibility(View.GONE);
            if(photoNotes.size() != 0) {
                textView.setText(photoNotes.size() + " Photos");
            }

        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    /**
     * Method to draw the trip route on the map.
     */
    private void drawTripOnMap() {
        int numOfPlaces = latLngs.size();

        TextView popupMessage = new TextView(this);
        popupMessage.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT
                , LinearLayout.LayoutParams.WRAP_CONTENT));


        Marker marker = mMap.addMarker(new MarkerOptions().position(latLngs.get(0))
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));

        marker.setTitle(names.get(0));
        if(photoCounts.get(0) != 0) {
            marker.setSnippet(photoCounts.get(0) + " photos");
            BitmapDescriptor icon = BitmapDescriptorFactory.fromResource(R.drawable.ic_action_camera);
            mMap.addMarker(new MarkerOptions().position(marker.getPosition()).icon(icon).flat(true));
        }
        marker.showInfoWindow();

        for(int i=1; i<numOfPlaces-1; i++) {
            marker = mMap.addMarker(new MarkerOptions().position(latLngs.get(i))
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_VIOLET)));
            marker.setTitle(names.get(i));
            if(photoCounts.get(i) != 0) {
                marker.setSnippet(photoCounts.get(i) + " photos");
                BitmapDescriptor icon = BitmapDescriptorFactory.fromResource(R.drawable.ic_action_camera);
                mMap.addMarker(new MarkerOptions().position(marker.getPosition()).icon(icon).flat(true));
            }
            //marker.showInfoWindow();
        }

        marker = mMap.addMarker(new MarkerOptions().position(latLngs.get(numOfPlaces - 1))
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));
        marker.setTitle(names.get(numOfPlaces-1));
        if(photoCounts.get(numOfPlaces-1) != 0) {
            marker.setSnippet(photoCounts.get(numOfPlaces-1) + " photos");
            BitmapDescriptor icon = BitmapDescriptorFactory.fromResource(R.drawable.ic_action_camera);
            mMap.addMarker(new MarkerOptions().position(marker.getPosition()).icon(icon).flat(true));
        }
        //marker.showInfoWindow();

        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLngs.get(0), 11.0f));
        MapHelperClass.drawRoute(route, mMap);
    }

    /**
     * Method that matches the photos taken in a trip to the place which is
     * closest to where the photo was taken.
     * @return A list of integers which contains the number of photos taken at each place.
     * @throws ParseException
     */
    private ArrayList<Integer> matchPhotosToPlaces() throws ParseException {
        ArrayList<Integer> photoCount = new ArrayList<Integer>(names.size());

        for (int i=0; i<names.size(); i++) {
            photoCount.add(0);
        }

        for (ParseObject photonote : photoNotes) {
            ParseGeoPoint geoPoint = (ParseGeoPoint) photonote.get("location");
            if (geoPoint != null) {

                float minDist = Float.MAX_VALUE;
                int closestPlace = 0;
                for (int i = 0; i < latLngs.size(); i++) {
                    float[] dist = new float[1];
                    Location.distanceBetween(geoPoint.getLatitude(), geoPoint.getLongitude()
                            , latLngs.get(i).latitude, latLngs.get(i).longitude, dist);
                    if (dist[0] < minDist) {
                        minDist = dist[0];
                        closestPlace = i;
                    }
                }

                photonote.put("geotag", "At " + names.get(closestPlace));
                photonote.pin(names.get(closestPlace));

                Log.d("MATCH PHOTOS TO PLACES", "closest place is " + names.get(closestPlace));

                int count = photoCount.get(closestPlace) + 1;
                photoCount.set(closestPlace, count);

                for(int i=0; i<names.size(); i++) {
                    Log.d("MATCH PHOTOS TO PLACES", photoCount.get(i) + "photos at " + names.get(i));
                }
            }
            photonote.pin(tripname);
        }

        return photoCount;
    }

    /**
     * Method that parses the given places JSONObject and extracts the
     * details of the places into the names, address and latLngs ArrayLists.
     * @param placesJSON The JSON Object that contains the details of the places visited.
     */
    private void parsePlacesJSON(JSONObject placesJSON) {
        double lat;
        double lng;
        JSONObject place;

        try {
            JSONArray placesArray = placesJSON.getJSONArray("places");
            for(int i=0; i< placesArray.length(); i++) {
                place = placesArray.getJSONObject(i);
                names.add(place.getString("name"));
                address.add(place.getString("address"));
                lat = place.getDouble("latitude");
                lng = place.getDouble("longitude");
                latLngs.add(new LatLng(lat, lng));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    private void setUpMapIfNeeded() {
        // Do a null check to confirm that we have not already instantiated the map.
        if (mMap == null) {
            // Try to obtain the map from the SupportMapFragment.
            mapFragment = getSupportFragmentManager().findFragmentById(R.id.map);
            mMap = ((SupportMapFragment) mapFragment).getMap();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_view_trip, menu);
        if(photoNotes.size() == 0) {
            menu.findItem(R.id.action_view_all_photos).setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        // When this option is selected, show all the photos taken in that trip.
        if (id == R.id.action_view_all_photos) {
            Intent intent;
            intent = new Intent(ViewTripActivity.this, ViewTripPhotosActivity.class);
            intent.putExtra("pin", tripname);
            startActivity(intent);
        } else if (id == R.id.action_view_places) {
            Animation animation;
            if (listView.getVisibility() == View.GONE) {
                animation = AnimationUtils.loadAnimation(ViewTripActivity.this, R.anim.swipe_right_to_left);
                //mapFragment.getView().startAnimation(animation);
                mapFragment.getView().setVisibility(View.GONE);
                if (photoNotes.size() != 0) {
                    textView.startAnimation(animation);
                    textView.setVisibility(View.VISIBLE);
                }
                listView.startAnimation(animation);
                listView.setVisibility(View.VISIBLE);
                item.setIcon(R.drawable.ic_map_white);

            } else if (listView.getVisibility() == View.VISIBLE) {
                animation = AnimationUtils.loadAnimation(ViewTripActivity.this, R.anim.swipe_left_to_right);
                if (photoNotes.size() != 0) {
                    textView.startAnimation(animation);
                    textView.setVisibility(View.GONE);
                }
                listView.startAnimation(animation);
                listView.setVisibility(View.GONE);
                mapFragment.getView().startAnimation(animation);
                mapFragment.getView().setVisibility(View.VISIBLE);
                item.setIcon(R.drawable.ic_action_navigation_menu);
            }
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Destroy all fragments and loaders.
     * Unpin all Parse Objects (photo notes).
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            ParseObject.unpinAll(tripname);
            for(String placename : names) {
                ParseObject.unpinAll(placename);
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }
}
