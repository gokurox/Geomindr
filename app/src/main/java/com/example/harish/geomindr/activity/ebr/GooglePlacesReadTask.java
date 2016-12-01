package com.example.harish.geomindr.activity.ebr;

import android.content.Context;
import android.os.AsyncTask;

public class GooglePlacesReadTask extends AsyncTask<Object, Integer, String> {

    // variable to store all the data from web url in the form of JSON
    private String googlePlacesData = null;

    //entity tag and name of a particular location set by the user
    private String Entity, nameEntity;

    // passing context from the service of the database
    protected Context context;

    @Override
    protected String doInBackground(Object... inputObj) {
        try {
            //adding values from the object
            String googlePlacesUrl = (String) inputObj[0];
            Entity = (String) inputObj[1];
            nameEntity = (String) inputObj[2];
            context = (Context) inputObj[3];
            Http http = new Http();
            googlePlacesData = http.read(googlePlacesUrl);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return googlePlacesData;
    }

    @Override
    protected void onPostExecute(String result) {
        PlacesDisplayTask placesDisplayTask = new PlacesDisplayTask();
        Object[] toPass = new Object[4];
        toPass[0] = result;
        toPass[1] = Entity;
        toPass[2] = nameEntity;
        toPass[3] = context;
        placesDisplayTask.execute(toPass);
    }
}