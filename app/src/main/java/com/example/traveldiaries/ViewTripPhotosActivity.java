package com.example.traveldiaries;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.view.MotionEventCompat;
import android.transition.Slide;
import android.transition.Transition;
import android.transition.TransitionManager;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.TranslateAnimation;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

public class ViewTripPhotosActivity extends Activity {
    private int imageShowing;
    private int imageCount;
    private float x1,x2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_photos);

        final ArrayList<Bitmap> pics_icons;
        final ArrayList<String> pic_notes;
        final ArrayList<String> pic_geotag_timestamp;

        Intent intent = getIntent();
        pics_icons = intent.getParcelableArrayListExtra("photos");
        pic_notes = intent.getStringArrayListExtra("notes");
        pic_geotag_timestamp = intent.getStringArrayListExtra("geotag_timestamp");
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
