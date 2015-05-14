package com.example.traveldiaries;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
//import com.example.lasyaboddapati.traveldiaries.R;

public class AddPhotoNoteActivity extends Activity {
    static final int REQUEST_IMAGE_CAPTURE = 1;
    static int RESULT_CODE = RESULT_CANCELED;
    String currentPhotoFilePath;
    String imageCaption;
    Intent returnIntent;
    Bundle bundle;
    ArrayList<Bitmap> photos;
    GridView picsThumbnailView;
    int selected;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        returnIntent = getIntent();
        bundle = new Bundle();
        currentPhotoFilePath = null;
        photos = new ArrayList<Bitmap>();

        // Call an intent to take pictures
        launchCameraIntent();

        setContentView(R.layout.activity_add_photo_note);

        TextView cancel = (TextView) findViewById(R.id.cancel);
        TextView done = (TextView) findViewById(R.id.done);
        ImageButton delete = (ImageButton) findViewById(R.id.delete);
        ImageButton addMore = (ImageButton) findViewById(R.id.addMore);

        picsThumbnailView = (GridView) findViewById(R.id.picIcons);
        ImageAdapter adapter = new ImageAdapter(this, photos);
        picsThumbnailView.setAdapter(adapter);
        picsThumbnailView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                selected = position;
                Toast.makeText(AddPhotoNoteActivity.this, selected+"th Photo Selected!", Toast.LENGTH_SHORT).show();
                setPreview(photos.get(selected));
            }
        });

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        done.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //TODO : Save the photos
                Toast.makeText(AddPhotoNoteActivity.this, photos.size()+" Photos saved!", Toast.LENGTH_SHORT).show();
                finish();
            }
        });

        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                photos.remove(selected);
                Toast.makeText(AddPhotoNoteActivity.this, photos.size()+" Photos left!", Toast.LENGTH_SHORT).show();
                if(photos.size() == 0) {
                    finish();
                } else {
                    ((BaseAdapter) picsThumbnailView.getAdapter()).notifyDataSetChanged();
                    if (selected != 0) {
                        selected -= 1;
                    }
                    setPreview(photos.get(selected));
                }
            }
        });

        addMore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                launchCameraIntent();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_IMAGE_CAPTURE) {
            if(resultCode == RESULT_OK) {
                //TODO : Add timestamp and caption
                Bundle extras = data.getExtras();
                Bitmap imageBitmap = (Bitmap) extras.get("data");
                photos.add(imageBitmap);
                ((BaseAdapter) picsThumbnailView.getAdapter()).notifyDataSetChanged();
                //Toast.makeText(AddPhotoNoteActivity.this, photos.size()+" Photos added!", Toast.LENGTH_SHORT).show();
                selected = photos.size()-1;
                setPreview(imageBitmap);
            } else {
                finish();
            }
        }
    }

    private void setPreview(Bitmap imageBitmap) {
        ImageView imagePreview = (ImageView) findViewById(R.id.picFocused);
        imagePreview.setImageBitmap(imageBitmap);
    }

    private void launchCameraIntent() {
        Intent takePhotoIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePhotoIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePhotoIntent, REQUEST_IMAGE_CAPTURE);
        }
    }
}
