package com.example.harish.geomindr.activity.tbr.alarm;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.PorterDuff;
import android.location.Address;
import android.location.Geocoder;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.harish.geomindr.MainActivity;
import com.example.harish.geomindr.R;
import com.example.harish.geomindr.activity.map.TaskMap;
import com.example.harish.geomindr.database.DatabaseHelper;

import org.adw.library.widgets.discreteseekbar.DiscreteSeekBar;

import java.io.IOException;
import java.util.List;

import static android.widget.Toast.makeText;

public class AlarmTask extends AppCompatActivity {
    public final static int ALARM_MAP_REQUEST = 1;
    // Name of the location where alarm needs to be triggered.
    String locationName;
    // LatLang of the location where alarm needs to be triggered.
    double latitude, longitude;
    // Radius around location within which alarm needs to be triggered.
    int triggerRadius;
    // Title of the alarm.
    String title;
    // Alarm description.
    String description;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alarm_task);
        setTitle("Add Alarm Task Reminder");

        // Initializing View objects.
        final EditText alarmTitle = (EditText) findViewById(R.id.alarm_title);
        final EditText alarmDescription = (EditText) findViewById(R.id.alarm_description);
        final EditText alarmLocation = (EditText) findViewById(R.id.alarm_location);
        Button btnAlarmSave = (Button) findViewById(R.id.btn_alarm_save);
        Button btnAlarmDiscard = (Button) findViewById(R.id.btn_alarm_discard);
        FloatingActionButton fabAddLocation = (FloatingActionButton) findViewById(R.id.fab_add_location);
        final DiscreteSeekBar radius = (DiscreteSeekBar) findViewById(R.id.radius);

        // Setting background color for 'Save Reminder' button.
        btnAlarmSave.getBackground().setColorFilter(
                ResourcesCompat.getColor(getResources(), R.color.colorPrimaryDark, null),
                PorterDuff.Mode.MULTIPLY);
        // Setting background color for 'Discard Reminder' button.
        btnAlarmDiscard.getBackground().setColorFilter(
                ResourcesCompat.getColor(getResources(), R.color.colorPrimaryDark, null),
                PorterDuff.Mode.MULTIPLY);

        // Click listener on 'Add Location' FloatingActionButton
        fabAddLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // First check if the device is connected to the internet.
                ConnectivityManager connMgr = (ConnectivityManager)
                        getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();

                // If device is connected, open the 'TaskMap' activity.
                if (networkInfo != null && networkInfo.isConnected()) {
                    Intent a = new Intent(AlarmTask.this, TaskMap.class);
                    a.putExtra("taskId", 2);
                    startActivityForResult(a, ALARM_MAP_REQUEST);
                }
                else {
                    Toast.makeText(AlarmTask.this, "Please connect to the internet.",
                            Toast.LENGTH_SHORT).show();
                }
            }
        });

        // Click listener on 'Save Reminder' button.
        // It validates all fields before saving the reminder.
        // It also validates location entered by the user.
        btnAlarmSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                title = alarmTitle.getText().toString();
                description = alarmDescription.getText().toString();
                locationName = alarmLocation.getText().toString();

                if (title.isEmpty()) {
                    Toast.makeText(AlarmTask.this, "Please enter alarm task title.",
                            Toast.LENGTH_SHORT).show();
                }
                else if (description.isEmpty()) {
                    Toast.makeText(AlarmTask.this, "Please enter alarm task description.",
                            Toast.LENGTH_SHORT).show();
                }
                else if (locationName.isEmpty()) {
                    Toast.makeText(AlarmTask.this, "Please select a location.",
                            Toast.LENGTH_SHORT).show();
                }
                else {
                    triggerRadius = radius.getProgress();
                    // Check if the location set by the user is a valid location.
                    if (ifLocationValid(locationName)) {
                        // If location is valid, show the confirm dialog box.
                        showConfirmDialogBox();
                    }
                    else {
                        Toast.makeText(AlarmTask.this, "Invalid location. Please enter a valid location.",
                                Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

        // Click listener for 'Discard Reminder' button.
        btnAlarmDiscard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDiscardDialogBox();
            }
        });
    }

    // Dialog box to confirm that user wants to add the reminder.
    // If user confirms, we add the reminder to the database.
    private void showConfirmDialogBox() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);

        // Setting Dialog Title.
        alertDialog.setTitle("SAVE REMINDER");

        // Setting Dialog Message.
        alertDialog.setMessage("Do you want to save the reminder?");

        // To prevent dismiss dialog box on back key pressed.
        alertDialog.setCancelable(false);

        // On pressing Yes button, save data in the database.
        alertDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                // Close the dialog box.
                dialog.cancel();

                DatabaseHelper databaseHelper = DatabaseHelper.getInstance(AlarmTask.this);

                // Getting SharedPreference instance.
                SharedPreferences sharedPreferences = getApplicationContext().getSharedPreferences("COUNTER", Context.MODE_PRIVATE);
                // Getting SharedPreferences.Editor instance.
                // SharedPreferences.Editor instance is required to edit the SharedPreference file.
                SharedPreferences.Editor editor = sharedPreferences.edit();

                // Insert the record into the database.
                long isInserted = databaseHelper.insertRecordTBR(sharedPreferences.getInt("counter", -1), 2, 1,
                        title, null, null, description, null, locationName, latitude, longitude, triggerRadius);

                // Check whether the record is successfully inserted or not.
                if(isInserted >= 0) {
                    makeText(AlarmTask.this, "Reminder created.", Toast.LENGTH_LONG).show();
                    editor.putInt("counter", sharedPreferences.getInt("counter", -1) + 1);
                    editor.apply();
                }
                else {
                    makeText(AlarmTask.this, "Reminder not created. Please try again.", Toast.LENGTH_LONG).show();
                }

                // Start 'HomeFragment' fragment.
                startHomeFragment();
            }
        });

        // On pressing No button, dismiss the dialog box.
        alertDialog.setNegativeButton("No", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        // Showing Alert Message.
        alertDialog.show();
    }

    // Dialog box to confirm that user wants to discard the reminder.
    // If user confirms, we go back to 'HomeFragment' fragment.
    private void showDiscardDialogBox() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);

        // Setting Dialog Title.
        alertDialog.setTitle("ALERT");

        // Setting Dialog Message.
        alertDialog.setMessage("Do you want to discard the reminder?");

        // To prevent dismiss dialog box on back key pressed.
        alertDialog.setCancelable(false);

        // On pressing Yes button, discard the reminder and take user to home activity.
        alertDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
                startHomeFragment();
            }
        });

        // On pressing No button, dismiss the dialog box.
        alertDialog.setNegativeButton("No", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        // Showing Alert Message.
        alertDialog.show();
    }

    // Check if location entered by the user is valid or not.
    private boolean ifLocationValid(String location) {
        List<Address> addressList = null;

        Geocoder geocoder = new Geocoder(AlarmTask.this);
        try {
            addressList = geocoder.getFromLocationName(location, 1);
        } catch (IOException e) {
            e.printStackTrace();
        }

        // If size is 0 then wrong item is entered.
        if (addressList!= null && addressList.size() > 0) {
            latitude = addressList.get(0).getLatitude();
            longitude = addressList.get(0).getLongitude();
            return true;
        }

        return false;
    }

    // Start 'HomeFragment' fragment.
    private void startHomeFragment() {
        Intent intent = new Intent(AlarmTask.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }

    // Get the location name, latitude and longitude returned by 'TaskMap' activity.
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == ALARM_MAP_REQUEST) {
            if(resultCode == RESULT_OK) {
                locationName = data.getStringExtra("locationName");
                EditText alarmLocation = (EditText) findViewById(R.id.alarm_location);
                alarmLocation.setText(locationName);
            }
        }
    }

    // Confirm before user actually goes back to 'HomeFragment' fragment.
    @Override
    public void onBackPressed() {
        showDiscardDialogBox();
    }
}