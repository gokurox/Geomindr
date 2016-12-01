package com.example.harish.geomindr.service.main;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.location.Location;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;

import com.example.harish.geomindr.R;
import com.example.harish.geomindr.activity.ebr.GooglePlacesReadTask;
import com.example.harish.geomindr.activity.ebr.PlaceMap;
import com.example.harish.geomindr.broadcast.alarm.AlarmDismissNotificationReceiver;
import com.example.harish.geomindr.broadcast.ebr.Cancel;
import com.example.harish.geomindr.broadcast.message.MessageConfirmSendReceiver;
import com.example.harish.geomindr.broadcast.message.MessageDeclineSendReceiver;
import com.example.harish.geomindr.broadcast.message.MessageSelectAgainReceiver;
import com.example.harish.geomindr.database.DatabaseHelper;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ReminderService extends Service implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    // To get the last known location.
    // For most cases, last known location is equivalent to current location.
    public static Location lastLocation;
    // Flag to stop the service once we have reached the destination location.
    // true = stop the service.
    // false = continue executing the service.
    public static boolean stopService;
    // Instance of GoogleApiClient.
    // Used to access google's FusedLocationApi (bette alternative of android's LocationListener).
    GoogleApiClient googleApiClient;
    // Device's current location's latitude.
    double curLatitude;
    // Device's current location's longitude.
    double curLongitude;
    // Creating an object of DatabaseHelper class.
    DatabaseHelper databaseHelper;
    // Radius for Entity Based Reminder.
    public static int PROXIMITY_RADIUS;
    String GOOGLE_API_KEY = "AIzaSyA68JFWLgTb_UzQQUUR_0ystLn6MFAvvR8";

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // Build the instance of GoogleApiClient.
        googleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();

        // Connect to the desired GoogleApiClient service.
        googleApiClient.connect();

        // Restart the service if it is killed by the android OS.
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        // Continue executing the service until stopService flag is true, i.e,
        // until we have not reached our destination location.
        if (!stopService) {
            Intent intent = new Intent(this, ReminderService.class);
            PendingIntent pendingIntent = PendingIntent.getService(this, 1000, intent, PendingIntent.FLAG_UPDATE_CURRENT);
            AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
            // Again start the service after 10 seconds.
            alarmManager.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + 10000, pendingIntent);
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    // Right now i am assuming that user will not explicitly revoke location permission manually from settings.
    @SuppressWarnings({"MissingPermission"})
    public void onConnected(Bundle bundle) {
        // Get device's last known location using google's FusedLocation API.
        lastLocation = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);

        if (lastLocation != null) {
            // First, check for Entity Based Reminders.
            checkEBRReminder();

            // Get the device's current location's latitude.
            // Only for debugging purpose
            curLatitude = lastLocation.getLatitude();
            // Get the device's current location's longitude.
            // Only for debugging purpose.
            curLongitude = lastLocation.getLongitude();

            //System.out.println(String.valueOf(curLatitude) + " : " + String.valueOf(curLongitude));

            // Access reminder objects from database and check if a reminder needs to be triggered.

            // Getting an instance of DatabaseHelper class.
            databaseHelper = DatabaseHelper.getInstance(ReminderService.this);
            // Retrieving all records from the database.
            Cursor res = databaseHelper.getAllRecordsTBR();

            // Check if there is something in the database
            if (res.getCount() > 0) {
                // Iterating through the retrieved records.
                while (res.moveToNext()) {
                    // If it is a location based message reminder (arrival type), then task_id equals 3.
                    // Also check the status of the reminder. If status is 0 then it means that
                    // app has not sent the arrival message. If status is 1 then it means that app has already
                    // sent the arrival message. The status will change from 1 to 0 when app sends departure
                    // message to the user.
                    if (res.getInt(1) == 3 && res.getInt(12) == 0) {
                        // Create a Location object for destination location.
                        Location destLocation = new Location("dest_location");
                        destLocation.setLatitude(res.getDouble(9));
                        destLocation.setLongitude(res.getDouble(10));

                        // Calculate the distance between device's current location and destination location.
                        double distance = lastLocation.distanceTo(destLocation);
                        //Toast.makeText(this, String.valueOf(distance), Toast.LENGTH_SHORT).show();

                        // If the distance is less than radius, then pop out the notification.
                        if (distance < res.getInt(11)) {
                            // Update the status of reminder to 1.
                            databaseHelper.updateStatus(res.getInt(0), 1);
                            // Update the status of departure message task reminder to 1.
                            databaseHelper.updateStatus(res.getInt(0)+1, 1);
                            // Send appropriate notification to the user.
                            sendMessageNotification(3, res.getString(4), res.getString(5),
                                    res.getString(6), res.getString(8));
                        }
                    }
                    // If it is a location based message reminder (departure type), then task_id equals 5.
                    else if (res.getInt(1) == 5 && res.getInt(12) == 1) {
                        // Create a Location object for destination location.
                        Location destLocation = new Location("dest_location");
                        destLocation.setLatitude(res.getDouble(9));
                        destLocation.setLongitude(res.getDouble(10));

                        // Calculate the distance between device's current location and destination location.
                        double distance = lastLocation.distanceTo(destLocation);
                        //Toast.makeText(this, String.valueOf(distance), Toast.LENGTH_SHORT).show();

                        // If the distance is more than radius, then pop out the notification.
                        if (distance > res.getInt(11)) {
                            // Update the status of reminder to 0.
                            databaseHelper.updateStatus(res.getInt(0), 0);
                            // Update the status of arrival message task reminder to 0.
                            databaseHelper.updateStatus(res.getInt(0)-1, 0);
                            // Send appropriate notification to the user.
                            sendMessageNotification(5, res.getString(4), res.getString(5),
                                    res.getString(7), res.getString(8));
                        }
                    }
                    // If it is a location based alarm reminder, then task_id equals 2.
                    else if (res.getInt(1) == 2) {
                        // Create a Location object for destination location.
                        Location destLocation = new Location("dest_location");
                        destLocation.setLatitude(res.getDouble(9));
                        destLocation.setLongitude(res.getDouble(10));

                        // Calculate the distance between device's current location and destination location
                        double distance = lastLocation.distanceTo(destLocation);
                        //System.out.println(distance);

                        // If the distance is less than radius and reminder has not yet been triggered,
                        // then pop out the notification.
                        if (distance < res.getInt(11) && res.getInt(12) == 0)
                        {
                            // Update the status to 1.
                            databaseHelper.updateStatus(res.getInt(0), 1);
                            // Send appropriate notification to the user.
                            sendAlarmNotification(res.getString(3), res.getString(6), res.getString(8));
                        }
                        // If the reminder has already been triggered and user has
                        // left the location, then change the status back to 0 again.
                        else if (res.getInt(12) == 1 && distance > 2.5 * (res.getInt(11))) {
                            // Update the status of reminder back to 0.
                            databaseHelper.updateStatus(res.getInt(0), 0);
                        }
                    }
                }
            }

            // Close the cursor.
            res.close();
        }

        // Stop the current service and start again if the lastLocation is null.
        stopSelf();
    }



    @Override
    public void onConnectionSuspended(int i) {

    }

    private void checkEBRReminder() {
        // Getting an instance of DatabaseHelper class.
        databaseHelper = DatabaseHelper.getInstance(ReminderService.this);
        // Retrieving all records from the database.
        Cursor res = databaseHelper.getAllRecordsEBR();

        // Check if there is something in the database
        if (res.getCount() > 0) {

            // Iterating through the retrieved records.
            while (res.moveToNext()) {
                // Check only if EBR record is inactive.
                SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm", Locale.ENGLISH);
                // Find current time.
                String currentDateTime = dateFormat.format(new Date());
                String indian_time[] = currentDateTime.split(":");
                int currHour = Integer.parseInt(indian_time[0]);

                if (currHour > 12) {
                    currHour = currHour - 12;
                }

                int currMin = Integer.parseInt(indian_time[1]);
                String data_time[] = res.getString(1).split(":");
                int dataHour = Integer.parseInt(data_time[0]);
                int dataMin = Integer.parseInt(data_time[1]);
                if ((res.getInt(6) == 0 && res.getDouble(3) == 0.0 && res.getDouble(4) == 0.0)) {
                    if (dataHour < currHour || (dataHour == currHour && dataMin <= currMin)) {
                        // Requesting this url for finding nearest locations
                        String googlePlacesUrl = "https://maps.googleapis.com/maps/api/place/nearbysearch/json?";
                        googlePlacesUrl += "location=" + lastLocation.getLatitude()
                                + "," + lastLocation.getLongitude();
                        googlePlacesUrl += "&radius=" + PROXIMITY_RADIUS;
                        googlePlacesUrl += "&types=" + res.getString(0);
                        googlePlacesUrl += "&sensor=true";
                        googlePlacesUrl += "&key=" + GOOGLE_API_KEY;

                        GooglePlacesReadTask googlePlacesReadTask = new GooglePlacesReadTask();
                        Object[] toPass = new Object[4];
                        toPass[0] = googlePlacesUrl;
                        toPass[1] = res.getString(0);
                        toPass[2] = res.getString(2);
                        toPass[3] = getApplicationContext();
                        googlePlacesReadTask.execute(toPass);
                    }
                }
                else if (res.getInt(6) == 0 && res.getDouble(3) != 0.0 && res.getDouble(4) != 0.0) {
                    databaseHelper.makeEBRActive(res.getString(0));
                    //send notification to the user
                    sendNotificationEBR(res.getString(0));
                }
            }
        }

        res.close();
        stopSelf();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);

        // Setting Dialog Title.
        alertDialog.setTitle("ALERT");

        // Setting Dialog Message.
        alertDialog.setMessage("Log in with your Facebook ID to allow the app" +
                " to post to your Facebook wall.");

        View view = LayoutInflater.from(this).inflate(R.layout.dialog_fb_login, null);

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

        alertDialog.show();

    }

    // Send alarm task reminder to the user.
    private void sendAlarmNotification(String title, String msg, String locationName) {
        // Send notification to the user.
        NotificationManager notificationManager = (NotificationManager)
                getSystemService(Context.NOTIFICATION_SERVICE);

        // Define sound URI.
        Uri soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);

        // If user has never set an alarm, then soundUri will be null.
        if (soundUri == null) {
            // soundUri is null, using backup.
            soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

            // I can't see this ever being null (as always have a default notification)
            // but just in case.
            if(soundUri == null) {
                // soundUri backup is null, using 2nd backup.
                soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);
            }
        }

        // This intent will be passed to Broadcast Receiver as a PendingIntent when user dismisses the alarm.
        Intent dismissIntent = new Intent(ReminderService.this, AlarmDismissNotificationReceiver.class);

        // If user dismisses the alarm.
        PendingIntent dismissPendingIntent = PendingIntent.getBroadcast
                (ReminderService.this, 0, dismissIntent, 0);

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(ReminderService.this)
                // Setting the title of the notification.
                .setContentTitle(title).setSmallIcon(R.drawable.ic_alarm_white_24dp)
                // Setting the content of the notification.
                .setStyle(new NotificationCompat.BigTextStyle().bigText
                        ("Looks like you have reached " + locationName + ". This is to remind you to " + msg))
                // Restrict user from swiping out the notification.
                .setOngoing(true)
                // Cancel on touch.
                .setAutoCancel(true)
                // Set the sound played with the notification.
                .setSound(soundUri)
                // Set vibration pattern
                .setVibrate(new long[] {1000, 1000, 0, 0, 1000, 1000})
                // Add Yes action to the notification.
                .addAction(R.drawable.ic_alarm_off_white_24dp, "Dismiss", dismissPendingIntent);

        // Passing an empty Intent to PendingIntent because we don't want to open any activity when user
        // clicks on the notification.
        PendingIntent resultPendingIntent = PendingIntent.getActivity(ReminderService.this,  0, new Intent(), 0);
        // Setting the PendingIntent on notification.
        notificationBuilder.setContentIntent(resultPendingIntent);

        // Build Notification based on specifications defined in Notification.Builder.
        Notification notification = notificationBuilder.build();
        // Setting this flag so that notification sound continues to play till user responds to the notification.
        notification.flags = Notification.FLAG_INSISTENT;
        // Finally, display the notification to the user.
        notificationManager.notify(2222, notification);
    }

    // Send message task reminder notification to the user.
    public void sendMessageNotification(int taskId, String name, String number, String msg, String locationName) {
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        // Intent passed to the Broadcast Receiver if user selects 'Yes'.
        Intent confirmIntent = new Intent(ReminderService.this, MessageConfirmSendReceiver.class);
        // Pass the recipient number.
        confirmIntent.putExtra("number", number);
        // Pass the message for the recipient.
        confirmIntent.putExtra("msg", msg);

        // Intent passed to the Broadcast Receiver if user selects 'No'.
        Intent declineIntent = new Intent(ReminderService.this, MessageDeclineSendReceiver.class);

        // Intent passed to the Broadcast Receiver if user does not select anything.
        Intent selectIntent = new Intent(ReminderService.this, MessageSelectAgainReceiver.class);

        // If user selects yes.
        PendingIntent confirmPendingIntent = PendingIntent.getBroadcast
                (ReminderService.this, 0, confirmIntent, 0);
        // If user selects no.
        PendingIntent declinePendingIntent = PendingIntent.getBroadcast
                (ReminderService.this, 0, declineIntent, 0);
        // If user clicks on the notification , i.e, selects nothing.
        PendingIntent selectPendingIntent = PendingIntent.getBroadcast
                (ReminderService.this, 0, selectIntent, 0);

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(ReminderService.this)
                // Setting the title of the notification.
                .setContentTitle("Message Reminder Alert").setSmallIcon(R.drawable.ic_textsms_white_24dp)
                // Vibrate the device twice when notification pops out.
                .setDefaults(Notification.DEFAULT_VIBRATE)
                // Sound the system's default ringtone when notification pops out.
                .setSound(Settings.System.DEFAULT_NOTIFICATION_URI)
                // Restrict user from swiping out the notification.
                .setOngoing(true)
                // Add Yes action to the notification.
                .addAction(R.drawable.ic_check_white_24dp, "Yes", confirmPendingIntent)
                // Add No action to the notification.
                .addAction(R.drawable.ic_close_white_24dp, "No", declinePendingIntent);

        // If it is an arrival message task reminder.
        if(taskId == 3) {
            if(name == null) {
                // Setting the content of the notification.
                notificationBuilder.setStyle(new NotificationCompat.BigTextStyle().bigText
                        ("Looks like you have arrived at " + locationName + "."
                                + " Do you want to send message \"" + msg + "\" to " + number + "."));
            }
            else {
                // Setting the content of the notification.
                notificationBuilder.setStyle(new NotificationCompat.BigTextStyle().bigText
                        ("Looks like you have arrived at " + locationName + "."
                                + " Do you want to send message \"" + msg + "\" to " + name + "."));
            }
        }
        // If it is a departure message task reminder.
        else {
            if(name == null) {
                // Setting the content of the notification.
                notificationBuilder.setStyle(new NotificationCompat.BigTextStyle().bigText
                        ("Looks like you are departing from " + locationName + "."
                                + " Do you want to send message \"" + msg + "\" to " + number + "."));
            }
            else {
                // Setting the content of the notification.
                notificationBuilder.setStyle(new NotificationCompat.BigTextStyle().bigText
                        ("Looks like you are departing from " + locationName + "."
                                + " Do you want to send message \"" + msg + "\" to " + name + "."));
            }
        }

        // Set pending intent to the notification.
        notificationBuilder.setContentIntent(selectPendingIntent);
        // Finally, display the notification to the user.
        notificationManager.notify(3333, notificationBuilder.build());
    }

    public void sendNotificationEBR(String entity) {
        String notName;
        int notId = (int) System.currentTimeMillis();

        //Passing all the necessary intents for PlaceMap activity for google map
        Intent mapIntent = new Intent(ReminderService.this, PlaceMap.class);
        mapIntent.putExtra("entity", entity);
        mapIntent.putExtra("curLat", lastLocation.getLatitude());
        mapIntent.putExtra("curLng", lastLocation.getLongitude());
        mapIntent.putExtra("notId", notId);

        //Intent for cancelling the reminder
        Intent cancelIntent = new Intent(ReminderService.this, Cancel.class);
        cancelIntent.putExtra("entity", entity);
        cancelIntent.putExtra("notId", notId);

        /*//Intent for Remind later the reminder
        Intent remindLaterIntent = new Intent(ReminderService.this, TimerNotification.class);
        remindLaterIntent.putExtra("entity", entity);
        remindLaterIntent.putExtra("notId", notId);*/

        //Intents to pass in actions
        PendingIntent mapPendingIntent = PendingIntent.getActivity
                (ReminderService.this, (int) System.currentTimeMillis(), mapIntent, 0);
        PendingIntent cancelPendingIntent = PendingIntent.getBroadcast
                (ReminderService.this, (int) System.currentTimeMillis(), cancelIntent, 0);
        /*PendingIntent remindLaterPendingIntent = PendingIntent.getActivity
                (ReminderService.this, (int) System.currentTimeMillis(), remindLaterIntent, 0);*/

        //Intents to set the action
        NotificationCompat.Action showAction = new NotificationCompat.Action.Builder
                (R.drawable.ic_check_white_24dp, "Yes", mapPendingIntent).build();
        NotificationCompat.Action cancelAction = new NotificationCompat.Action.Builder
                (R.drawable.ic_close_white_24dp, "No", cancelPendingIntent).build();
        /*NotificationCompat.Action remindLaterAction = new NotificationCompat.Action.Builder
                (R.drawable.ic_watch_later_white_24dp, "Later", remindLaterPendingIntent).build();*/

        //Switch cases to show the readable name on notification from the entity tag
        switch (entity) {
            case "atm":
                notName = "ATM";
                break;
            case "food":
                notName = "Restaurant";
                break;
            case "hospital":
                notName = "Hospital";
                break;
            case "police":
                notName = "Police";
                break;
            case "shopping_mall":
                notName = "Shopping Complex";
                break;
            case "pharmacy":
                notName = "Medical Store";
                break;
            case "gym":
                notName = "Gym";
                break;
            case "bank":
                notName = "Bank";
                break;
            case "post_office":
                notName = "Post Office";
                break;
            case "library":
                notName = "Library";
                break;
            case "bar":
                notName = "Bar";
                break;
            case "movie_theater":
                notName = "Movie Theatre";
                break;
            case "book_store":
                notName = "Book Store";
                break;
            case "local_government_office":
                notName = "Government Office";
                break;
            default:
                notName = "Petrol Pump";
                break;
        }

        // Build notification
        Notification notification = new NotificationCompat.Builder(ReminderService.this)
                .setContentTitle("Geomindr ALERT!")
                .setStyle(new NotificationCompat.BigTextStyle().bigText
                        ("Found " + notName + " nearby you. Do you want to have a look?"))
                .setSmallIcon(R.drawable.ic_notifications_white_24dp)
                .setContentIntent(mapPendingIntent)
                // Vibrate the device twice when notification pops out.
                .setDefaults(Notification.DEFAULT_VIBRATE)
                .setSound(Settings.System.DEFAULT_NOTIFICATION_URI)
                .setOngoing(true)
                .addAction(showAction)
                .addAction(cancelAction)
//                .addAction(remindLaterAction)
                .build();

        //Sending the notification
        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notificationManager.notify(notId, notification);
    }
}