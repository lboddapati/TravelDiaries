package com.example.traveldiaries;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import java.util.ArrayList;
import java.util.List;

public class ViewTripPhotosActivity extends Activity {
    private int imageShowing;
    private int imageCount;
    private float x1,x2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_photos);

        final ArrayList<Bitmap> pics_icons = new ArrayList<Bitmap>();
        final ArrayList<String> pic_notes = new ArrayList<String>();
        final ArrayList<String> pic_geotag_timestamp = new ArrayList<String>();

        //Intent intent = getIntent();
        //pics_icons = intent.getParcelableArrayListExtra("photos");
        //pic_notes = intent.getStringArrayListExtra("notes");
        //pic_geotag_timestamp = intent.getStringArrayListExtra("geotag_timestamp");
        String pin = getIntent().getStringExtra("pin");

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = 2;

        ParseQuery<ParseObject> query = ParseQuery.getQuery("TripPhotoNote");
        query.fromPin(pin);
        List<ParseObject> photonotes = null;
        try {
            photonotes = query.find();
            setTitle(photonotes.size() + " Photos");
            for(ParseObject photonote : photonotes) {
                byte[] data = photonote.getParseFile("photo").getData();
                Bitmap photo = BitmapFactory.decodeByteArray(data, 0, data.length, options);
                String note = photonote.getString("note");
                String geotagTimeStamp = photonote.getCreatedAt().toString();
                if (photonote.getString("geotag") != null) {
                    geotagTimeStamp = photonote.getString("geotag") +" (" + geotagTimeStamp + ")";
                }
                pics_icons.add(photo);
                pic_notes.add(note);
                pic_geotag_timestamp.add(geotagTimeStamp);
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }

        imageCount = pics_icons.size();

        GridView gridview = (GridView) findViewById(R.id.gridview);
        gridview.setAdapter(new ImageAdapter(this, pics_icons));
        gridview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                imageShowing = position;

                final View dialogView = ((LayoutInflater)getSystemService(LAYOUT_INFLATER_SERVICE)).inflate(R.layout.places_pics, null);

                final Dialog image_dialog= new Dialog(ViewTripPhotosActivity.this, android.R.style.Theme_Black_NoTitleBar_Fullscreen);
                image_dialog.setContentView(dialogView);

                final ImageView picture= (ImageView) image_dialog.findViewById(R.id.picture);
                picture.setImageBitmap(pics_icons.get(imageShowing));

                final TextView caption=(TextView) image_dialog.findViewById(R.id.caption);
                caption.setText(pic_notes.get(imageShowing));

                final TextView geotag_timestamp=(TextView) image_dialog.findViewById(R.id.geotag_timestamp);
                geotag_timestamp.setText(pic_geotag_timestamp.get(imageShowing));

                picture.setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        switch (event.getActionMasked()) {
                            case (MotionEvent.ACTION_DOWN) :
                                x1 = event.getX();
                                return true;
                            case (MotionEvent.ACTION_UP) :
                                x2 = event.getX();
                                if(x2 > x1) {   //Left to Right sweep
                                    if(imageShowing > 0) {
                                        imageShowing--;

                                        Animation animation = AnimationUtils.loadAnimation(ViewTripPhotosActivity.this
                                                , R.anim.swipe_left_to_right);
                                        animation.setAnimationListener(new Animation.AnimationListener() {
                                            @Override
                                            public void onAnimationStart(Animation animation) {
                                            }

                                            @Override
                                            public void onAnimationEnd(Animation animation) {
                                                picture.setImageBitmap(pics_icons.get(imageShowing));
                                                caption.setText(pic_notes.get(imageShowing));
                                                geotag_timestamp.setText(pic_geotag_timestamp.get(imageShowing));
                                            }

                                            @Override
                                            public void onAnimationRepeat(Animation animation) {
                                            }
                                        });
                                        dialogView.startAnimation(animation);
                                    }
                                } else if(x2 < x1) {  //Right to Left sweep
                                    if(imageShowing < imageCount-1) {
                                        imageShowing++;

                                        Animation animation = AnimationUtils.loadAnimation(ViewTripPhotosActivity.this
                                                , R.anim.swipe_right_to_left);
                                        animation.setAnimationListener(new Animation.AnimationListener() {
                                            @Override
                                            public void onAnimationStart(Animation animation) {
                                            }

                                            @Override
                                            public void onAnimationEnd(Animation animation) {
                                                picture.setImageBitmap(pics_icons.get(imageShowing));
                                                caption.setText(pic_notes.get(imageShowing));
                                                geotag_timestamp.setText(pic_geotag_timestamp.get(imageShowing));
                                            }

                                            @Override
                                            public void onAnimationRepeat(Animation animation) {
                                            }
                                        });
                                        dialogView.startAnimation(animation);
                                    }
                                }
                                return true;
                            default:
                                return false;
                        }
                    }
                });

                image_dialog.show();
            }
        });



    }
}
