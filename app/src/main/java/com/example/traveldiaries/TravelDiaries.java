package com.example.traveldiaries;

import android.app.Application;
import com.parse.Parse;

public class TravelDiaries extends Application {
    String ApplicationID = "OIklvzrNsbv9JkSqlgTwTB87k2894E7UcEFtX3Dv";
    String ClientKey = "5sKdITA68zq8Lgb9nnK37udjHxcerTJX5EoAdPci";

    @Override
    public void onCreate() {
        super.onCreate();
        Parse.enableLocalDatastore(getApplicationContext());
        Parse.initialize(this, ApplicationID, ClientKey);
    }
}
