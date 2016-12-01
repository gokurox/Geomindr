package com.example.harish.geomindr.activity.ebr;

import android.app.NotificationManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.PorterDuff;
import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.harish.geomindr.MainActivity;
import com.example.harish.geomindr.R;
import com.example.harish.geomindr.database.DatabaseHelper;
import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.HttpMethod;
import com.facebook.appevents.AppEventsLogger;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONException;

import java.util.ArrayList;
import java.util.Collections;

import static android.widget.Toast.makeText;
import static com.example.harish.geomindr.R.id.googleMap;

public class PlaceMap extends AppCompatActivity implements LocationListener, OnMapReadyCallback {
    // GoogleMap instance.
    GoogleMap map;
    // Device's current lat and lng.
    double currLat, currLng;
    // Type of entity.
    String entity;
    // Check if user has clicked on a marker or not.
    boolean markerClicked = false;
    // Latitude of clicked marker
    double markerLat, markerLng;
    // Message to post to Facebook.
    String msg;
    // ID of page for place on Facebook.
    String placePageId;
    // Found a place corresponding to user's latitude and longitude on facebook.
    boolean foundPlace = false;

    // Store name of top 5 places returned by Facebook.
    ArrayList<String> placeName;
    // Store placeId of top 5 places returned by Facebook.
    ArrayList<String> placeId;

