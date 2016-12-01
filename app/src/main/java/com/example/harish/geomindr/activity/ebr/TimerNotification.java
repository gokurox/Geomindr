package com.example.harish.geomindr.activity.ebr;

import android.app.Activity;
import android.app.NotificationManager;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.example.harish.geomindr.R;
import com.example.harish.geomindr.database.DatabaseHelper;
import com.wdullaer.materialdatetimepicker.time.TimePickerDialog;

import java.util.Calendar;

public class TimerNotification extends Activity implements TimePickerDialog.OnTimeSetListener{

    //name of the entity taken from the intent passed in reminderService's sendnotification method
    String entity;
    // particular notification id for cancelling that notification after viewed by the user
    int notId;
    //database instance for deleting and adding
    DatabaseHelper databaseHelper;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_timer_notification);

        //Taking the intents
        entity = this.getIntent().getStringExtra("entity");
        notId = this.getIntent().getExtras().getInt("notId");

        //Cancelling the notification
        NotificationManager notificationManager = (NotificationManager) this.getSystemService(NOTIFICATION_SERVICE);
        notificationManager.cancel(notId);

        databaseHelper = DatabaseHelper.getInstance(this);

        //Calling the Timepicker dialog box
        Calendar now = Calendar.getInstance();
        TimePickerDialog tpd = TimePickerDialog.newInstance(
                TimerNotification.this,
                now.get(Calendar.HOUR_OF_DAY),
                now.get(Calendar.MINUTE),
                false
        );
        tpd.setVersion(TimePickerDialog.Version.VERSION_2);
        tpd.setTitle("Select your new time");
        tpd.setAccentColor(Color.parseColor("#E91E63"));
        tpd.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialogInterface) {
                Log.d("TimePicker", "Dialog was cancelled");
                endActivity();
            }

        });
        tpd.show(getFragmentManager(), "Timepickerdialog");
    }

    //After setting the time
    @Override
    public void onTimeSet(TimePickerDialog view, int hourOfDay, int minute, int second) {

        if(hourOfDay>12)
            hourOfDay = hourOfDay-12;

        // First all the objects of that particular entity is deleted
        Integer deleted = databaseHelper.deleteData(entity);
        if(deleted==0)
            Toast.makeText(this, "Nothing deleted", Toast.LENGTH_SHORT).show();
        // Then new entity is added afresh with updated time
        Boolean isInserted = databaseHelper.insertRecordEBR(entity,Integer.toString(hourOfDay)+":"+Integer.toString(minute),"", 0.0, 0.0, 0);
        if(!isInserted)
            Toast.makeText(this, "There is some error in adding data", Toast.LENGTH_SHORT).show();

//        Log.d("TimerNotification",Integer.toString(hourOfDay)+":"+Integer.toString(minute));
        endActivity();
    }

    //method to end the dialog box
    public void endActivity(){
        this.finish();
    }
}
