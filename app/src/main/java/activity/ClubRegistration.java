package activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
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

public class ClubRegistration extends AppCompatActivity {
    private static final String TAG = ClubRegistration.class.getSimpleName();
    EditText clubName, clubLocation, getCountry, clubHandle, passCode;
    Button submitButton, openSailingClubAccount;
    private ProgressDialog pDialog;
    private SessionManager sessionManager;
    private SQLiteHandler db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_club_registration);

        clubName = (EditText)findViewById(R.id.fullClubNameText);
        clubLocation = (EditText)findViewById(R.id.clubLocationText);
        getCountry = (EditText)findViewById(R.id.countryText);
        clubHandle = (EditText)findViewById(R.id.clubHandleText);
        passCode = (EditText)findViewById(R.id.passcodeText);

        //buttons
        submitButton = (Button)findViewById(R.id.submitClubInfoButton);
        openSailingClubAccount = (Button)findViewById(R.id.openClubAccount);

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
            Intent intent = new Intent(ClubRegistration.this, ClubAccountHome.class);
            startActivity(intent);
            finish();
        }

        //Login button click event
        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String club_name = clubName.getText().toString().trim();
                String club_location = clubLocation.getText().toString().trim();
                String country = getCountry.getText().toString().trim();
                String club_handle = clubHandle.getText().toString().trim();
                String passcode = passCode.getText().toString().trim();

                //check for empty data in the form
                if (!club_name.isEmpty() && !club_location.isEmpty() && !country.isEmpty()
                        && !club_handle.isEmpty() && !passcode.isEmpty()) {
                    //login club
                    registerClub(club_name, club_location, country, club_handle, passcode);
                } else {
                    //prompt user to enter credentials
                    Toast.makeText(getApplicationContext(), "Please enter necessary credentials",
                            Toast.LENGTH_LONG).show();
                }
            }
        });

        //link to register a sailing club
        openSailingClubAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), OpenClubAccount.class);
                startActivity(intent);
                finish();
            }
        });
    }

    //function to register club
    private void registerClub(final String club_name, final String club_location, final String country,
                              final String club_handle, final String passcode){
        //tag used to cancel the request
        String tag_string_req = "req_club_register";

        pDialog.setMessage("Registering ...");
        showDialog();

        StringRequest stringRequest = new StringRequest(Request.Method.POST,
                AppConfig.URL_CLUB_REGISTER, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.d(TAG, "Register Response: " + response);
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

                        Toast.makeText(getApplicationContext(), "Thank you for using Sailance!", Toast.LENGTH_LONG).show();

                        //launch home screen
                        Intent intent = new Intent(ClubRegistration.this, ClubAccountHome.class);
                        startActivity(intent);
                        finish();
                    } else {
                        //error in registration. get error message
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
                Log.e(TAG, "Registration Error: "+error.getMessage());
                Toast.makeText(getApplicationContext(), ""+error.getMessage(),
                        Toast.LENGTH_LONG).show();
                hideDialog();
            }
        }){
            @Override
            protected Map<String, String> getParams(){
                //posting parameters to registration url
                Map<String, String> params = new HashMap<>();
                params.put("club_name", club_name);
                params.put("club_location", club_location);
                params.put("country", country);
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
