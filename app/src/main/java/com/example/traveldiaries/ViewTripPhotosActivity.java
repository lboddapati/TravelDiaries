package com.example.traveldiaries;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
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
    private List<ParseObject> photonotes;

    private byte[] data;
    private Bitmap bitmap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_photos);

        ArrayList<Bitmap> pics_icons = new ArrayList<Bitmap>();
        final ArrayList<String> pic_notes = new ArrayList<String>();
        final ArrayList<String> pic_geotag_timestamp = new ArrayList<String>();

        String pin = getIntent().getStringExtra("pin");

        ImageAdapter adapter = new ImageAdapter(ViewTripPhotosActivity.this, pics_icons);

        //BitmapFactory.Options options = new BitmapFactory.Options();
        //options.inSampleSize = 4;

        final DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);

        ParseQuery<ParseObject> query = ParseQuery.getQuery("TripPhotoNote");
        query.fromPin(pin);
        try {
            String note;
            String geotagTimeStamp;
            photonotes = query.find();
            setTitle(photonotes.size() + " Photos");
            for(ParseObject photonote : photonotes) {
                data = photonote.getParseFile("photo").getData();
                bitmap = ImageProcessingHelperClass.decodeSampledBitmapFromByteArry(data
                        , adapter.getImageWidth(), adapter.getImageHeight());
                note = photonote.getString("note");
                geotagTimeStamp = photonote.getCreatedAt().toString();
                if (photonote.getString("geotag") != null) {
                    geotagTimeStamp = photonote.getString("geotag") +" (" + geotagTimeStamp + ")";
                }
                pics_icons.add(bitmap);
                pic_notes.add(note);
                pic_geotag_timestamp.add(geotagTimeStamp);
            }
        } catch (ParseException e) {
            e.printStackTrace();
        } finally {
            data = null;
            bitmap = null;
        }

        imageCount = pics_icons.size();

        GridView gridview = (GridView) findViewById(R.id.gridview);
        gridview.setAdapter(adapter);
        pics_icons = null;
        gridview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                imageShowing = position;

                final View dialogView = ((LayoutInflater)getSystemService(LAYOUT_INFLATER_SERVICE)).inflate(R.layout.places_pics, null);

                final Dialog image_dialog= new Dialog(ViewTripPhotosActivity.this, android.R.style.Theme_Black_NoTitleBar_Fullscreen);
                image_dialog.setContentView(dialogView);

                final ImageView picture= (ImageView) image_dialog.findViewById(R.id.picture);
                try {
                    data = photonotes.get(position).getParseFile("photo").getData();
                    bitmap = ImageProcessingHelperClass.decodeSampledBitmapFromByteArry(data
                                                   , metrics.widthPixels, metrics.heightPixels);
                    picture.setImageBitmap(bitmap);
                } catch (ParseException e) {
                    e.printStackTrace();
                } finally {
                    data = null;
                    bitmap = null;
                }
                //picture.setImageBitmap(pics_icons.get(imageShowing));

                final TextView caption=(TextView) image_dialog.findViewById(R.id.caption);
                caption.setText(pic_notes.get(imageShowing));

                final TextView geotag_timestamp=(TextView) image_dialog.findViewById(R.id.geotag_timestamp);
                geotag_timestamp.setText(pic_geotag_timestamp.get(imageShowing));

                final Animation.AnimationListener animationListener = new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {
                        try {
                            data = photonotes.get(imageShowing).getParseFile("photo").getData();
                            bitmap = ImageProcessingHelperClass.decodeSampledBitmapFromByteArry(data
                                    , metrics.widthPixels, metrics.heightPixels);
                            picture.setImageBitmap(bitmap);
                            caption.setText(pic_notes.get(imageShowing));
                            geotag_timestamp.setText(pic_geotag_timestamp.get(imageShowing));
                        } catch (ParseException e) {
                            e.printStackTrace();
                        } finally {
                            data = null;
                            bitmap = null;
                        }
                    }

                    @Override
                    public void onAnimationEnd(Animation animation) { }

                    @Override
                    public void onAnimationRepeat(Animation animation) { }
                };

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
                                        animation.setAnimationListener(animationListener);
                                        dialogView.startAnimation(animation);
                                    }
                                } else if(x2 < x1) {  //Right to Left sweep
                                    if(imageShowing < imageCount-1) {
                                        imageShowing++;

                                        Animation animation = AnimationUtils.loadAnimation(ViewTripPhotosActivity.this
                                                , R.anim.swipe_right_to_left);
                                        animation.setAnimationListener(animationListener);
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
