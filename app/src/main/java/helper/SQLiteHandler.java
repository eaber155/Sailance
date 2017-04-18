package helper;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.sql.Struct;
import java.util.ArrayList;
import java.util.HashMap;

import static app.AppConfig.ADJUSTMENT_FACTOR;
import static app.AppConfig.BOAT_HANDICAP;
import static app.AppConfig.BOAT_ID;
import static app.AppConfig.BOAT_TYPE;
import static app.AppConfig.CLUB_ID;
import static app.AppConfig.DATABASE_NAME;
import static app.AppConfig.DATABASE_VERSION;
import static app.AppConfig.ELAPSEDTIME_COLUMN_ID;
import static app.AppConfig.ELAPSEDTIME_COLUMN_PARTICIPANT;
import static app.AppConfig.ELAPSEDTIME_COLUMN_TIMETAKEN;
import static app.AppConfig.ELAPSEDTIME_TABLE_NAME;
import static app.AppConfig.KEY_CLUB;
import static app.AppConfig.KEY_CLUB_COUNTRY;
import static app.AppConfig.KEY_CLUB_CREATED_AT;
import static app.AppConfig.KEY_CLUB_HANDLE;
import static app.AppConfig.KEY_CLUB_ID;
import static app.AppConfig.KEY_CLUB_LOCATION;
import static app.AppConfig.KEY_CLUB_NAME;
import static app.AppConfig.KEY_CLUB_UID;
import static app.AppConfig.KEY_HANDICAP;
import static app.AppConfig.KEY_NAME;
import static app.AppConfig.KEY_USER_CLUB;
import static app.AppConfig.KEY_USER_CREATED_AT;
import static app.AppConfig.KEY_USER_EMAIL;
import static app.AppConfig.KEY_USER_ID;
import static app.AppConfig.KEY_USER_NAME;
import static app.AppConfig.KEY_USER_UID;
import static app.AppConfig.KEY_USER_USERNAME;
import static app.AppConfig.MEMBER_ID;
import static app.AppConfig.MEMBER_NAME;
import static app.AppConfig.NEW_RACE_TABLE;
import static app.AppConfig.PERSONAL_HANDICAP;
import static app.AppConfig.RIG;
import static app.AppConfig.SAIL_NUMBER;
import static app.AppConfig.TABLE_BOATS;
import static app.AppConfig.TABLE_CLUB;
import static app.AppConfig.TABLE_USER;
import static app.AppConfig.TABLE_USERS_FOR_SAILING;

/**
 * Created by Admin on 23-Mar-17.
 */
public class SQLiteHandler extends SQLiteOpenHelper {
    private static final String TAG = SQLiteHandler.class.getSimpleName();

    public SQLiteHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    // Creating Tables
    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_CLUB_LOGIN_TABLE = "CREATE TABLE " + TABLE_CLUB + "("
                + KEY_CLUB_ID + " INTEGER PRIMARY KEY," + CLUB_ID + " TEXT," + KEY_CLUB_NAME + " TEXT,"
                + KEY_CLUB_LOCATION + " TEXT," + KEY_CLUB_COUNTRY + " TEXT," + KEY_CLUB_UID + " TEXT," + KEY_CLUB_HANDLE
                + " TEXT UNIQUE," + KEY_CLUB_CREATED_AT + " TEXT" + ")";

        //other tables to create
        String CREATE_CLUB_USER_TABLE = "CREATE TABLE " + TABLE_USERS_FOR_SAILING + "("
                + KEY_USER_ID + " INTEGER PRIMARY KEY," + KEY_NAME + " TEXT,"
                + KEY_CLUB + " TEXT," + KEY_HANDICAP + " FLOAT" + ")";

        String CREATE_RACE_TABLE = "CREATE TABLE " + NEW_RACE_TABLE + "("
                + MEMBER_ID + " INTEGER PRIMARY KEY," + MEMBER_NAME + " TEXT,"
                + SAIL_NUMBER + " VARCHAR UNIQUE," + RIG + " TEXT," + PERSONAL_HANDICAP+ " FLOAT," + ADJUSTMENT_FACTOR + " FLOAT" + ")";

