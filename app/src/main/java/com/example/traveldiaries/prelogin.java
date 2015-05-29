package com.example.traveldiaries;

import android.app.ActionBar;
import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.example.traveldiaries.R;
import com.parse.ParseUser;

import java.util.ArrayList;
import android.widget.AdapterView;
import android.widget.TextView;

public class prelogin extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if(ParseUser.getCurrentUser() != null) {
            Intent intent = new Intent(this, PreviousTrip.class);
            startActivity(intent);
            finish();
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_prelogin);
        //final ArrayList<Bitmap> main_page= new ArrayList<Bitmap>();
        View decorView = getWindow().getDecorView();
        // Hide the status bar.
        int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN;
        decorView.setSystemUiVisibility(uiOptions);

        // Remember that you should never show the action bar if the
        // status bar is hidden, so hide that too if necessary.
        ActionBar actionBar = getActionBar();
        actionBar.hide();
        Bitmap td1 = BitmapFactory.decodeResource(getResources(), R.drawable.td1);
        //main_page.add(td1);
        ImageView imageview=(ImageView) findViewById(R.id.imageview);
        imageview.setImageBitmap(td1);
        Button startButton = (Button) findViewById(R.id.button);
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
