package app;

import java.text.DateFormat;
import java.util.Date;

import helper.SQLiteHandler;

/**
 * Created by Admin on 23-Mar-17.
 */
public class AppConfig {
    /**
     * Data base definition
     */

    //database info for starting a new race
    public static final String TAG = SQLiteHandler.class.getSimpleName();

    // Database Version
    public static final int DATABASE_VERSION = 1;

    // Database Name
    public static final String DATABASE_NAME = "sailance_database";

    /**
     * Tables to be used
     */
    // user Login table name
    public static final String TABLE_USER = "user";

    //club login table name
    public static final String TABLE_CLUB = "club";

    // user Login table name
    public static final String TABLE_USERS_FOR_SAILING = "users_for_sailing";

    public static final String TABLE_BOATS = "boats_available";

    //table for a race

    public static final String NEW_RACE_TABLE = "new_race";

    /**
     * Configuration variables for login and registration
     */
    //server club login url
    public static final String URL_CLUB_LOGIN = "http://192.168.43.125/sailance_database/login_club.php";

    //server club register url
    public static final String URL_CLUB_REGISTER = "http://192.168.43.125/sailance_database/register_club.php";


    //registering members to the club's account
    public static final String URL_REGISTER_MEMBER = "http://192.168.43.125/sailance_database/add_new_member.php";

    public static final String URL_REGISTER_ADMIN = "http://192.168.43.125/sailance_database/add_new_admin.php";

    //url for getting all members of a particular club
    public static final String URL_ALL_MEMBERS = "http://192.168.43.125/sailance_database/get_all_members.php?club_id=";
    public static final String URL_ALL_ADMINS = "http://192.168.43.125/sailance_database/get_all_admins.php?club_id=";

    /**
     * Configuration variables for starting new race
     */

    public static final String DATA_URL = "http://192.168.43.125/colleg_info/connect.php?club=";
    public static final String BOAT_TYPE_URL = "http://192.168.43.125/colleg_info/selectboat.php";


    public static final String KEY_TYPE = "type";
    public static final String JSON_ARRAY = "result";
    public static final String JSON_ARRAY_MEMBERS = "members";
    public static final String JSON_ARRAY_ADMINS = "admins";



    //record timer
    public static final String ELAPSEDTIME_TABLE_NAME = "elapsedtime";
    public static final String ELAPSEDTIME_COLUMN_PARTICIPANT = "participant";
    public static final String ELAPSEDTIME_COLUMN_TIMETAKEN = "timetaken";
    public static final String ELAPSEDTIME_COLUMN_ID = "id";

    // user Login users columns Table Columns names
    public static final String KEY_USER_ID = "id";
    public static final String KEY_USER_NAME = "name";
    public static final String KEY_USER_CLUB = "club";
    public static final String KEY_USER_USERNAME = "username";
    public static final String KEY_USER_EMAIL = "email";
    public static final String KEY_USER_UID = "uid";
    public static final String KEY_USER_CREATED_AT = "created_at";


    //club login column names
    public static final String KEY_CLUB_ID = "id";
    public static final String CLUB_ID = "club_id";
    public static final String KEY_CLUB_NAME = "club_name";
    public static final String KEY_CLUB_LOCATION = "club_location";
    public static final String KEY_CLUB_COUNTRY = "country";
    public static final String KEY_CLUB_HANDLE = "club_handle";
    public static final String KEY_CLUB_UID = "uid";
    public static final String KEY_CLUB_CREATED_AT = "created_at";


    // user for race Columns names
    public static final String KEY_ID = "id";
    public static final String KEY_NAME = "name";
    public static final String KEY_CLUB = "club";
    public static final String KEY_HANDICAP = "handicap";


    //columns for race table
    public static final String MEMBER_ID = "id";
    public static final String MEMBER_NAME= "name";
    public static final String SAIL_NUMBER= "sail_number";
    public static final String RIG= "rig"; //type of the boat
    public static final String PERSONAL_HANDICAP= "handicap";
    public static final String ADJUSTMENT_FACTOR= "Adj_Factor";

    //columns for available boats table
    public static final String BOAT_ID = "id";
    public static final String BOAT_TYPE= "type";
    public static final String BOAT_HANDICAP= "handicap";

    public static final String FIRST_NAME = "first_name";
    public static final String LAST_NAME = "last_name";
    public static final String PRIVILEGES = "privileges";
}