        String CREATE_BOAT_TABLE = "CREATE TABLE " + TABLE_BOATS + "("
                + BOAT_ID + " INTEGER PRIMARY KEY," + BOAT_TYPE + " TEXT,"
                + BOAT_HANDICAP + " FLOAT" + ")";

        String SQL_CREATE_ENTRIES = "create table " + ELAPSEDTIME_TABLE_NAME + "("
                + ELAPSEDTIME_COLUMN_ID + " integer primary key, " +
                ELAPSEDTIME_COLUMN_PARTICIPANT +" text, "+ ELAPSEDTIME_COLUMN_TIMETAKEN + " text)";


        db.execSQL(CREATE_CLUB_LOGIN_TABLE);
        db.execSQL(CREATE_CLUB_USER_TABLE);
        db.execSQL(CREATE_RACE_TABLE);
        db.execSQL(CREATE_BOAT_TABLE);
        db.execSQL(SQL_CREATE_ENTRIES);

        Log.d(TAG, "Database tables created");
    }

    // Upgrading database
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop older table if existed
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USER);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_CLUB);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS_FOR_SAILING);
        db.execSQL("DROP TABLE IF EXISTS " + NEW_RACE_TABLE);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_BOATS);
        db.execSQL("DROP TABLE IF EXISTS " + ELAPSEDTIME_TABLE_NAME);

        // Create tables again
        onCreate(db);
    }


    /**
     * Storing club details in database
     * */
    public void addClub(String club_id, String club_name, String club_location, String country, String club_handle, String uid, String created_at) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(CLUB_ID, club_id);
        values.put(KEY_CLUB_NAME, club_name); // Name
        values.put(KEY_CLUB_LOCATION, club_location); // Email
        values.put(KEY_CLUB_COUNTRY, country);
        values.put(KEY_CLUB_HANDLE, club_handle);
        values.put(KEY_CLUB_UID, uid); // Email
        values.put(KEY_CLUB_CREATED_AT, created_at); // Created At

        // Inserting Row
        long id = db.insert(TABLE_CLUB, null, values);
        db.close(); // Closing database connection

        Log.d(TAG, "New club inserted into sqlite: " + id);
    }

    /**
     * Getting club data from database
     * */
    public HashMap<String, String> getClubDetails() {
        HashMap<String, String> club = new HashMap<String, String>();
        String selectQuery = "SELECT  * FROM " + TABLE_CLUB;

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        // Move to first row
        cursor.moveToFirst();
        if (cursor.getCount() > 0) {
            club.put("club_id", cursor.getString(1));
            club.put("club_name", cursor.getString(2));
            club.put("club_location", cursor.getString(3));
            club.put("club_country", cursor.getString(4));
            club.put("club_handle", cursor.getString(6));
            club.put("uid", cursor.getString(5));
            club.put("created_at", cursor.getString(7));
        }
        cursor.close();
        db.close();
        // return user
        Log.d(TAG, "Fetching user from Sqlite: " + club.toString());

        return club;
    }

    /**
     * Re crate database Delete all tables and create them again
     * */
    public void deleteClubs() {
        SQLiteDatabase db = this.getWritableDatabase();
        // Delete All Rows
        db.delete(TABLE_CLUB, null, null);
        db.close();

        Log.d(TAG, "Deleted all clubs info from sqlite");
    }

    /**
     * Database details for adding new sailors to the race
     */

    /**
     * Storing user details in database
     * */
    public void storeUserForRace(String name, String club, Float handicap) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_NAME, name);
        values.put(KEY_CLUB, club);
        values.put(KEY_HANDICAP, handicap);

        // Inserting Row
        long id = db.insert(TABLE_USERS_FOR_SAILING, null, values);
        db.close(); // Closing database connection

        Log.d(TAG, "New user for race: " + id);
    }

    //check if boat Number is already added to the race
    private boolean isBoatNumberIncluded(String sailNumber){
        String selectQuery = "SELECT  * FROM " + NEW_RACE_TABLE + " WHERE " + SAIL_NUMBER + " = '" + sailNumber+ "'" ;

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        // Move to first row
        cursor.moveToFirst();
        if (cursor.getCount() > 0) {
            cursor.close();
            return false;
        }else{
            return true;
        }
    }

    //check if sailor is already added to the race
    private boolean isSailorIncluded(String sailorName){
        String selectQuery = "SELECT  * FROM " + NEW_RACE_TABLE + " WHERE " + MEMBER_NAME + " = '" + sailorName+ "'" ;

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        // Move to first row
        cursor.moveToFirst();
        if (cursor.getCount() > 0) {
            cursor.close();
            return false;
        }else{
            return true;
        }
    }

    //recreate race table when a new race is started
    public  void recreateRaceTable(){
        SQLiteDatabase db = this.getWritableDatabase();
        String DROP_TABLE = "DROP TABLE IF EXISTS " + NEW_RACE_TABLE;

        String CREATE_RACE_TABLE = "CREATE TABLE " + NEW_RACE_TABLE + "("
                + MEMBER_ID + " INTEGER PRIMARY KEY," + MEMBER_NAME + " TEXT,"
                + SAIL_NUMBER + " VARCHAR UNIQUE," + RIG + " TEXT," + PERSONAL_HANDICAP+ " FLOAT," + ADJUSTMENT_FACTOR + " FLOAT" + ")";

        db.execSQL(DROP_TABLE);
        db.execSQL(CREATE_RACE_TABLE);

        Log.d(TAG, "Database recreated");
    }

    public boolean addSailorToRace(String name, String sailNumber, String rig, float handicap, float adjFactor) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        if(isBoatNumberIncluded(sailNumber) && isSailorIncluded(name)){
            values.put(MEMBER_NAME, name);
            values.put(SAIL_NUMBER, sailNumber);
            values.put(RIG, rig);
            values.put(PERSONAL_HANDICAP, handicap);
            values.put(ADJUSTMENT_FACTOR, adjFactor);
        }else {
            return false;
        }
        // Inserting Row
        long id = db.insert(NEW_RACE_TABLE, null, values);
        db.close(); // Closing database connection

        Log.d(TAG, "New race table added to sqlite: " + id);
        return true;
    }

    //delete and re-create table for boats
    public  void recreateBoatTable(){
        SQLiteDatabase db = this.getWritableDatabase();
        String DROP_TABLE = "DROP TABLE IF EXISTS " + TABLE_BOATS;

        String CREATE_BOAT_TABLE = "CREATE TABLE " + TABLE_BOATS + "("
                + BOAT_ID + " INTEGER PRIMARY KEY," + BOAT_TYPE + " TEXT,"
                + BOAT_HANDICAP + " FLOAT" + ")";

        db.execSQL(DROP_TABLE);
        db.execSQL(CREATE_BOAT_TABLE);

        Log.d(TAG, "Database recreated");
    }

    //delete and recreate table for users
    public  void recreateUserTable(){
        SQLiteDatabase db = this.getWritableDatabase();
        String DROP_TABLE = "DROP TABLE IF EXISTS " + TABLE_USERS_FOR_SAILING;

        String CREATE_CLUB_USER_TABLE = "CREATE TABLE " + TABLE_USERS_FOR_SAILING + "("
                + KEY_USER_ID + " INTEGER PRIMARY KEY," + KEY_NAME + " TEXT,"
                + KEY_CLUB + " TEXT," + KEY_HANDICAP + " FLOAT" + ")";

        db.execSQL(DROP_TABLE);
        db.execSQL(CREATE_CLUB_USER_TABLE);

        Log.d(TAG, "Database recreated");
    }

    public void storeBoat(String type, float handicap) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(BOAT_TYPE,type);
        values.put(BOAT_HANDICAP, handicap);

        // Inserting Row
        long id = db.insert(TABLE_BOATS, null, values);
        db.close(); // Closing database connection

        Log.d(TAG, "Boats added to sqlite: " + id);
    }

    /**
     * getting user handicap
     * */
    public HashMap<String, Float> getPersonalHandicap(String nameOfUser) {
        HashMap<String, Float> user = new HashMap<String, Float>();
        String selectQuery = "SELECT  * FROM " + TABLE_USERS_FOR_SAILING + " WHERE " + KEY_NAME + " = '" + nameOfUser+"'" ;

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        // Move to first row
        cursor.moveToFirst();
        if (cursor.getCount() > 0) {
            user.put("handicap", cursor.getFloat(3));
        }
        cursor.close();
        db.close();
        // return user
        Log.d(TAG, "Fetching user from Sqlite: " + user.toString());

        return user;
    }

    public HashMap<String, Float> getBoatHandicap(String boatType) {
        HashMap<String, Float> boat = new HashMap<String, Float>();
        String selectQuery = "SELECT  * FROM " + TABLE_BOATS + " WHERE " + BOAT_TYPE + " = '" + boatType+"'" ;

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        // Move to first row
        cursor.moveToFirst();
        if (cursor.getCount() > 0) {
            boat.put("boatHandicap", cursor.getFloat(2));
        }
        cursor.close();
        db.close();
        // return user
        Log.d(TAG, "Fetching boat from Sqlite: " + boat.toString());

        return boat;
    }


    //get Array of boat types
    public ArrayList<String> getBoatTypes(){
        ArrayList<String> boatType = new ArrayList<String>();
        String selectQuery = "SELECT * FROM "+TABLE_BOATS;

        try{
            SQLiteDatabase db = this.getReadableDatabase();
            Cursor cursor = db.rawQuery(selectQuery, null);

            //move cursor to first
            if(!cursor.moveToFirst())
                return boatType;
            do{
                String boatTypeAvailabe = cursor.getString(cursor.getColumnIndex(BOAT_TYPE));
                boatType.add(boatTypeAvailabe);
            }while (cursor.moveToNext());
            cursor.close();
            db.close();
            return boatType;
        }
        finally {
            //return
            Log.d(TAG, "Fetcing Boat Types: " + boatType.toString());

        }
    }

    //get sailors to add to the game
    public ArrayList<String> getSailorsRegistered(){
        ArrayList<String> sailorsRegistered = new ArrayList<String>();
        String selectQuery = "SELECT * FROM " +NEW_RACE_TABLE;

        try{
            SQLiteDatabase db = this.getReadableDatabase();
            Cursor cursor = db.rawQuery(selectQuery, null);

            //move cursor to first
            if(!cursor.moveToFirst())
                return sailorsRegistered;
            do{
                String sailorsAvailabe = cursor.getString(cursor.getColumnIndex(MEMBER_NAME));
                sailorsRegistered.add(sailorsAvailabe);
            }while (cursor.moveToNext());
            cursor.close();
            db.close();
            return sailorsRegistered;
        }
        finally {
            //return
            Log.d(TAG, "Fetcing Boat Types: " + sailorsRegistered.toString());
        }
    }

    //Time recorder database stuff
    public boolean recordElapsedTime(String participant, String timeTaken){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(ELAPSEDTIME_COLUMN_PARTICIPANT, participant);
        contentValues.put(ELAPSEDTIME_COLUMN_TIMETAKEN, timeTaken);
        db.insert(ELAPSEDTIME_TABLE_NAME, null, contentValues);
        return true;
    }

    public Cursor getTimeElapsed(int id){
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("select * from "+ ELAPSEDTIME_TABLE_NAME + " where id="+id, null);
    }

    public ArrayList<String> getAllTimes(){
        ArrayList<String> arrayList = new ArrayList<String>();

        //hp = new HashMap();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res =  db.rawQuery( "select * from "+ELAPSEDTIME_TABLE_NAME, null );
        res.moveToFirst();

        while(!res.isAfterLast()){ //res.isAfterLast() == false
            arrayList.add(res.getString(res.getColumnIndex(ELAPSEDTIME_COLUMN_TIMETAKEN)));
            res.moveToNext();
        }
        return arrayList;
    }
}
