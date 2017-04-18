package activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.evivian.admin.sailance.ClubAccountHome;
import com.evivian.admin.sailance.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import app.AppConfig;
import app.AppController;
import helper.SQLiteHandler;
import helper.SessionManager;

public class OpenClubAccount extends AppCompatActivity {
    private static final String TAG = OpenClubAccount.class.getSimpleName();
    EditText getClubHandle, getPassCode;
    Button loginButton, registerClub;
    TextView inCorrectCredentials,warning;
    LinearLayout incorrectCredentialsContainer;
    private ProgressDialog pDialog;
    private SessionManager sessionManager;
    private SQLiteHandler db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_open_club_account);

        getClubHandle = (EditText)findViewById(R.id.clubHandle);
        getPassCode = (EditText)findViewById(R.id.clubPasscode);

        //buttons
        loginButton = (Button)findViewById(R.id.club_login_button);
        registerClub = (Button)findViewById(R.id.registerSailingClub);

        //Progress dialog
        pDialog = new ProgressDialog(this);
        pDialog.setCancelable(false);

        //Session Manager
        sessionManager = new SessionManager(getApplicationContext());

        //SQLite database handler
        db = new SQLiteHandler(getApplicationContext());


        //check if user is already logged in or not
        if(sessionManager.isLoggedIn()){
            //user is already logged in. Take to club account
            Intent intent = new Intent(OpenClubAccount.this, ClubAccountHome.class);
            startActivity(intent);
            finish();
        }

        //Login button click event
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String club_handle = getClubHandle.getText().toString().trim();
                String passcode = getPassCode.getText().toString().trim();

                //check for empty data in the form
                if(!club_handle.isEmpty() && !passcode.isEmpty()){
                    //login club
                    checkClubLogin(club_handle, passcode);
                }else{
                    //prompt user to enter credentials
                    Toast.makeText(getApplicationContext(), "Please enter necessary credentials", Toast.LENGTH_LONG).show();
                }
            }
        });

        //link to register a sailing club
        registerClub.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), ClubRegistration.class);
                startActivity(intent);
                finish();
            }
        });
    }

    //function to verify club login
    private void checkClubLogin(final String club_handle, final String passcode){
        //tag used to cancel the request
        String tag_string_req = "req_club_login";

        pDialog.setMessage("Logging in ...");
        showDialog();

        StringRequest stringRequest = new StringRequest(Request.Method.POST,
                AppConfig.URL_CLUB_LOGIN, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.d(TAG, "Login Response: " + response);
                hideDialog();

                try {
                    JSONObject jsonObject = new JSONObject(response);
                    boolean error = jsonObject.getBoolean("error");

                    //check for error node in json
                    if (!error) {
                        //club successfully logged in
                        //create club session
                        sessionManager.setLogin(true);

                        //Now store the club in sqlite
                        String uid = jsonObject.getString("uid");

                        JSONObject club = jsonObject.getJSONObject("club");
                        String club_id = club.getString("club_id");
                        String club_name = club.getString("club_name");
                        String location = club.getString("club_location");
                        String country = club.getString("country");
                        String club_handle = club.getString("club_handle");
                        String create_at = club.getString("created_at");

                        //inserting row in club table
                        db.addClub(club_id, club_name, location, country, club_handle, uid, create_at);

                        //launch home screen
                        Intent intent = new Intent(OpenClubAccount.this,
                                ClubAccountHome.class);
                        startActivity(intent);
                        finish();
                    } else {
                        //error in login. get error message
                        String errorMessage = jsonObject.getString("error_msg");
                        Toast.makeText(getApplicationContext(), "" + errorMessage, Toast.LENGTH_LONG).show();
                    }
                } catch (JSONException e) {
                    //JSON error
                    e.printStackTrace();
                    Toast.makeText(getApplicationContext(), "JSON error: " +
                            e.getMessage(), Toast.LENGTH_LONG).show();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, "Login Error: "+error.getMessage());
                Toast.makeText(getApplicationContext(), ""+error.getMessage(),
                        Toast.LENGTH_LONG).show();
                hideDialog();
            }
        }){
            @Override
            protected Map<String, String> getParams(){
                //posting parameters to login url
                Map<String, String> params = new HashMap<>();
                params.put("club_handle", club_handle);
                params.put("passcode", passcode);
                return params;
            }
        };

        //adding request to request queue
        AppController.getInstance().addToRequestQueue(stringRequest, tag_string_req);
    }

    private void showDialog(){
        if(!pDialog.isShowing())
            pDialog.show();
    }

    private void hideDialog(){
        if(pDialog.isShowing()){
            pDialog.dismiss();
        }
    }
}

