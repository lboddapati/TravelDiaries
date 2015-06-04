package com.example.traveldiaries;

import android.app.ActionBar;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v7.internal.app.ToolbarActionBar;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.content.Intent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.app.Activity;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.List;
import java.util.ArrayList;


/**
 * The Activity which displays all previous trips taken by the user.
 */
public class PreviousTrip extends Activity {
    private ParseUser user;
    private GridView gridview;
    private ImageView pointer;
    private boolean noTrips = false;
    private boolean noNetwork = false;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        noNetwork = !isNetworkAvailable();
        if(noNetwork) {
            setContentView(R.layout.no_network_error);
            Button refreshButton = (Button) findViewById(R.id.refresh);
            refreshButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    refresh();
                }
            });

        } else {
            setContentView(R.layout.activity_previous_trip);

            List<ParseObject> trips;
            ArrayList<String> trip_names = new ArrayList<String>();
            ArrayList<Bitmap> trip_icons = new ArrayList<Bitmap>();

            gridview = (GridView) findViewById(R.id.gridview);
            pointer = (ImageView) findViewById(R.id.pointer);

            //Get current user.
            user = ParseUser.getCurrentUser();

            //Get all trips previously taken by the user.
            ParseQuery<ParseObject> tripsQuery = ParseQuery.getQuery("Trip");
            tripsQuery.whereEqualTo("user", user);
            try {
                trips = tripsQuery.find();

                // If no trips taken yet, display a message
                if (trips == null || trips.size() == 0) {
                    gridview.setVisibility(View.GONE);
                    noTrips = true;
                }
                // Else display taken trips
                else {
                    noTrips = false;
                    BitmapFactory.Options options = new BitmapFactory.Options();
                    options.inSampleSize = 2;  //TODO: set optimal size;

                    PicAdapter adapter = new PicAdapter(this, trip_names, trip_icons);
                    gridview.setAdapter(adapter);

                    for (ParseObject trip : trips) {
                        String tripname = trip.getString("tripName");
                        String tripdate = trip.getCreatedAt().toString().replaceAll("\\s+\\d+:\\d+:\\d+\\s+PDT", "");
                        trip_names.add(tripname + " (" + tripdate + ")");

                        ParseQuery<ParseObject> picsQuery = ParseQuery.getQuery("TripPhotoNote");
                        picsQuery.whereEqualTo("trip", trip.getObjectId());
                        ParseObject photoNote = null;
                        Bitmap icon = null;
                        try {
                            // Get the first photo from the list of photos taken for each trip to use
                            // as the icon for that trip.
                            photoNote = picsQuery.getFirst();
                            if (photoNote != null) {
                                byte[] data = photoNote.getParseFile("photo").getData();
                                icon = BitmapFactory.decodeByteArray(data, 0, data.length, options);
                            }
                        } catch (ParseException e) {
                            Log.d("PHOTOS", "Error:: fetching photos!!");
                            e.printStackTrace();
                        } finally {
                            // If no photos are available for the trip, use the default icon.
                            if (icon == null) {
                                icon = BitmapFactory.decodeResource(getResources(), R.drawable.defaulticon, options);
                            }
                            trip_icons.add(icon);
                            adapter.notifyDataSetChanged();
                        }
                    }

                    // Display the trips taken by the user.
                    final List<ParseObject> finalTrips = trips;

                    // When the trip is clicked, take to user to the ViewTripActivity.
                    gridview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                            if(!isNetworkAvailable()) {
                                refresh();
                            } else {
                                Intent intent;
                                intent = new Intent(PreviousTrip.this, ViewTripActivity.class);
                                // Pass the ID of the trip selected.
                                intent.putExtra("tripId", finalTrips.get(position).getObjectId());
                                startActivity(intent);
                            }
                        }
                    });
                }
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if(!noNetwork) {
            // Inflate the menu; this adds items to the action bar if it is present.
            getMenuInflater().inflate(R.menu.menu_previous_trip, menu);
            if (noTrips) {
                pointer.setVisibility(View.VISIBLE);
                TextView popupMessage = new TextView(this);
                popupMessage.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT
                        , LinearLayout.LayoutParams.WRAP_CONTENT));
                popupMessage.setText("No Trips taken yet.\nStart a new trip today!");
                popupMessage.setGravity(Gravity.CENTER);
                popupMessage.setPadding(30, 30, 30, 30);
                //popupMessage.setElevation(30);
                popupMessage.setBackgroundColor(getResources().getColor(R.color.Red));
                popupMessage.setTextColor(Color.WHITE);
                PopupWindow popupWindow = new PopupWindow(popupMessage, LinearLayout.LayoutParams.WRAP_CONTENT
                        , LinearLayout.LayoutParams.WRAP_CONTENT);
                popupWindow.showAsDropDown(pointer, 320, 200);
            }
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(!isNetworkAvailable()) {
            refresh();
        } else {
            int id = item.getItemId();

            // If 'New Trip' option is selected, take user to getPlacesActivity to start
            // building a new trip.
            if (id == R.id.action_new_trip) {
                Intent buildNewTrip = new Intent(PreviousTrip.this, getPlacesActivity.class);
                startActivity(buildNewTrip);
                //finish();
            }
            // If 'Logout Out' option is selected, log out the user and redirect back to startup screen.
            else if (id == R.id.action_sign_out) {
                user.logOut();
                Intent loginIntent = new Intent(PreviousTrip.this, prelogin.class);
                startActivity(loginIntent);
                finish();
            }
        }

        return super.onOptionsItemSelected(item);
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    private void refresh() {
        PreviousTrip.super.onResume();
        PreviousTrip.this.onCreate(null);
        PreviousTrip.this.invalidateOptionsMenu();
    }
}


