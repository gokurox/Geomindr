package com.example.harish.geomindr.activity.ebr;

import android.content.Context;
import android.location.Location;
import android.os.AsyncTask;

import com.example.harish.geomindr.database.DatabaseHelper;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;

class PlacesDisplayTask extends AsyncTask<Object, Integer, List<HashMap<String, String>>> {
    // Database instance.
    private DatabaseHelper databaseHelper;
    // Type of entity.
    private String entity;
    // Name of the entity.
    private String entityName;
    // Passing context from service.
    protected Context context;

    @Override
    protected List<HashMap<String, String>> doInBackground(Object... inputObj) {
        JSONObject googlePlacesJson;
        Places placeJsonParser = new Places();
        List<HashMap<String, String>> googlePlacesList = null;

        try {
            googlePlacesJson = new JSONObject((String) inputObj[0]);
            entity = (String) inputObj[1];
            entityName = (String) inputObj[2];
            System.out.println(inputObj[3]);
            context = (Context) inputObj[3];
            databaseHelper = DatabaseHelper.getInstance(context);
            googlePlacesList = placeJsonParser.parse(googlePlacesJson);
        } catch (Exception e) {
            e.printStackTrace();
        }

        // If there is a place returned by Google Map API.
        if(googlePlacesList != null && googlePlacesList.size() > 0)
        {
            // When the user wants particular entity.
            if(!entityName.equals("")){
                for (int i = 0; i < googlePlacesList.size(); i++) {
                    HashMap<String, String> googlePlace = googlePlacesList.get(i);
                    String lower = googlePlace.get("place_name").toLowerCase();
                    if (lower.contains(entityName.toLowerCase())) {
                        databaseHelper.updateLat(entity, Double.valueOf(googlePlace.get("lat")));
                        databaseHelper.updateLng(entity, Double.valueOf(googlePlace.get("lng")));
                        databaseHelper.updateName(entity, googlePlace.get("place_name"));
                        break;
                    }
                }
            }
            // When user want any nearest entity.
            else {
                String place;
                boolean flag = true;

                for (int i = 0; i < googlePlacesList.size(); i++) {
                    HashMap<String, String> googlePlace = googlePlacesList.get(i);

                    double lat = Double.valueOf(googlePlace.get("lat"));
                    double lng = Double.valueOf(googlePlace.get("lng"));

                    Location loc = new Location("");
                    loc.setLatitude(lat);
                    loc.setLongitude(lng);

                    /*Location loc2 = new Location("");
                    loc2.setLatitude(ReminderService.lastLocation.getLatitude());
                    loc2.setLongitude(ReminderService.lastLocation.getLongitude());*/

                    //distance = loc1.distanceTo(loc2);
                    place = googlePlace.get("place_name");

                    if (flag) {
                        databaseHelper.updateLat(entity, lat);
                        databaseHelper.updateLng(entity, lng);
                        databaseHelper.updateName(entity, place);
                        flag = false;
                    }
                    else {
                        boolean ins = databaseHelper.insertRecordEBR(entity, "00:00", place, lat, lng, -1);
                    }
                }
            }
        }

        return googlePlacesList;
    }

    @Override
    protected void onPostExecute(List<HashMap<String, String>> list) {

    }
}