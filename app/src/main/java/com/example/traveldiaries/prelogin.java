package com.example.traveldiaries;

import android.app.ActionBar;
import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.text.Layout;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.example.traveldiaries.R;
import com.parse.ParseUser;

import java.util.ArrayList;
import android.widget.AdapterView;
import android.widget.RelativeLayout;
import android.widget.TextView;
/*
* The main page of travel Diaries before login
* */

public class prelogin extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_prelogin);

        if(ParseUser.getCurrentUser() != null) {
            Intent intent = new Intent(this, PreviousTrip.class);
            startActivity(intent);
            finish();
        }

        final DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);

        Bitmap bm = ImageProcessingHelperClass.decodeSampledBitmapFromResource(getResources()
                        , R.drawable.startup_background, metrics.widthPixels, metrics.heightPixels);
        BitmapDrawable bitmapDrawable = new BitmapDrawable(getResources(), bm);
        getWindow().setBackgroundDrawable(bitmapDrawable);

        Button startButton = (Button) findViewById(R.id.button);
        startButton.setVisibility(View.VISIBLE);
        startButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                 {
                    start();
                }
            }
        });
    }

    public void start() {
        Intent intent = new Intent(prelogin.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_prelogin, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
