package com.example.harish.geomindr.activity.map;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.location.Address;
import android.location.Geocoder;
import android.location.LocationManager;
import android.provider.Settings;
import android.support.design.widget.TextInputLayout;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.harish.geomindr.R;
import com.example.harish.geomindr.service.main.ReminderService;
import com.github.clans.fab.FloatingActionButton;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import static com.example.harish.geomindr.service.main.ReminderService.lastLocation;
import static com.example.harish.geomindr.service.main.ReminderService.stopService;

public class TaskMap extends AppCompatActivity implements OnMapReadyCallback {
    // GoogleMap instance.
    GoogleMap map;
    // Location to be searched in google map.
    EditText searchLocation;
    // Button indicating 'use the specified location on google map to trigger reminder'.
    Button btnUseLocation;
    // Search for a location in google map.
    Button btnSearch;
    // Button which makes the google map point to the current location of the user.
    FloatingActionButton btnMyLocation;

    // Name of the location returned by google map.
    String locationName;
    // LatLang of the location returned by google map.
    Double latitude, longitude;
    // Set custom hint on the 'btnUseLocation' button.
    String hint;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_map);
        setTitle("Add Location");

        // Initializing location as null.
        locationName = null;
        latitude = 0.0;
        longitude = 0.0;

        // Initializing View objects.
        searchLocation = (EditText) findViewById(R.id.location);
        btnUseLocation = (Button) findViewById(R.id.btn_use_location);
        btnSearch = (Button) findViewById(R.id.btn_search);
        btnMyLocation = (FloatingActionButton) findViewById(R.id.fab_my_location);

        // Setting background color for the buttons.
        btnUseLocation.getBackground().setColorFilter(ResourcesCompat.getColor(getResources(),
                R.color.colorPrimaryDark, null), PorterDuff.Mode.MULTIPLY);
        btnSearch.getBackground().setColorFilter(ResourcesCompat.getColor(getResources(),
                R.color.colorPrimaryDark, null), PorterDuff.Mode.MULTIPLY);

        btnUseLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            // Send back locationName, latitude and longitude.
            public void onClick(View view) {
                Intent returnIntent = new Intent();

                // If user has not selected any location, then don't pass result to the calling activity.
                if (locationName == null) {
                    Toast.makeText(TaskMap.this, "You have not selected any location.",
                            Toast.LENGTH_SHORT).show();
                    return;
                }

                // If everything is fine, send location and its LatLang to the calling activity.
                returnIntent.putExtra("locationName", locationName);
                setResult(Activity.RESULT_OK, returnIntent);
                finish();
            }
        });

        // Search for the location in google map entered by the user.
        btnSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Clear any marker present on the google map.
                map.clear();

                String location = searchLocation.getText().toString();

                List<Address> addressList = null;

                // If user has actually entered some search string, then proceed.
                if (!location.equals(""))
                {
                    Geocoder geocoder = new Geocoder(TaskMap.this);
                    try {
                        addressList = geocoder.getFromLocationName(location, 1);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    //If size is 0 then wrong location is entered.
                    if(addressList != null) {
                        if (addressList.size() == 0) {
                            Toast.makeText(TaskMap.this, "Invalid location.", Toast.LENGTH_SHORT).show();
                            searchLocation.setText("");
                        }
                        // If location is valid, show it on the google map with a marker.
                        else {
                            Address address = addressList.get(0);

                            String cityName = address.getAddressLine(0);
                            String stateName = address.getAddressLine(1);
                            String countryName = address.getAddressLine(2);

                            locationName = "";
                            if (cityName != null) {
                                locationName += cityName;
                            }
                            if (stateName != null) {
                                locationName += ", " + stateName;
                            }
                            if (countryName != null) {
                                locationName += ", " + countryName;
                            }
                            latitude = address.getLatitude();
                            longitude = address.getLongitude();

                            LatLng latLng = new LatLng(latitude, longitude);

                            // Set a marker on the location in google map.
                            MarkerOptions markerOptions = new MarkerOptions();
                            markerOptions.position(latLng);
                            markerOptions.title(locationName);
                            map.addMarker(markerOptions);
                            map.animateCamera(CameraUpdateFactory.newLatLngZoom(
                                    new LatLng(address.getLatitude(), address.getLongitude()), 14.0f));
                        }
                    }
                }
                else {
                    Toast.makeText(TaskMap.this, "Please enter a location.", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // Set a marker on current location of the user in google map.
        btnMyLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // First check if the GPS is enabled.
                LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
                // Prompt user to enable GPS if it is not enabled.
                if(!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                    showEnableGpsAlertDialogBox();
                }
                else {
                    // Wait before any location is retrieved.
                    // If location is retrieved, then proceed.
                    if (lastLocation != null) {
                        Geocoder geocoder = new Geocoder(TaskMap.this, Locale.getDefault());
                        List<Address> addresses = null;
                        try {
                            addresses = geocoder.getFromLocation(lastLocation.getLatitude(),
                                    lastLocation.getLongitude(), 1);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                        // If the location corresponds to a valid location address, then proceed.
                        if(addresses != null) {
                            if(addresses.size() == 0) {
                                Toast.makeText(TaskMap.this, "Invalid location. Please try again.",
                                        Toast.LENGTH_SHORT).show();
                            }
                            else {
                                String cityName = addresses.get(0).getAddressLine(0);
                                String stateName = addresses.get(0).getAddressLine(1);
                                String countryName = addresses.get(0).getAddressLine(2);

                                locationName = "";
                                if (cityName != null) {
                                    locationName += cityName;
                                }
                                if (stateName != null) {
                                    locationName += ", " + stateName;
                                }
                                if (countryName != null) {
                                    locationName += ", " + countryName;
                                }
                                latitude = lastLocation.getLatitude();
                                longitude = lastLocation.getLongitude();
                                // Show the marker on the google map.
                                setFocus();
                            }
                        }
                    }
                    else {
                        Toast.makeText(TaskMap.this, "Unable to retrieve your location. " +
                                        "Please wait for few moments to allow the app to retrieve your location.",
                                Toast.LENGTH_LONG).show();
                    }
                }
            }
        });

        // Set respective hints.
        if(getIntent().getExtras().getInt("taskId") == 2) {
            TextInputLayout textInputLayout = (TextInputLayout) findViewById(R.id.text_input_layout_location);
            textInputLayout.setHint("Trigger alarm at location?");
            hint = "Trigger alarm at this location";
            btnUseLocation.setText(hint);
        }
        else if(getIntent().getExtras().getInt("taskId") == 3) {
            TextInputLayout textInputLayout = (TextInputLayout) findViewById(R.id.text_input_layout_location);
            textInputLayout.setHint("Send message at location?");
            hint = "Send message at this location";
            btnUseLocation.setText(hint);
        }

        // Start 'ReminderService' service to get user's location.
        startReminderService();
        // Set up map if not already set.
        setUpMapIfNeeded();
    }

    private void startReminderService() {
        stopService = false;
        Intent intent = new Intent(TaskMap.this, ReminderService.class);
        startService(intent);
    }

    private void setUpMapIfNeeded() {
        // Do a null check to confirm that we have not already instantiated the map.
        if (map == null) {
            // Try to obtain the map from the SupportMapFragment.
            SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.map);
            mapFragment.getMapAsync(this);
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;
    }

    public void setFocus() {
        if (lastLocation != null) {
            map.clear();
            LatLng latLng = new LatLng(lastLocation.getLatitude(), lastLocation.getLongitude());
            MarkerOptions markerOptions = new MarkerOptions();
            markerOptions.position(latLng);
            markerOptions.title(locationName);
            map.addMarker(markerOptions);
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 14.0f));
        }
    }

    // Showing GPS settings alert dialog.
    // It will take user to GPS settings menu so that user can enable GPS.
    private void showEnableGpsAlertDialogBox() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);

        // Setting Dialog Title.
        alertDialog.setTitle("GPS ALERT");

        // Setting Dialog Message.
        alertDialog.setMessage("GPS is not enabled. Please enable it to allow the app to work properly.");

        // To prevent dismiss dialog box on back key pressed.
        alertDialog.setCancelable(false);

        // On pressing Settings button, take user to GPS settings menu.
        alertDialog.setPositiveButton("Enable", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(intent);
            }
        });

        // On pressing cancel button, dismiss the dialog box
        alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        // Showing Alert Message
        alertDialog.show();
    }
}