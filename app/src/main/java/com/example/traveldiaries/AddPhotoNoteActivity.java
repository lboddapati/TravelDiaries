package com.example.traveldiaries;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.SaveCallback;

import java.io.ByteArrayOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class AddPhotoNoteActivity extends Activity {
    //TODO: Save photos to phone as well.
    static final int REQUEST_IMAGE_CAPTURE = 1;
    Intent returnIntent;
    Bundle bundle;
    ArrayList<Bitmap> photos;
    ArrayList<String> notes;
    GridView picsThumbnailView;
    ImageView imagePreview;
    EditText imageCaption;
    int selected;
    String tripname;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        returnIntent = getIntent();
        tripname = returnIntent.getStringExtra("tripname");
        photos = new ArrayList<Bitmap>();
        notes = new ArrayList<String>();

        // Call an intent to take pictures
        launchCameraIntent();

        setContentView(R.layout.activity_add_photo_note);

        TextView cancel = (TextView) findViewById(R.id.cancel);
        TextView done = (TextView) findViewById(R.id.done);
        ImageButton delete = (ImageButton) findViewById(R.id.delete);
        ImageButton addMore = (ImageButton) findViewById(R.id.addMore);
        imagePreview = (ImageView) findViewById(R.id.picFocused);
        imageCaption = (EditText) findViewById(R.id.caption);

        picsThumbnailView = (GridView) findViewById(R.id.picIcons);
        ImageAdapter adapter = new ImageAdapter(this, photos);
        picsThumbnailView.setAdapter(adapter);
        picsThumbnailView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                selected = position;
                //Toast.makeText(AddPhotoNoteActivity.this, selected+"th Photo Selected!", Toast.LENGTH_SHORT).show();
                setPreview(selected);
            }
        });

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //setResult(RESULT_CANCELED, returnIntent);
                finish();
            }
        });

        done.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Bundle extras = new Bundle();
                //extras.putStringArrayList("imageObjectIds", imageObjectIds);
                //returnIntent.putExtras(extras);
                //setResult(RESULT_OK, returnIntent);
                if(saveImagesInLocalDataStore()) {
                    Toast.makeText(AddPhotoNoteActivity.this, photos.size() + " Photos Added!", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(AddPhotoNoteActivity.this, "Error:: Photos Upload Failed!", Toast.LENGTH_SHORT).show();
                }
                finish();
            }
        });

        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                photos.remove(selected);
                notes.remove(selected);
                //Toast.makeText(AddPhotoNoteActivity.this, photos.size()+" Photos left!", Toast.LENGTH_SHORT).show();
                if(photos.size() == 0) {
                    //setResult(RESULT_CANCELED, returnIntent);
                    finish();
                } else {
                    ((BaseAdapter) picsThumbnailView.getAdapter()).notifyDataSetChanged();
                    if (selected != 0) {
                        selected -= 1;
                    }
                    setPreview(selected);
                }
            }
        });

        addMore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                launchCameraIntent();
            }
        });

        imageCaption.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) { }

            @Override
            public void afterTextChanged(Editable s) {
                notes.set(selected, s.toString());
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            //TODO : Add timestamp and caption
            Bundle extras = data.getExtras();
            Bitmap imageBitmap = (Bitmap) extras.get("data");
            photos.add(imageBitmap);
            notes.add("");
            ((BaseAdapter) picsThumbnailView.getAdapter()).notifyDataSetChanged();
            //Toast.makeText(AddPhotoNoteActivity.this, photos.size()+" Photos added!", Toast.LENGTH_SHORT).show();
            selected = photos.size()-1;
            setPreview(selected);
        } else {
            if(photos.size() == 0) {
                //setResult(RESULT_CANCELED, returnIntent);
                finish();
            }
        }
    }

    private void setPreview(int selected) {
        imagePreview.setImageBitmap(photos.get(selected));
        imageCaption.setText(notes.get(selected));
    }

    private void launchCameraIntent() {
        Intent takePhotoIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePhotoIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePhotoIntent, REQUEST_IMAGE_CAPTURE);
        }
    }

    private boolean saveImagesInLocalDataStore() {
        Boolean success;

        //ParseGeoPoint geoPoint = getCurrentLocation();
        final ParseGeoPoint geoPoint = new ParseGeoPoint(37.269382, -122.005476); //TODO: Remove this
        //TODO: Retry getCurrentLocation 2 times and then prompt for location entry if fail again
        if(geoPoint != null) {
            for (int i = 0; i < photos.size(); i++) {
                ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
                photos.get(i).compress(Bitmap.CompressFormat.JPEG, 100, byteStream);
                byte[] data = byteStream.toByteArray();
                String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
                final String imageFileName = tripname +"_image_"+ timeStamp +"_"+ i +".jpeg";
                final ParseFile parseImageFile = new ParseFile(imageFileName, data);
                final String caption = notes.get(i);

                try {
                    parseImageFile.save();
                    final ParseObject imageObject = new ParseObject("TripPhotoNote");
                    imageObject.put("photo", parseImageFile);
                    imageObject.put("note", caption);
                    imageObject.put("location", geoPoint);
                    imageObject.pin(tripname);
                    Log.d("PINNING IMAGES", tripname+" - "+imageFileName+" pinned");
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
            success = true;
        } else {
            //TODO: Prompt for entering location
            success = false;
        }
        return success;
    }

    private ParseGeoPoint getCurrentLocation() {
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        Location lastKnownLocation = null;
        if(locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
            Log.d("GET CURRENT LOCATION", "NETWORK_PROVIDER ENABLED");
            lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        } else if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            Log.d("GET CURRENT LOCATION", "GPS_PROVIDER ENABLED");
            lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        }

        if(lastKnownLocation != null) {
            return new ParseGeoPoint(lastKnownLocation.getLatitude(), lastKnownLocation.getLongitude());
        } else {
            Toast.makeText(AddPhotoNoteActivity.this, "Error :: Could not get current location", Toast.LENGTH_SHORT).show();
            return null;
        }
    }
}
