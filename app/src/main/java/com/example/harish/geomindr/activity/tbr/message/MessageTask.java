package com.example.harish.geomindr.activity.tbr.message;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.PorterDuff;
import android.location.Address;
import android.location.Geocoder;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.provider.ContactsContract;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
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

public class MessageTask extends AppCompatActivity {

    private static final int MY_PERMISSIONS_REQUEST_READ_CONTACTS = 1;
    private static final int PICK_CONTACT_REQUEST = 2;
    private static final int MESSAGE_MAP_REQUEST = 3;

    // Name of the recipient to whom we want to send the message at the desired location.
    String name;
    // Contact number of the recipient.
    String number;
    // Arrival message
    String aMessage;
    // Departure message
    String dMessage;
    // Name of the location where alarm needs to be triggered.
    String locationName;
    // LatLang of the location where alarm needs to be triggered.
    double latitude, longitude;
    // Radius around location within which alarm needs to be triggered.
    int triggerRadius;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message_task);
        setTitle("Add Message Task Reminder");

        // We are not sure about the recipient's name.
        // What if user has no recipient name but only recipient number in his contacts app.
        // Or what if user manually enters a number in the EditText.
        name = null;

        // Initializing View objects
        final EditText recipientNumber = (EditText) findViewById(R.id.recipient_number);
        final EditText msgArrival = (EditText) findViewById(R.id.msg_arrival);
        final EditText msgDeparture = (EditText) findViewById(R.id.msg_departure);
        final EditText msgLocation = (EditText) findViewById(R.id.msg_location);
        FloatingActionButton fabContact = (FloatingActionButton) findViewById(R.id.fab_contact);
        FloatingActionButton fabAddLocation = (FloatingActionButton) findViewById(R.id.fab_add_location);
        final DiscreteSeekBar radius = (DiscreteSeekBar) findViewById(R.id.radius);
        Button btnMsgSave = (Button) findViewById(R.id.btn_msg_save);
        Button btnMsgDiscard = (Button) findViewById(R.id.btn_msg_discard);

        // Setting background color for 'Save Reminder' button.
        btnMsgSave.getBackground().setColorFilter(
                ResourcesCompat.getColor(getResources(), R.color.colorPrimaryDark, null),
                PorterDuff.Mode.MULTIPLY);
        // Setting background color for 'Discard Reminder' button.
        btnMsgDiscard.getBackground().setColorFilter(
                ResourcesCompat.getColor(getResources(), R.color.colorPrimaryDark, null),
                PorterDuff.Mode.MULTIPLY);

        // If user clicks on the button, send him to default contacts app of android.
        // He will select the desired contact and that contact will be fetched to the 'recipientNumber' EditText.
        fabContact.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Check for read contacts permission.
                int permissionCheck = ContextCompat.checkSelfPermission(MessageTask.this,
                        Manifest.permission.READ_CONTACTS);

                // If permission is not granted, prompt user for permission.
                if(permissionCheck != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(MessageTask.this,
                            new String[]{Manifest.permission.READ_CONTACTS},
                            MY_PERMISSIONS_REQUEST_READ_CONTACTS);
                    return;
                }
                // Pick contact from android's default contact app.
                pickContact();
            }
        });

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
                    Intent a = new Intent(MessageTask.this, TaskMap.class);
                    a.putExtra("taskId", 3);
                    startActivityForResult(a, MESSAGE_MAP_REQUEST);
                }
                else {
                    Toast.makeText(MessageTask.this, "Please connect to the internet.",
                            Toast.LENGTH_SHORT).show();
                }
            }
        });

        // Click listener on 'Save Reminder' button.
        // It validates all fields before saving the reminder.
        // It also validates location entered by the user.
        btnMsgSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                number = recipientNumber.getText().toString();
                aMessage = msgArrival.getText().toString();
                dMessage = msgDeparture.getText().toString();
                locationName = msgLocation.getText().toString();

                System.out.println(number);

                if (number.isEmpty()) {
                    Toast.makeText(MessageTask.this, "Please enter recipient's phone number.",
                            Toast.LENGTH_SHORT).show();
                }
                else if (aMessage.isEmpty()) {
                    Toast.makeText(MessageTask.this, "Please enter arrival message.",
                            Toast.LENGTH_SHORT).show();
                }
                else if (dMessage.isEmpty()) {
                    Toast.makeText(MessageTask.this, "Please enter departure message.",
                            Toast.LENGTH_SHORT).show();
                }
                else if (locationName.isEmpty()) {
                    Toast.makeText(MessageTask.this, "Please select a location.",
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
                        Toast.makeText(MessageTask.this, "Invalid location. Please enter a valid location.",
                                Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

        // Click listener for 'Discard Reminder' button.
        btnMsgDiscard.setOnClickListener(new View.OnClickListener() {
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

                DatabaseHelper databaseHelper = DatabaseHelper.getInstance(MessageTask.this);

                // Getting SharedPreference instance.
                SharedPreferences sharedPreferences = getApplicationContext().
                        getSharedPreferences("COUNTER", Context.MODE_PRIVATE);
                // Getting SharedPreferences.Editor instance.
                // SharedPreferences.Editor instance is required to edit the SharedPreference file.
                SharedPreferences.Editor editor = sharedPreferences.edit();

                // Insert the record into the database.
                // We need to insert two records - one for arrival message and one for departure message.
                long isInserted;

                if (name == null || name.isEmpty()) {
                    name = null;
                }

                isInserted = databaseHelper.insertRecordTBR(sharedPreferences.getInt("counter", -1), 3, 1,
                        null, name, number, aMessage, null, locationName,
                        latitude, longitude, triggerRadius);

                // Check whether the first record is successfully inserted or not.
                // If inserted, insert the second record.
                if (isInserted >= 0) {
                    // First increment the counter so that this record can get a unique ID as well.
                    editor.putInt("counter", sharedPreferences.getInt("counter", -1) + 1);
                    editor.apply();

                    isInserted = databaseHelper.insertRecordTBR(sharedPreferences.getInt("counter", -1), 5, 1,
                            null, name, number, null, dMessage, locationName,
                            latitude, longitude, triggerRadius);

                    if (isInserted >= 0) {
                        makeText(MessageTask.this, "Reminder created.", Toast.LENGTH_LONG).show();
                        editor.putInt("counter", sharedPreferences.getInt("counter", -1) + 1);
                        editor.apply();
                    }
                    else {
                        makeText(MessageTask.this, "Reminder not created. Please try again.",
                                Toast.LENGTH_LONG).show();
                    }
                }
                else {
                    makeText(MessageTask.this, "Reminder not created. Please try again.",
                            Toast.LENGTH_LONG).show();
                }

                // start 'HomeFragment' fragment.
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

        Geocoder geocoder = new Geocoder(MessageTask.this);
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
        Intent intent = new Intent(MessageTask.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }

    private void pickContact() {
        Intent pickContactIntent = new Intent(Intent.ACTION_PICK, Uri.parse("content://contacts"));
        // Show user only contacts w/ phone numbers.
        pickContactIntent.setType(ContactsContract.CommonDataKinds.Phone.CONTENT_TYPE);
        startActivityForResult(pickContactIntent, PICK_CONTACT_REQUEST);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Check which request it is that we're responding to.
        if (requestCode == PICK_CONTACT_REQUEST) {
            // Make sure the request was successful.
            if (resultCode == RESULT_OK) {
                // Get the URI that points to the selected contact.
                Uri contactUri = data.getData();
                // We only need the NUMBER column, because there will be only one row in the result.
                String[] projection = {ContactsContract.CommonDataKinds.Phone.NUMBER,
                        ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME};

                @SuppressLint("Recycle")
                Cursor cursor = getContentResolver().query(contactUri, projection, null, null, null);
                assert cursor != null;
                cursor.moveToFirst();

                // Retrieve the phone number from the NUMBER column.
                int column1 = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);
                number = cursor.getString(column1);
                // Retrieve the contact name from the DISPLAY_NAME column.
                int column2 = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME);
                name = cursor.getString(column2);

                EditText msg_number = (EditText) findViewById(R.id.recipient_number);
                // Set phone number and name on the EditText.
                msg_number.setText(number);
            }
        }
        // Get the location name, latitude and longitude returned by 'TaskMap' activity.
        else if(requestCode == MESSAGE_MAP_REQUEST) {
            if(resultCode == RESULT_OK) {
                locationName = data.getStringExtra("locationName");
                EditText msgLocation = (EditText) findViewById(R.id.msg_location);
                msgLocation.setText(locationName);
            }
        }
    }

    // Confirm before user actually goes back to 'HomeFragment' fragment.
    @Override
    public void onBackPressed() {
        showDiscardDialogBox();
    }
}