package com.example.traveldiaries;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;
import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseUser;
import com.parse.ProgressCallback;
import com.parse.SaveCallback;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Activity to take photos and notes and add them to the trip.
 */
public class AddPhotoNoteActivity extends Activity {
    //TODO: Save photos to phone as well.
    static final int REQUEST_IMAGE_CAPTURE = 1;
    private ArrayList<Bitmap> photos;
    private ArrayList<String> notes;

    private GridView picsThumbnailView;
    private ImageView imagePreview;
    private EditText imageCaption;
    private ImageAdapter adapter;

    private int selected;
    private String tripname;
    private Location currentLocation;
    private ArrayList<String> photoFilePaths;
    private int currPhotoNum;
    private String user;

    private int previewwidth;
    private int previewheight;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        user = ParseUser.getCurrentUser().getObjectId();

        Intent intent = getIntent();
        tripname = intent.getStringExtra("tripname");
        currentLocation = intent.getParcelableExtra("location");

        photos = new ArrayList<Bitmap>();
        notes = new ArrayList<String>();
        photoFilePaths = new ArrayList<String>();

        previewwidth = (int) getResources().getDimension(R.dimen.addPhotoNote_previewImageWidth);
        previewheight = (int) getResources().getDimension(R.dimen.addPhotoNote_previewImageHeight);

        // Call an intent to take pictures
        launchCameraIntent();

        setContentView(R.layout.activity_add_photo_note);

        //final int STATUS_BAR_COLOR = getResources().getColor(R.color.DarkerTurquoise);
        //getWindow().setStatusBarColor(STATUS_BAR_COLOR);

        // UI Elements
        TextView cancel = (TextView) findViewById(R.id.cancel);
        TextView done = (TextView) findViewById(R.id.done);
        ImageButton delete = (ImageButton) findViewById(R.id.delete);
        ImageButton addMore = (ImageButton) findViewById(R.id.addMore);
        imagePreview = (ImageView) findViewById(R.id.picFocused);
        imageCaption = (EditText) findViewById(R.id.caption);
        picsThumbnailView = (GridView) findViewById(R.id.picIcons);
        adapter = new ImageAdapter(this, photos);

