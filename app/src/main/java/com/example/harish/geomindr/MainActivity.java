package com.example.harish.geomindr;

import android.Manifest;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.example.harish.geomindr.database.DatabaseHelper;
import com.example.harish.geomindr.fragment.main.HomeFragment;
import com.example.harish.geomindr.fragment.other.DemoFragment2;
import com.example.harish.geomindr.service.main.ReminderService;

import static com.example.harish.geomindr.service.main.ReminderService.stopService;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    // Request code for asking permission for accessing user's location.
    public static final int PERMISSION_LOCATION_REQUEST_CODE = 1;
    // Is the dialog box shown already?
    boolean alreadyDialogBoxShown;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        alreadyDialogBoxShown = false;

        // Check if the app has permission to access user's location.
        int permissionGPSCheck = ContextCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.ACCESS_FINE_LOCATION);
        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        // If the user has not granted the permission, prompt it to grant the permission.
        // Do not proceed until the user has granted the permission.
        if (permissionGPSCheck != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSION_LOCATION_REQUEST_CODE);
        }
        // Prompt user to enable GPS if it is not enabled.
        else if(!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            alreadyDialogBoxShown = true;
            showEnableGpsAlertDialogBox();
        }

        // Use ActionBar as Toolbar.
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Enable services if there are some reminder tasks in the database.
        // If the service is already running, it will not affect anything.
        DatabaseHelper databaseHelper = DatabaseHelper.getInstance(this);
        // Check if there are some reminders in the database.
        Cursor cursor = databaseHelper.getAllRecordsTBR();

        // If there are any reminders in the database, then start 'ReminderService' service
        if (cursor.getCount() > 0) {
            // First close the cursor.
            cursor.close();
            // Pass an appropriate intent.
            Intent intent = new Intent(this, ReminderService.class);
            // 1000 is the request code for this PendingIntent.
            PendingIntent pendingIntent = PendingIntent.getService(this,
                    1000, intent, PendingIntent.FLAG_UPDATE_CURRENT);
            // Get AlarmManager to schedule start of 'ReminderService' service.
            AlarmManager alarmManager = (AlarmManager)getSystemService(ALARM_SERVICE);
            // Start the service after 1 second.
            alarmManager.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + 1000, pendingIntent);
        }
        else {
            cursor.close();
        }

        // Creating a navigation drawer.
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        View headerView = navigationView.getHeaderView(0);
        headerView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startHomeFragment();
                // Close the drawer after user has selected the desired item.
                DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
                drawer.closeDrawer(GravityCompat.START);
            }
        });

        // Getting SharedPreference instance.
        // We will be using SharedPreferences to provide unique ID to reminders.
        SharedPreferences sharedPreferencesCounter = getApplicationContext().
                getSharedPreferences("COUNTER", Context.MODE_PRIVATE);
        SharedPreferences sharedPreferencesRadius = getApplicationContext().
                getSharedPreferences("RADIUS", Context.MODE_PRIVATE);
        // Getting SharedPreferences.Editor instance.
        // SharedPreferences.Editor instance is required to edit the SharedPreference file.
        SharedPreferences.Editor editorCounter = sharedPreferencesCounter.edit();
        SharedPreferences.Editor editorRadius = sharedPreferencesRadius.edit();
        // Check if we have a 'counter' key in our SharedPreferences.
        // The value of this key is used to give unique ID to the reminders.
        if (sharedPreferencesCounter.getInt("counter", -1) == -1) {
            editorCounter.putInt("counter", 0);
        }
        if (sharedPreferencesRadius.getInt("radius", -1) == -1) {
            editorRadius.putInt("radius", 1000);
            ReminderService.PROXIMITY_RADIUS = 1000;
        }
        // Apply the changes to the SharedPreferences.
        editorCounter.apply();
        editorRadius.apply();

        // Starting Broadcast Receivers for Wifi and GPS.
        // Pass an appropriate intent.
        /*Intent wifiIntent = new Intent(this, WifiReceiver.class);
        // 1000 is the request code for this PendingIntent.
        PendingIntent wifiPendingIntent = PendingIntent.getBroadcast(this,
                1000, wifiIntent, 0);
        // Get AlarmManager to schedule start of 'ReminderService' service.
        AlarmManager alarmManager = (AlarmManager)getSystemService(ALARM_SERVICE);
        // Start the service after 1 second.
        alarmManager.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + 1000, wifiPendingIntent);*/

        // start 'HomeFragment' fragment
        startHomeFragment();
    }

    // Start 'HomeFragment' fragment.
    private void startHomeFragment() {
        setTitle("Geomindr");
        Fragment fragment = new HomeFragment();
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction
                .replace(R.id.fragment_container, fragment)
                .commit();
    }

    // Handing back button press by the user.
    // If DrawerLayout is opened, then we will close it on back button press,
    // else we will close the app.
    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu.
        // This adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handling action bar item clicks.
        int id = item.getItemId();

        // Start 'ReminderService' service on item click.
        if (id == R.id.action_start_service) {
            stopService = false;
            startService(new Intent(this, ReminderService.class));
            Toast.makeText(getApplicationContext(), "Reminder service started.", Toast.LENGTH_SHORT).show();
            return true;
        }
        // Stop 'ReminderService' service on item click.
        else if (id == R.id.action_stop_service) {
            stopService = true;
            stopService(new Intent(this, ReminderService.class));
            Toast.makeText(getApplicationContext(), "Reminder service stopped", Toast.LENGTH_SHORT).show();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        // Handling navigation view item clicks here.

        Fragment fragment = null;

        // Getting the id of the selected item.
        int id = item.getItemId();

        // If home is selected.
        if (id == R.id.nav_home) {
            setTitle("Geomindr");
            fragment = new HomeFragment();
        }
        // If settings item is selected.
        else if (id == R.id.nav_settings) {
            setTitle("Settings");
            fragment = new DemoFragment2();
        }
        // If about item is selected.
        else if (id == R.id.nav_about) {
            setTitle("About");
            fragment = new DemoFragment2();
        }

        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction
                .replace(R.id.fragment_container, fragment)
                .commit();

        // Close the drawer after user has selected the desired item.
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);

        return true;
    }

    // If the user does not allow the app to access device's location,
    // then show an alert to the user. If the user cancels the alert, then exit from the app.
    // Don't allow user to continue unless it provides location access permission to the app.
    private void showLocationAlertDialogBox() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(MainActivity.this);

        // Setting Dialog Title.
        alertDialog.setTitle("ALERT");

        // Setting Dialog Message.
        alertDialog.setMessage("The app won't be able to function without access to your device's location." +
                "Please allow access to continue.");

        // To prevent dismiss dialog box on back key pressed.
        alertDialog.setCancelable(false);

        // On pressing Ok button, again show permission dialog to the user.
        alertDialog.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                ActivityCompat.requestPermissions(MainActivity.this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_LOCATION_REQUEST_CODE);
            }
        });

        // On pressing cancel button, exit from the app.
        alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
                finish();
            }
        });

        // Show AlertDialog.
        alertDialog.show();
    }

    // Showing GPS settings alert dialog.
    // It will take user to GPS settings menu so that user can enable GPS.
    private void showEnableGpsAlertDialogBox() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(MainActivity.this);

        // Setting Dialog Title.
        alertDialog.setTitle("ALERT");

        // Setting Dialog Message.
        alertDialog.setMessage("The app uses your device's GPS to give location specific reminders. Please enable" +
                " GPS to make the app function properly.");

        // To prevent dismiss dialog box on back key pressed.
        alertDialog.setCancelable(false);

        // On pressing Settings button, take user to GPS settings menu.
        alertDialog.setPositiveButton("Enable", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(intent);
            }
        });

        // On pressing cancel button, show alert to the user.
        alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        // Showing Alert Message
        alertDialog.show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            // Checking whether user has granted location access permission or not.
            case PERMISSION_LOCATION_REQUEST_CODE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_DENIED) {
                    // Permission denied, boo!
                    // Show alert dialog box.
                    showLocationAlertDialogBox();
                }
            }
            break;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Check if the app has permission to access user's location.
        int permissionGPSCheck = ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION);

        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        // If app has access to user's location but GPS is not enabled,
        // the show the appropriate alert to the user
        if(permissionGPSCheck == PackageManager.PERMISSION_GRANTED &&
                !locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) && !alreadyDialogBoxShown) {
            showEnableGpsAlertDialogBox();
        }
    }
}