    // Database instance;
    DatabaseHelper databaseHelper;
    // Facebook callback manager.
    CallbackManager callbackManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Initializing facebook's SDK.
        // NOTE: Do this before displaying the layout of the activity.
        FacebookSdk.sdkInitialize(getApplicationContext());
        AppEventsLogger.activateApp(getApplication());

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_place_map);

        placeName = new ArrayList<>();
        placeId = new ArrayList<>();

        // Cancel the notification.
        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notificationManager.cancel(this.getIntent().getExtras().getInt("notId"));

        entity = this.getIntent().getStringExtra("entity");
        currLat = this.getIntent().getDoubleExtra("curLat", -9.99);
        currLng = this.getIntent().getDoubleExtra("curLng", -9.99);

        // Set app action bar name appropriately
        switch (entity) {
            case "atm":
                setTitle("Geomindr : ATM");
                break;
            case "food":
                setTitle("Geomindr : Food Outlet");
                break;
            case "hospital":
                setTitle("Geomindr : Hospital");
                break;
            case "police":
                setTitle("Geomindr : Police Station");
                break;
            case "shopping_mall":
                setTitle("Geomindr : Shopping Complex");
                break;
            case "pharmacy":
                setTitle("Geomindr : Pharmacy");
                break;
            case "gym":
                setTitle("Geomindr : Gym");
                break;
            case "bank":
                setTitle("Geomindr : Bank");
                break;
            case "post_office":
                setTitle("Geomindr : Post Office");
                break;
            case "bar":
                setTitle("Geomindr : Bar");
                break;
            case "library":
                setTitle("Geomindr : Library");
                break;
            case "movie_theater":
                setTitle("Geomindr : Movie Theatre");
                break;
            case "book_store":
                setTitle("Geomindr : Book Store");
                break;
            case "local_government_office":
                setTitle("Geomindr : Government Office");
                break;
            case "gas_station":
                setTitle("Geomindr : Petrol Pump");
                break;
        }

        Button btnPostToFacebook = (Button) findViewById(R.id.btn_post_to_facebook);

        // Setting background color for 'Save Reminder' button.
        btnPostToFacebook.getBackground().setColorFilter(
                ResourcesCompat.getColor(getResources(), R.color.colorPrimaryDark, null),
                PorterDuff.Mode.MULTIPLY);

        btnPostToFacebook.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (AccessToken.getCurrentAccessToken() == null ||
                        AccessToken.getCurrentAccessToken().isExpired()) {
                    showFacebookLoginDialog();
                }
                else {
                    if (markerClicked) {
                        if (markerLat != currLat && markerLng != currLng) {
                            showMsgDialog();
                        }
                        else {
                            Toast.makeText(PlaceMap.this, "Please select an appropriate marker.",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                    else {
                        Toast.makeText(PlaceMap.this, "Please select a marker.", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

        setUpMapIfNeeded();
    }

    private void showMsgDialog() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);

        View view = LayoutInflater.from(this).inflate(R.layout.facebook_msg_post, null);
        final EditText facebookMsg = (EditText) view.findViewById(R.id.facebook_msg);

        // Set dialog view.
        alertDialog.setView(view);

        // To prevent dismiss dialog box on back key pressed.
        alertDialog.setCancelable(false);

        // On pressing Post button, post the message to user's facebook wall.
        alertDialog.setPositiveButton("Continue", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                if (!facebookMsg.getText().toString().isEmpty()) {
                    msg = facebookMsg.getText().toString();
                    getLocationFromFacebook();
                }
                else {
                    Toast.makeText(PlaceMap.this, "Please enter message.",
                            Toast.LENGTH_SHORT).show();
                }
                dialog.cancel();
            }
        });

        // On pressing Cancel button, dismiss the dialog box.
        alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        // Showing Alert Message.
        alertDialog.show();
    }

    private void showFacebookLocationDialog() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
        final AlertDialog dialog = alertDialog.create();

        // Title.
        alertDialog.setTitle("Post Location?");

        // To prevent dismiss dialog box on back key pressed.
        alertDialog.setCancelable(false);

        alertDialog.setItems(placeName.toArray(new String[placeName.size()]), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int which) {
                Toast.makeText(PlaceMap.this, "Posting.", Toast.LENGTH_SHORT).show();
                placePageId = placeId.get(which);
                doPost();
            }
        });

        // On pressing Cancel button, dismiss the dialog box.
        alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialogInterface, int which) {
                dialog.cancel();
            }
        });

        // Showing Alert Message.
        alertDialog.show();
    }

    private void showFacebookLoginDialog() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);

        // Setting Dialog Title.
        alertDialog.setTitle("ALERT");

        // Setting Dialog Message.
        alertDialog.setMessage("Log in with your Facebook ID to allow the app" +
                " to post to your Facebook wall.");

        View view = LayoutInflater.from(this).inflate(R.layout.dialog_fb_login, null);

        // creating the callback manager
        callbackManager = CallbackManager.Factory.create();

        LoginButton loginButton = (LoginButton) view.findViewById(R.id.login_button);
        // setting 'publish_actions' permission
        // this permission will allow our app to post to user's facebook wall
        loginButton.setPublishPermissions(Collections.singletonList("publish_actions"));

        // Set dialog view.
        alertDialog.setView(view);

        // To prevent dismiss dialog box on back key pressed.
        alertDialog.setCancelable(false);

        // On pressing Cancel button, dismiss the dialog box.
        alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        final AlertDialog dialog = alertDialog.create();
        // Showing Alert Message.
        dialog.show();

        // registering the callback manager
        loginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            // if the login via facebook is successful
            // we will get the access token with requested permissions, if the login is successful
            public void onSuccess(LoginResult loginResult) {
                makeText(PlaceMap.this, "Logged in successfully.", Toast.LENGTH_SHORT).show();
                dialog.cancel();
            }

            // if the user cancels the login
            @Override
            public void onCancel() {
                makeText(PlaceMap.this, "Login cancelled.", Toast.LENGTH_SHORT).show();
                dialog.cancel();
            }

            // if there is some error logging in via facebook (maybe internet connectivity issue)
            @Override
            public void onError(FacebookException e) {
                makeText(PlaceMap.this, "Error logging in with facebook.", Toast.LENGTH_SHORT).show();
                dialog.cancel();
            }
        });
    }

    private void setUpMapIfNeeded() {
        // Do a null check to confirm that we have not already instantiated the map.
        if (map == null) {
            // Try to obtain the map from the SupportMapFragment.
            SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                    .findFragmentById(googleMap);
            mapFragment.getMapAsync(this);
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        // Setting marker click listener.
        googleMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                markerClicked = true;
                markerLat = marker.getPosition().latitude;
                markerLng = marker.getPosition().longitude;
                return false;
            }
        });

        // Setting markers for user's current location.
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));

        LatLng latLng = new LatLng(currLat, currLng);
        markerOptions.position(latLng);
        markerOptions.title("You are here!");
        googleMap.addMarker(markerOptions);
        googleMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        googleMap.animateCamera(CameraUpdateFactory.zoomTo(13));

        // Setting markers for entity locations.
        databaseHelper = DatabaseHelper.getInstance(this);
        Cursor res = databaseHelper.getRecordsByEntity(entity);

        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));

        while(res.moveToNext()) {
            latLng = new LatLng(res.getDouble(3), res.getDouble(4));
            markerOptions.position(latLng);
            markerOptions.title(res.getString(2));
            googleMap.addMarker(markerOptions);
        }

        res.close();

        // Delete the Entity Based Reminder record from database.
        databaseHelper.deleteData(entity);
    }


    private void getLocationFromFacebook() {
        // Make the API call.
        // Search for facebook page corresponding to user's location.
        GraphRequest request = GraphRequest.newGraphPathRequest(
                AccessToken.getCurrentAccessToken(),
                "/search",
                new GraphRequest.Callback() {
                    @Override
                    public void onCompleted(GraphResponse response) {
                        if (response.getError() == null) {
                            if (response.getJSONObject() != null) {
                                try {

                                    if (!placeName.isEmpty()) {
                                        placeName.clear();
                                    }
                                    if (!placeId.isEmpty()) {
                                        placeId.clear();
                                    }

                                    for (int i = 0; i < 5 && i < response.getJSONObject()
                                            .getJSONArray("data").length(); ++i) {
                                        placeName.add(response.getJSONObject().getJSONArray("data")
                                                .getJSONObject(i).getString("name"));
                                        placeId.add(response.getJSONObject().getJSONArray("data")
                                                .getJSONObject(i).getString("id"));
                                    }
                                    showFacebookLocationDialog();
                                    //Log.d("place", response.getJSONObject().getJSONArray("data").toString());
                                    foundPlace = true;
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    }
                });

        // Bundle object to send request parameters to Graph API for
        // searching the facebook page corresponding to user's location.
        Bundle parameters = new Bundle();
        // Type of search.
        parameters.putString("type", "place");
        // Coordinates corresponding to the place to be searched.
        parameters.putString("center", String.valueOf(markerLat) + "," + String.valueOf(markerLng));
        // Search for facebook pages within 1000 metres of user's location.
        parameters.putString("distance", "1000");
        request.setParameters(parameters);
        request.executeAsync();
    }

    private void doPost() {
        Bundle params = new Bundle();

        // Setting the message of the status.
        // This will be posted to user's facebook wall.
        params.putString("message", msg);

        // If, found a place corresponding to user's latitude and longitude on facebook
        // then, set the place field in the post.
        if (foundPlace) {
            params.putString("place", placePageId);
        }

        // Make the API call, i.e, post the status to user's facebook wall.
        new GraphRequest(
                AccessToken.getCurrentAccessToken(),
                "/me/feed",
                params,
                HttpMethod.POST,
                new GraphRequest.Callback() {
                    public void onCompleted(GraphResponse response) {
                        Toast.makeText(PlaceMap.this, "Posted.",
                                Toast.LENGTH_SHORT).show();
                    }
                }
        ).executeAsync();
    }

    // Result sent by facebook activity when user log in with facebook.
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onLocationChanged(Location location) {

    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {

    }

    @Override
    public void onProviderEnabled(String s) {

    }

    @Override
    public void onProviderDisabled(String s) {

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }
}