        picsThumbnailView.setAdapter(adapter);
        picsThumbnailView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                selected = position;
                setPreview(selected);
            }
        });

        // Cancel and dont add any pictures.
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteTempFiles(photoFilePaths, 0, photoFilePaths.size() - 1);
                finish();
            }
        });

        // Done; save taken images locally until trip is finished.
        done.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    saveImagesInLocalDataStore();
                } catch (ParseException e) {
                    Log.d("AddPhotoNoteActivity", "Error saveing Images in Local DataStore");
                    e.printStackTrace();
                }
                finish();
            }
        });

        // Delete selected photo.
        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                photos.remove(selected);
                notes.remove(selected);
                deleteTempFile(photoFilePaths.get(selected));
                photoFilePaths.remove(selected);
                if(photos.size() == 0) {
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
        String currentPhotoFilePath = photoFilePaths.get(currPhotoNum);

        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {

            //BitmapFactory.Options options = new BitmapFactory.Options();
            //options.inSampleSize = 3;  // Experiment with different sizes
            //Bitmap b = BitmapFactory.decodeFile(currentPhotoFilePath, options);

            Bitmap b = ImageProcessingHelperClass.decodeSampledBitmapFromFile(currentPhotoFilePath
                    //, previewwidth, previewheight);
                    ,adapter.getImageWidth(), adapter.getImageHeight());

            if (b == null){
                Log.e("BITMAP", "BITMAP NULL!!");
            }
            photos.add(b);
            notes.add("");
            ((BaseAdapter) picsThumbnailView.getAdapter()).notifyDataSetChanged();
            selected = photos.size()-1;
            setPreview(selected);
        }
        else {
            deleteTempFile(currentPhotoFilePath);  // Delete temp file if photo not taken
            if(photos.size() == 0) {
                finish();
            }
        }
    }

    private void setPreview(int selected) {
        Bitmap b = ImageProcessingHelperClass.decodeSampledBitmapFromFile(photoFilePaths.get(selected)
                , previewwidth, previewheight);
        //imagePreview.setImageBitmap(photos.get(selected));
        imagePreview.setImageBitmap(b);
        imageCaption.setText(notes.get(selected));
    }

    /**
     * Launch the camera intent to take pictures.
     */
    private void launchCameraIntent() {
        Intent takePhotoIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePhotoIntent.resolveActivity(getPackageManager()) != null) {
            //startActivityForResult(takePhotoIntent, REQUEST_IMAGE_CAPTURE);
            currPhotoNum = photos.size();
            // Delete an temp files
            if (photoFilePaths.size() > photos.size()) {
                deleteTempFiles(photoFilePaths, photos.size(), photoFilePaths.size() - 1);
            }

            //generate a unique file name for the image file
            File photoFile = null;
            try {
                File dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
                String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
                String imageFileName = "TravelDiaries_image_"+ timeStamp +"_"+ currPhotoNum;
                photoFile = File.createTempFile(imageFileName, ".jpeg", dir);

                photoFilePaths.add(currPhotoNum, photoFile.getAbsolutePath());
            }
            catch (IOException e) {
                e.printStackTrace();
            }

            // Call an intent to take pictures
            if (photoFile != null) {
                takePhotoIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(photoFile));
                startActivityForResult(takePhotoIntent, REQUEST_IMAGE_CAPTURE);
            }
        }
    }

    private void deleteTempFiles(List<String> filePaths, int i, int j) {
        for (int x=i; x<=j; x++) {
            File file = new File(filePaths.get(x));
            file.delete();
        }
    }

    private void deleteTempFile(String filePath) {
        File file = new File(filePath);
        file.delete();
    }

    /**
     * Geotag the images and save to local datastore until they can be uploded to Parse.
     * @return Sucess or Failure.
     */
    private void saveImagesInLocalDataStore() throws ParseException {
        final ParseGeoPoint geoPoint;
        if(currentLocation != null && currentLocation.getLatitude() != 0 && currentLocation.getLongitude() != 0) {
            geoPoint = new ParseGeoPoint(currentLocation.getLatitude(), currentLocation.getLongitude());
        } else {
            geoPoint = getLastKnownLocation();
        }

        //BitmapFactory.Options options = new BitmapFactory.Options();
        //options.inSampleSize = 2;

        for (int i = 0; i < photoFilePaths.size(); i++) {
            final String fileName = photoFilePaths.get(i);
            final String caption = notes.get(i);

            // Make it available in the gallery
            Intent mediaScanIntent = new Intent (Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
            Uri contentUri = Uri.fromFile(new File(fileName));
            mediaScanIntent.setData(contentUri);
            sendBroadcast(mediaScanIntent);

            ParseObject imageObject = new ParseObject("TripPhotoNote");
            imageObject.put("note", caption);
            if(geoPoint != null) {
                imageObject.put("location", geoPoint);
            }
            imageObject.put("imageFilePath", fileName);
            imageObject.pin(tripname);
            Log.d("PINNING IMAGES", tripname + " - " + fileName + " pinned");

            /*Bitmap b = BitmapFactory.decodeFile(fileName, options);
            ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
            if(b != null) {
                b.compress(Bitmap.CompressFormat.JPEG, 100, byteStream);

                byte[] data = byteStream.toByteArray();
                String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
                final String imageFileName = user+"_image_" + timeStamp + "_" + i + ".jpeg";
                final ParseFile parseImageFile = new ParseFile(imageFileName, data);
                final String caption = notes.get(i);

                /*try {
                    parseImageFile.save();
                    final ParseObject imageObject = new ParseObject("TripPhotoNote");
                    imageObject.put("photo", parseImageFile);
                    imageObject.put("note", caption);
                    if(geoPoint != null) {
                        imageObject.put("location", geoPoint);
                    }
                    imageObject.pin(tripname);
                    Log.d("PINNING IMAGES", tripname + " - " + imageFileName + " pinned");
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            } else {
                Log.d("ADD PHOTO NOTE ACTIVITY", "bit map is null");
            }*/
        }
    }

    /**
     * Get the current user location.
     * @return User location or null if current location is not available.
     */
    private ParseGeoPoint getLastKnownLocation() {
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        Location lastKnownLocation = null;
        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            Log.d("GET CURRENT LOCATION", "GPS_PROVIDER ENABLED");
            lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        } else if(locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
            Log.d("GET CURRENT LOCATION", "NETWORK_PROVIDER ENABLED");
            lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        }

        if(lastKnownLocation != null) {
            return new ParseGeoPoint(lastKnownLocation.getLatitude(), lastKnownLocation.getLongitude());
        } else {
            Toast.makeText(AddPhotoNoteActivity.this, "Could not get current location", Toast.LENGTH_SHORT).show();
            return null;
        }
    }

}
