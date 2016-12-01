package com.example.harish.geomindr.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {
    // Name of the database.
    private static final String DATABASE_NAME = "task_reminder.db";
    // Name of the tables in the database.
    private static final String TABLE_TBR = "reminder_tbr_table";
    private static final String TABLE_EBR = "reminder_ebr_table";
    // Fields present in the tbr table.
    private static final String COL_0_TBR = "TBR_ID";
    private static final String COL_1_TBR = "TASK_ID";
    private static final String COL_2_TBR = "REMINDER_TYPE";
    private static final String COL_3_TBR = "TITLE";
    private static final String COL_4_TBR = "NAME";
    private static final String COL_5_TBR = "NUMBER";
    private static final String COL_6_TBR = "ARRIVAL_MESSAGE";
    private static final String COL_7_TBR = "DEPARTURE_MESSAGE";
    private static final String COL_8_TBR = "LOCATION_NAME";
    private static final String COL_9_TBR = "LOCATION_LATITUDE";
    private static final String COL_10_TBR = "LOCATION_LONGITUDE";
    private static final String COL_11_TBR = "RADIUS";
    private static final String COL_12_TBR = "REMINDER_STATUS";
    // Fields present in ebr table.
    private static final String COL_1_EBR = "ENTITY";
    private static final String COL_2_EBR = "REALTIME";
    private static final String COL_3_EBR = "NAME";
    private static final String COL_4_EBR = "LATITUDE";
    private static final String COL_5_EBR = "LONGITUDE";
    private static final String COL_6_EBR = "ACTIVE";
    // Singleton instance of the database.
    private static DatabaseHelper instance = null;

    // Constructor to instantiate an object of DatabaseHelper class.
    // This will create the database if it doesn't exist.
    // The constructor is private to ensure that there is only one instance
    // of the database present in the application at a time.
    private DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, 1);
    }

    // Get instance of the database.
    public static DatabaseHelper getInstance(Context context) {
        // Use the application context, which will ensure that you
        // don't accidentally leak an Activity's context.
        if (instance == null) {
            instance = new DatabaseHelper(context.getApplicationContext());
        }
        return instance;
    }

    // Method to create the table in the database.
    // This method will only run if the database file do not exist.
    // TBR_ID is the primary key of the database.
    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL("CREATE TABLE " + TABLE_TBR + "(" +
                "TBR_ID INTEGER PRIMARY KEY," +
                "TASK_ID INTEGER," +
                "REMINDER_TYPE INTEGER," +
                "TITLE TEXT," +
                "NAME TEXT," +
                "NUMBER TEXT," +
                "ARRIVAL_MESSAGE TEXT," +
                "DEPARTURE_MESSAGE TEXT," +
                "LOCATION_NAME TEXT," +
                "LOCATION_LATITUDE REAL," +
                "LOCATION_LONGITUDE REAL," +
                "RADIUS INTEGER," +
                "REMINDER_STATUS INTEGER)");

        sqLiteDatabase.execSQL("CREATE TABLE " + TABLE_EBR + "(" +
                "ENTITY TEXT," +
                "REALTIME TEXT," +
                "NAME TEXT," +
                "LATITUDE REAL," +
                "LONGITUDE REAL," +
                "EBR_ID INTEGER PRIMARY KEY AUTOINCREMENT," +
                "ACTIVE INTEGER)");
    }

    // Upgrade the database (if required)
    // This method is called when version of our DB changes which means underlying table structure changes etc.
    // It will first delete the old database and then will call onCreate() method to create a new database.
    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + "reminder_add_table");
        onCreate(sqLiteDatabase);
    }

    // Method to insert record into the database.
    // It returns -1 if record is not inserted in the database.
    // It returns a number >= 0 if record is inserted in the database.
    public long insertRecordTBR(int reminderId, int taskId, int reminderType, String title, String name, String number,
                      String arrivalMessage, String departureMessage, String locationName,
                      double locationLatitude, double locationLongitude, int radius) {
        // Get the database instance.
        SQLiteDatabase db = this.getWritableDatabase();
        // ContentValues provide an empty set of name-value pair.
        ContentValues contentValues = new ContentValues();
        contentValues.put(COL_0_TBR, reminderId);
        contentValues.put(COL_1_TBR, taskId);
        contentValues.put(COL_2_TBR, reminderType);
        contentValues.put(COL_3_TBR, title);
        contentValues.put(COL_4_TBR, name);
        contentValues.put(COL_5_TBR, number);
        contentValues.put(COL_6_TBR, arrivalMessage);
        contentValues.put(COL_7_TBR, departureMessage);
        contentValues.put(COL_8_TBR, locationName);
        contentValues.put(COL_9_TBR, locationLatitude);
        contentValues.put(COL_10_TBR, locationLongitude);
        contentValues.put(COL_11_TBR, radius);
        // Status = 0 means reminder is not triggered.
        contentValues.put(COL_12_TBR, 0);

        // Insert the record to the specified table in the database and
        // return the result of the operation.
        return db.insert(TABLE_TBR, null, contentValues);
    }

    public boolean insertRecordEBR(String entity, String realtime, String name,
                                   double latitude, double longitude, int active) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues contentValues = new ContentValues();
        contentValues.put(COL_1_EBR, entity);
        contentValues.put(COL_2_EBR, realtime);
        contentValues.put(COL_3_EBR, name);
        contentValues.put(COL_4_EBR, latitude);
        contentValues.put(COL_5_EBR, longitude);
        contentValues.put(COL_6_EBR, active);

        long result = db.insert(TABLE_EBR ,null ,contentValues);
        return result != -1;
    }

    // Method to retrieve all records from the specified table in the database.
    public Cursor getAllRecordsTBR() {
        // Get the database instance.
        SQLiteDatabase db = this.getWritableDatabase();
        // Select all records from the specified table in the database.
        return db.rawQuery("SELECT * FROM " + TABLE_TBR, null);
    }

    public Cursor getAllRecordsEBR() {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.rawQuery("SELECT * FROM " + TABLE_EBR, null);
    }

    public Cursor getRecordsByEntity(String entity) {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.rawQuery("SELECT * FROM " + TABLE_EBR + " WHERE ENTITY='" + entity + "'", null);
    }

    // Method to update status of a reminder record.
    // Primary key, i.e, TBR_ID of the reminder is used for upgrading the record.
    public int updateStatus(int reminderId, int status){
        // Get the database instance.
        SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();
        // ContentValues provide an empty set of name-value pair.
        ContentValues contentValues = new ContentValues();
        // status = 1 means that the reminder has been triggered.
        contentValues.put(COL_12_TBR, status);

        // Update the specified table in the database and
        // return the result of the operation.
        return sqLiteDatabase.update(TABLE_TBR, contentValues, "TBR_ID = ?",
                new String[] {String.valueOf(reminderId)});
    }

    // Method to update TASK_ID of a reminder record.
    // Primary key, i.e, TBR_ID of the reminder is used for upgrading the record.
    /*public int updateTaskId(int reminderId, int taskId){
        // Get the database instance.
        SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();
        // ContentValues provide an empty set of name-value pair.
        ContentValues contentValues = new ContentValues();
        contentValues.put(COL_1_TBR, taskId);

        // Update the specified table in the database and
        // return the result of the operation.
        return sqLiteDatabase.update(TABLE_TBR, contentValues, "TBR_ID = ?",
                new String[] {String.valueOf(reminderId)});
    }*/

    // Delete a record from the specified table in the database.
    // Primary key, i.e, TBR_ID is used to delete the record
    public long deleteTask(int reminderId) {
        // Get the database instance.
        SQLiteDatabase db = this.getWritableDatabase();
        // Delete the record from the specified table in the database and
        // return the result of the operation.
        return db.delete(TABLE_TBR, "TBR_ID = ?", new String[]{String.valueOf(reminderId)});
    }

    /*public boolean updateTime(String entity ,String realtime) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COL_1_EBR, entity);
        contentValues.put(COL_2_EBR, realtime);
        db.update(TABLE_EBR, contentValues, "ENTITY = ?",new String[] { entity });
        return true;
    }*/

    public boolean updateLat(String entity, double latitude) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COL_1_EBR, entity);
        contentValues.put(COL_4_EBR, latitude);
        db.update(TABLE_EBR, contentValues, "ENTITY = ?",new String[] { entity });
        return true;
    }

    public boolean updateLng(String entity, double longitude) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COL_1_EBR, entity);
        contentValues.put(COL_5_EBR, longitude);
        db.update(TABLE_EBR, contentValues, "ENTITY = ?",new String[] { entity });
        return true;
    }

    public boolean updateName(String entity, String name) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COL_1_EBR, entity);
        contentValues.put(COL_3_EBR, name);
        db.update(TABLE_EBR, contentValues, "ENTITY = ?",new String[] { entity });
        return true;
    }

    public boolean makeEBRActive(String entity) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COL_6_EBR, 1);
        db.update(TABLE_EBR, contentValues, "ENTITY = ?",new String[] { entity });
        return true;
    }

    public boolean makeEBRInactive(String entity) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COL_6_EBR, 0);
        db.update(TABLE_EBR, contentValues, "ENTITY = ?",new String[] { entity });
        return true;
    }

    public Integer deleteData (String entity) {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete(TABLE_EBR, "ENTITY = ?",new String[] {entity});
    }
}