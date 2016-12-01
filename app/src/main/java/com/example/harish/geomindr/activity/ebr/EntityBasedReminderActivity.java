package com.example.harish.geomindr.activity.ebr;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Toast;

import com.example.harish.geomindr.MainActivity;
import com.example.harish.geomindr.R;
import com.example.harish.geomindr.database.DatabaseHelper;
import com.example.harish.geomindr.service.main.ReminderService;

import org.adw.library.widgets.discreteseekbar.DiscreteSeekBar;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class EntityBasedReminderActivity extends AppCompatActivity {
    // Buttons.
    Button atm, food, hospital, police, mall,
            pharmacy, gym, bank, postal, bar,
            lib, movie, books, gov, gas;

    DiscreteSeekBar radius;

     /*Checking if button is clicked or not.
     If 0 then not clicked and if 1 then clicked.*/
    boolean atmCheck = false,
            foodCheck = false,
            hospitalCheck = false,
            policeCheck = false,
            mallCheck = false,
            pharmacyCheck = false,
            gymCheck = false,
            bankCheck = false,
            postalCheck = false,
            barCheck = false,
            libCheck = false,
            movieCheck = false,
            booksCheck = false,
            govCheck = false,
            gasCheck = false;

    // Database instance.
    DatabaseHelper databaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_entity_based_reminder);
        setTitle("Remind me nearby?");

        atm = (Button) findViewById(R.id.btn_atm);
        food = (Button) findViewById(R.id.btn_food);
        hospital = (Button) findViewById(R.id.btn_hospital);
        police = (Button) findViewById(R.id.btn_police);
        mall = (Button) findViewById(R.id.btn_mall);
        pharmacy = (Button) findViewById(R.id.btn_pharmacy);
        gym = (Button) findViewById(R.id.btn_gym);
        bank = (Button) findViewById(R.id.btn_bank);
        postal = (Button) findViewById(R.id.btn_postal);
        bar = (Button) findViewById(R.id.btn_bar);
        lib = (Button) findViewById(R.id.btn_lib);
        movie = (Button) findViewById(R.id.btn_movie);
        books = (Button) findViewById(R.id.btn_books);
        gov = (Button) findViewById(R.id.btn_gov);
        gas = (Button) findViewById(R.id.btn_gas);

        radius = (DiscreteSeekBar) findViewById(R.id.radius);

        setRadius();

        radius.setOnProgressChangeListener(new DiscreteSeekBar.OnProgressChangeListener() {
            @Override
            public void onProgressChanged(DiscreteSeekBar seekBar, int value, boolean fromUser) {

            }

            @Override
            public void onStartTrackingTouch(DiscreteSeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(DiscreteSeekBar seekBar) {
                // Getting SharedPreference instance.
                // We will be using SharedPreferences to provide unique ID to reminders.
                SharedPreferences sharedPreferences = getApplicationContext().
                        getSharedPreferences("RADIUS", Context.MODE_PRIVATE);
                // Getting SharedPreferences.Editor instance.
                // SharedPreferences.Editor instance is required to edit the SharedPreference file.
                SharedPreferences.Editor editor = sharedPreferences.edit();
                if (sharedPreferences.getInt("radius", -1) == -1) {
                    editor.putInt("radius", 1000);
                    ReminderService.PROXIMITY_RADIUS = 1000;
                }
                else {
                    editor.putInt("radius", seekBar.getProgress());
                    ReminderService.PROXIMITY_RADIUS = seekBar.getProgress();
                }
                // Apply the changes to the SharedPreferences.
                editor.apply();
            }
        });

        databaseHelper = DatabaseHelper.getInstance(EntityBasedReminderActivity.this);

        Cursor res = databaseHelper.getAllRecordsEBR();

        //checking to see if the following buttons were clicked by user or not by fetching from database
        //this check is necessary for when the user reopens the app
        while(res.moveToNext()){
            String r = res.getString(0);
            switch (r) {
                case "atm":
                    atmCheck = true;
                    atm.setBackgroundColor(ContextCompat.getColor(this, R.color.button_clicked));
                    break;
                case "food":
                    foodCheck = true;
                    food.setBackgroundColor(ContextCompat.getColor(this, R.color.button_clicked));
                    break;
                case "hospital":
                    hospitalCheck = true;
                    hospital.setBackgroundColor(ContextCompat.getColor(this, R.color.button_clicked));
                    break;
                case "police":
                    policeCheck = true;
                    police.setBackgroundColor(ContextCompat.getColor(this, R.color.button_clicked));
                    break;
                case "shopping_mall":
                    mallCheck = true;
                    mall.setBackgroundColor(ContextCompat.getColor(this, R.color.button_clicked));
                    break;
                case "pharmacy":
                    pharmacyCheck = true;
                    pharmacy.setBackgroundColor(ContextCompat.getColor(this, R.color.button_clicked));
                    break;
                case "gym":
                    gymCheck = true;
                    gym.setBackgroundColor(ContextCompat.getColor(this, R.color.button_clicked));
                    break;
                case "bank":
                    bankCheck = true;
                    bank.setBackgroundColor(ContextCompat.getColor(this, R.color.button_clicked));
                    break;
                case "post_office":
                    postalCheck = true;
                    postal.setBackgroundColor(ContextCompat.getColor(this, R.color.button_clicked));
                    break;
                case "bar":
                    barCheck = true;
                    bar.setBackgroundColor(ContextCompat.getColor(this, R.color.button_clicked));
                    break;
                case "library":
                    libCheck = true;
                    lib.setBackgroundColor(ContextCompat.getColor(this, R.color.button_clicked));
                    break;
                case "movie_theater":
                    movieCheck = true;
                    movie.setBackgroundColor(ContextCompat.getColor(this, R.color.button_clicked));
                    break;
                case "book_store":
                    booksCheck = true;
                    books.setBackgroundColor(ContextCompat.getColor(this, R.color.button_clicked));
                    break;
                case "local_government_office":
                    govCheck = true;
                    gov.setBackgroundColor(ContextCompat.getColor(this, R.color.button_clicked));
                    break;
                case "gas_station":
                    gasCheck = true;
                    gas.setBackgroundColor(ContextCompat.getColor(this, R.color.button_clicked));
                    break;
            }
        }

        //All click listeners fro buttons
        atm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!atmCheck){
                    atmCheck = true;
                    atm.setBackgroundColor(ContextCompat.
                            getColor(EntityBasedReminderActivity.this, R.color.button_clicked));
                    saveData("atm", "");
                }
                else{
                    atmCheck = false;
                    atm.setBackgroundColor(ContextCompat.
                            getColor(EntityBasedReminderActivity.this, R.color.button_unclicked));
                    deleteData("atm");
                }
            }
        });

        pharmacy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!pharmacyCheck){
                    pharmacyCheck = true;
                    pharmacy.setBackgroundColor(ContextCompat.
                            getColor(EntityBasedReminderActivity.this, R.color.button_clicked));
                    saveData("pharmacy", "");
                }
                else{
                    pharmacyCheck = false;
                    pharmacy.setBackgroundColor(ContextCompat.
                            getColor(EntityBasedReminderActivity.this, R.color.button_unclicked));
                    deleteData("pharmacy");
                }
            }
        });

        postal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!postalCheck){
                    postalCheck = true;
                    postal.setBackgroundColor(ContextCompat.
                            getColor(EntityBasedReminderActivity.this, R.color.button_clicked));
                    saveData("post_office", "");
                }
                else{
                    postalCheck = false;
                    postal.setBackgroundColor(ContextCompat.
                            getColor(EntityBasedReminderActivity.this, R.color.button_unclicked));
                    deleteData("post_office");
                }
            }
        });

        books.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!booksCheck){
                    booksCheck = true;
                    books.setBackgroundColor(ContextCompat.
                            getColor(EntityBasedReminderActivity.this, R.color.button_clicked));
                    saveData("book_store", "");
                }
                else{
                    booksCheck = false;
                    books.setBackgroundColor(ContextCompat.
                            getColor(EntityBasedReminderActivity.this, R.color.button_unclicked));
                    deleteData("book_store");
                }
            }
        });

        gas.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!gasCheck){
                    gasCheck = true;
                    gas.setBackgroundColor(ContextCompat.
                            getColor(EntityBasedReminderActivity.this, R.color.button_clicked));
                    saveData("gas_station", "");
                }
                else{
                    gasCheck = false;
                    gas.setBackgroundColor(ContextCompat.
                            getColor(EntityBasedReminderActivity.this, R.color.button_unclicked));
                    deleteData("gas_station");
                }
            }
        });

        food.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!foodCheck){
                    showRemindAtDialog(2);
                }
                else{
                    foodCheck = false;
                    food.setBackgroundColor(ContextCompat.
                            getColor(EntityBasedReminderActivity.this, R.color.button_unclicked));
                    deleteData("food");
                }
            }
        });

        hospital.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!hospitalCheck){
                    showRemindAtDialog(3);
                }
                else
                {
                    hospitalCheck = false;
                    hospital.setBackgroundColor(ContextCompat.
                            getColor(EntityBasedReminderActivity.this, R.color.button_unclicked));
                    deleteData("hospital");
                }
            }
        });

        police.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!policeCheck){
                    showRemindAtDialog(4);
                }
                else{
                    policeCheck = false;
                    police.setBackgroundColor(ContextCompat.
                            getColor(EntityBasedReminderActivity.this, R.color.button_unclicked));
                    deleteData("police");
                }
            }
        });

        mall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!mallCheck){
                    showRemindAtDialog(5);
                }
                else{
                    mallCheck = false;
                    mall.setBackgroundColor(ContextCompat.
                            getColor(EntityBasedReminderActivity.this, R.color.button_unclicked));
                    deleteData("shopping_mall");
                }
            }
        });

        gym.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!gymCheck){
                    showRemindAtDialog(7);
                }
                else{
                    gymCheck = false;
                    gym.setBackgroundColor(ContextCompat.
                            getColor(EntityBasedReminderActivity.this, R.color.button_unclicked));
                    deleteData("gym");
                }
            }
        });

        bank.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!bankCheck){
                    showRemindAtDialog(8);
                }
                else{
                    bankCheck = false;
                    bank.setBackgroundColor(ContextCompat.
                            getColor(EntityBasedReminderActivity.this, R.color.button_unclicked));
                    deleteData("bank");
                    Toast.makeText(EntityBasedReminderActivity.this,
                            "Reminder removed.", Toast.LENGTH_SHORT).show();
                }
            }
        });

        bar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!barCheck){
                    showRemindAtDialog(10);
                }
                else{
                    barCheck = false;
                    bar.setBackgroundColor(ContextCompat.
                            getColor(EntityBasedReminderActivity.this, R.color.button_unclicked));
                    deleteData("bar");
                }
            }
        });

        lib.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!libCheck){
                    showRemindAtDialog(11);
                }
                else{
                    libCheck = false;
                    lib.setBackgroundColor(ContextCompat.
                            getColor(EntityBasedReminderActivity.this, R.color.button_unclicked));
                    deleteData("library");
                }
            }
        });

        movie.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!movieCheck){
                    showRemindAtDialog(12);
                }
                else{
                    movieCheck = false;
                    movie.setBackgroundColor(ContextCompat.
                            getColor(EntityBasedReminderActivity.this, R.color.button_unclicked));
                    deleteData("movie_theater");
                }
            }
        });

        gov.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!govCheck){
                    showRemindAtDialog(14);
                }
                else{
                    govCheck = false;
                    gov.setBackgroundColor(ContextCompat.
                            getColor(EntityBasedReminderActivity.this, R.color.button_unclicked));
                    deleteData("local_government_office");
                }
            }
        });

    }

    private void setRadius() {
        // Getting SharedPreference instance.
        // We will be using SharedPreferences to provide unique ID to reminders.
        SharedPreferences sharedPreferences = getApplicationContext().
                getSharedPreferences("RADIUS", Context.MODE_PRIVATE);
        ReminderService.PROXIMITY_RADIUS = sharedPreferences.getInt("radius", 1000);
        radius.setProgress(ReminderService.PROXIMITY_RADIUS);
    }

    //Dialog box for particular entities where pafticular location can matter for the user
    private void showRemindAtDialog(final int btnPos) {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(EntityBasedReminderActivity.this);

        // Setting Dialog Title.
        alertDialog.setTitle("Remind at?");

        // Set the dialog layout.
        View view = LayoutInflater.from(this).inflate(R.layout.dialog, null);
        alertDialog.setView(view);

        final CheckBox checkAny = (CheckBox) view.findViewById(R.id.checkAny);
        final CheckBox checkParticular = (CheckBox) view.findViewById(R.id.checkParticular);
        final EditText entityName = (EditText) view.findViewById(R.id.entity_name);

        String anyLocText = "Any Location";
        String particularLocText = "Particular Location";
        String entityNameHint = "Enter Location Name";

        switch (btnPos) {
            case 2:
                anyLocText = "Any Food Outlet";
                particularLocText = "Particular Food Outlet";
                entityNameHint = "Enter Food Outlet Name";
                break;
            case 3:
                anyLocText = "Any Hospital";
                particularLocText = "Particular Hospital";
                entityNameHint = "Enter Hospital Name";
                break;
            case 4:
                anyLocText = "Any Police Station";
                particularLocText = "Particular Police Station";
                entityNameHint = "Enter Police Station Name";
                break;
            case 5:
                anyLocText = "Any Shopping Complex";
                particularLocText = "Particular Shopping Complex";
                entityNameHint = "Enter Shopping Complex Name";
                break;
            case 7:
                anyLocText = "Any Gym";
                particularLocText = "Particular Gym";
                entityNameHint = "Enter Gym Name";
                break;
            case 8:
                anyLocText = "Any Bank";
                particularLocText = "Particular Bank";
                entityNameHint = "Enter Bank Name";
                break;
            case 10:
                anyLocText = "Any Bar";
                particularLocText = "Particular Bar";
                entityNameHint = "Enter Bar Name";
                break;
            case 11:
                anyLocText = "Any Library";
                particularLocText = "Particular Library";
                entityNameHint = "Enter Library Name";
                break;
            case 12:
                anyLocText = "Any Movie Theatre";
                particularLocText = "Particular Movie Theatre";
                entityNameHint = "Enter Movie Theatre Name";
                break;
            case 14:
                anyLocText = "Any Government Office";
                particularLocText = "Particular Government Office";
                entityNameHint = "Enter Government Office Name";
                break;
        }

        checkAny.setText(anyLocText);
        checkParticular.setText(particularLocText);
        entityName.setHint(entityNameHint);

        checkParticular.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean selected) {
                if(selected) {
                    entityName.setVisibility(View.VISIBLE);
                    checkAny.setEnabled(false);
                }
                else{
                    entityName.setVisibility(View.GONE);
                    checkAny.setEnabled(true);
                }
            }
        });

        checkAny.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean selected) {
                if(selected) {
                    entityName.setVisibility(View.GONE);
                    checkParticular.setEnabled(false);
                }
                else{
                    entityName.setVisibility(View.GONE);
                    checkParticular.setEnabled(true);
                }
            }
        });

        // On pressing Save button.
        alertDialog.setPositiveButton("Save", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                if(!checkAny.isChecked() && !checkParticular.isChecked()){
                    Toast.makeText(EntityBasedReminderActivity.this,
                            "Please select appropriate checkbox.", Toast.LENGTH_SHORT).show();
                }
                else if(checkAny.isChecked() && checkParticular.isChecked()){
                    Toast.makeText(EntityBasedReminderActivity.this,
                            "Please select one checkbox only.", Toast.LENGTH_SHORT).show();
                }
                //if any random location is allowed by the user
                else if(checkAny.isChecked()){
                    switch (btnPos) {
                        case 2:
                            foodCheck = true;
                            food.setBackgroundColor(ContextCompat.getColor
                                    (EntityBasedReminderActivity.this, R.color.button_clicked));
                            saveData("food", "");
                            break;
                        case 3:
                            hospitalCheck = true;
                            hospital.setBackgroundColor(ContextCompat.getColor
                                    (EntityBasedReminderActivity.this, R.color.button_clicked));
                            saveData("hospital", "");
                            break;
                        case 4:
                            policeCheck = true;
                            police.setBackgroundColor(ContextCompat.getColor
                                    (EntityBasedReminderActivity.this, R.color.button_clicked));
                            saveData("police", "");
                            break;
                        case 5:
                            mallCheck = true;
                            mall.setBackgroundColor(ContextCompat.getColor
                                    (EntityBasedReminderActivity.this, R.color.button_clicked));
                            saveData("shopping_mall", "");
                            break;
                        case 7:
                            gymCheck = true;
                            gym.setBackgroundColor(ContextCompat.getColor
                                    (EntityBasedReminderActivity.this, R.color.button_clicked));
                            saveData("gym", "");
                            break;
                        case 8:
                            bankCheck = true;
                            bank.setBackgroundColor(ContextCompat.getColor
                                    (EntityBasedReminderActivity.this, R.color.button_clicked));
                            saveData("bank", "");
                            break;
                        case 10:
                            barCheck = true;
                            bar.setBackgroundColor(ContextCompat.getColor
                                    (EntityBasedReminderActivity.this, R.color.button_clicked));
                            saveData("bar", "");
                            break;
                        case 11:
                            libCheck = true;
                            lib.setBackgroundColor(ContextCompat.getColor
                                    (EntityBasedReminderActivity.this, R.color.button_clicked));
                            saveData("library", "");
                            break;
                        case 12:
                            movieCheck = true;
                            movie.setBackgroundColor(ContextCompat.getColor
                                    (EntityBasedReminderActivity.this, R.color.button_clicked));
                            saveData("movie_theater", "");
                            break;
                        case 14:
                            govCheck = true;
                            gov.setBackgroundColor(ContextCompat.getColor
                                    (EntityBasedReminderActivity.this, R.color.button_clicked));
                            saveData("local_government_office", "");
                            break;
                    }
                    dialog.cancel();
                }
                //if particular location is set by the user
                else if(checkParticular.isChecked() && !entityName.getText().toString().equals("")) {
                    switch (btnPos) {
                        case 2:
                            foodCheck = true;
                            food.setBackgroundColor(ContextCompat.getColor
                                    (EntityBasedReminderActivity.this, R.color.button_clicked));
                            saveData("food", entityName.getText().toString());
                            break;
                        case 3:
                            hospitalCheck = true;
                            hospital.setBackgroundColor(ContextCompat.getColor
                                    (EntityBasedReminderActivity.this, R.color.button_clicked));
                            saveData("hospital", entityName.getText().toString());
                            break;
                        case 4:
                            policeCheck = true;
                            police.setBackgroundColor(ContextCompat.getColor
                                    (EntityBasedReminderActivity.this, R.color.button_clicked));
                            saveData("police", entityName.getText().toString());
                            break;
                        case 5:
                            mallCheck = true;
                            mall.setBackgroundColor(ContextCompat.getColor
                                    (EntityBasedReminderActivity.this, R.color.button_clicked));
                            saveData("shopping_mall", entityName.getText().toString());
                            break;
                        case 7:
                            gymCheck = true;
                            gym.setBackgroundColor(ContextCompat.getColor
                                    (EntityBasedReminderActivity.this, R.color.button_clicked));
                            saveData("gym", entityName.getText().toString());
                            break;
                        case 8:
                            bankCheck = true;
                            bank.setBackgroundColor(ContextCompat.getColor
                                    (EntityBasedReminderActivity.this, R.color.button_clicked));
                            saveData("bank", entityName.getText().toString());
                            break;
                        case 10:
                            barCheck = true;
                            bar.setBackgroundColor(ContextCompat.getColor
                                    (EntityBasedReminderActivity.this, R.color.button_clicked));
                            saveData("bar", entityName.getText().toString());
                            break;
                        case 11:
                            libCheck = true;
                            lib.setBackgroundColor(ContextCompat.getColor
                                    (EntityBasedReminderActivity.this, R.color.button_clicked));
                            saveData("library", entityName.getText().toString());
                            break;
                        case 12:
                            movieCheck = true;
                            movie.setBackgroundColor(ContextCompat.getColor
                                    (EntityBasedReminderActivity.this, R.color.button_clicked));
                            saveData("movie_theater", entityName.getText().toString());
                            break;
                        case 14:
                            govCheck = true;
                            gov.setBackgroundColor(ContextCompat.getColor
                                    (EntityBasedReminderActivity.this, R.color.button_clicked));
                            saveData("local_government_office", entityName.getText().toString());
                            break;
                    }
                    dialog.cancel();
                }
                else {
                    Toast.makeText(EntityBasedReminderActivity.this,
                            "Incomplete input. Please try again.", Toast.LENGTH_SHORT).show();
                }
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

    //saving the data into the database
    public void saveData(String entity,String name){
        SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm", Locale.ENGLISH);
        // Find current time
        String currentDateTime = dateFormat.format(new Date());
        String indian_time[] = currentDateTime.split(":");
        int currTime = Integer.parseInt(indian_time[0]);

        if (currTime > 12) {
            currTime = currTime - 12;
        }

        currentDateTime = Integer.toString(currTime) + ":" + indian_time[1];
        Boolean isInserted = databaseHelper.insertRecordEBR(entity, currentDateTime, name, 0.0, 0.0, 0);

        if (isInserted) {
            Toast.makeText(this, "Reminder added.", Toast.LENGTH_SHORT).show();
            startService(new Intent(this, ReminderService.class));
        }
        else {
            Toast.makeText(this, "Reminder not added. Please try again.", Toast.LENGTH_SHORT).show();
        }
    }

    //deleting the data
    public void deleteData(String entity){
        Integer deleted = databaseHelper.deleteData(entity);
        if(deleted<=0) {
            Toast.makeText(this, "Nothing deleted.", Toast.LENGTH_SHORT).show();
        }
        else {
            Toast.makeText(this, "Reminder deleted.", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();

    }
}