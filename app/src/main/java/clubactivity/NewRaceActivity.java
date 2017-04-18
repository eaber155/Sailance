package clubactivity;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import android.content.Intent;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.app.ProgressDialog;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

import android.widget.AdapterView.OnItemSelectedListener;

import com.evivian.admin.sailance.R;
import timer.RecordRaceActivity;

import fragment.AddGuestSailorFragment;
import helper.SQLiteHandler;

import static app.AppConfig.BOAT_TYPE_URL;
import static app.AppConfig.DATA_URL;
import static app.AppConfig.JSON_ARRAY;
import static app.AppConfig.KEY_CLUB;
import static app.AppConfig.KEY_HANDICAP;
import static app.AppConfig.KEY_NAME;
import static app.AppConfig.KEY_TYPE;

public class NewRaceActivity extends AppCompatActivity implements  AddGuestSailorFragment.GuestSailorListener{
    private Spinner spinner, spinnerBoat;
    private ArrayList<String> members, boatTypes;
    private JSONArray result, resultBoat;
    SQLiteHandler db;


    private EditText sailNumber,personalHandicap;
    private Button addMemberToRace, startRace, addGuestSailor;
    private TextView getAdjFactor;

    private ProgressDialog loading;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_race);

        members = new ArrayList<String>();
        boatTypes = new ArrayList<String>();

        //sqlite database
        db = new SQLiteHandler(getApplicationContext());
        db.recreateRaceTable();

        //db.onUpgrade(openOrCreateDatabase(SQLiteHandler.DATABASE_NAME, MODE_PRIVATE, null), 1, 2);

        //spinner element
        spinner = (Spinner)findViewById(R.id.spinnerMembers);
        spinnerBoat = (Spinner)findViewById(R.id.spinnerBoatType);

        //editText
        personalHandicap = (EditText)findViewById(R.id.personalHandicap);
        sailNumber = (EditText)findViewById(R.id.boatID);

        //textview
        getAdjFactor = (TextView)findViewById(R.id.adjFactor);

        //buttons
        startRace = (Button)findViewById(R.id.startRace);
        addMemberToRace = (Button)findViewById(R.id.addToRace);
        addGuestSailor = (Button)findViewById(R.id.addGuestSailor);

        spinner.setOnItemSelectedListener(new OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                //on selecting a spinner item
                String item = parent.getItemAtPosition(position).toString();
                //showing selected member
                //Toast.makeText(parent.getContext(), "Selected: "+item, Toast.LENGTH_LONG).show();

                HashMap<String, Float> getPersonalHandicap = db.getPersonalHandicap(item);

                Float handicap = getPersonalHandicap.get("handicap");
                personalHandicap.setText(String.valueOf(handicap));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // TODO Auto-generated method stub
            }
        });

        spinnerBoat.setOnItemSelectedListener(new OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                //on selecting a spinner item
                String item = parent.getItemAtPosition(position).toString();

                HashMap<String, Float> getBoatHandicap = db.getBoatHandicap(item);

                Float handicap = getBoatHandicap.get("boatHandicap");
                getAdjFactor.setText(String.valueOf(handicap));

                //showing selected member
                //Toast.makeText(parent.getContext(), "Selected: "+item, Toast.LENGTH_LONG).show();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // TODO Auto-generated method stub
            }
        });

        //spinner click listener
        getMembers();

        getBoatType();

        addMemberToRace.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String nameOfSailor = spinner.getSelectedItem().toString();
                String getSailNumber = sailNumber.getText().toString();
                String boatType = spinnerBoat.getSelectedItem().toString();
                Float getPersonalHandicap = Float.parseFloat(personalHandicap.getText().toString());
                Float adjFactor = Float.parseFloat(getAdjFactor.getText().toString());

                if(db.addSailorToRace(nameOfSailor, getSailNumber, boatType, getPersonalHandicap, adjFactor)){
                    Toast.makeText(getApplicationContext(), "Sailor Added to Race. Add another sailor ", Toast.LENGTH_SHORT).show();
                }else {
                    Toast.makeText(getApplicationContext(), "Sail Number or Sailor already added to Race", Toast.LENGTH_SHORT).show();
                }
                //recreate();
                sailNumber.setText("");
            }
        });

        //adding a guest sailor
        addGuestSailor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // close existing dialog fragments
                FragmentManager manager = getSupportFragmentManager();
                Fragment fragment = manager.findFragmentByTag("fragment_guest_sailors");
                if (fragment != null) {
                    manager.beginTransaction().remove(fragment).commit();
                    //manager.beginTransaction().remove(frag).commit();
                }
                AddGuestSailorFragment addGuestSailorDialog = new AddGuestSailorFragment();
                addGuestSailorDialog.show(manager, "fragment_guest_sailors");
            }
        });

        startRace.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(NewRaceActivity.this, RecordRaceActivity.class);
                startActivity(intent);
            }
        });
    }

    @Override
    public void onFinishUserDialog(String user) {
        Toast.makeText(this, "Hello, " + user, Toast.LENGTH_SHORT).show();
    }

    private void getMembers() {
        //loading = ProgressDialog.show(this,"Please wait...","Fetching...",false,false);

        String url = DATA_URL+ "vic"; //editTextId.getText().toString().trim();

        StringRequest stringRequest = new StringRequest(url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                //loading.dismiss();
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    result = jsonObject.getJSONArray(JSON_ARRAY);
                    getDataMembers(result);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(NewRaceActivity.this,""+error.getMessage(),Toast.LENGTH_LONG).show();
                    }
                });

        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);
    }

    private void getDataMembers(JSONArray j){
        db.recreateUserTable();
        for (int i=0; i<j.length(); i++){
            try{
                JSONObject json = j.getJSONObject(i);
                String name = json.getString(KEY_NAME);
                String club = json.getString(KEY_CLUB);
                Float handicap = Float.parseFloat(json.getString(KEY_HANDICAP));
                members.add(name);
                db.storeUserForRace(name, club, handicap);
            }catch (JSONException e){
                e.printStackTrace();
            }
        }

        //spinner.setAdapter(new ArrayAdapter<String>(MainActivity.this, android.R.layout.simple_spinner_dropdown_item, members));

        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, members);

        //dropdown layout style-list view with radio button
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setPrompt("Select member to add!");

        spinner.setAdapter(dataAdapter);
    }

    public void getBoatType(){
        //loading = ProgressDialog.show(this,"Please wait...","Fetching...",false,false);

        StringRequest stringRequest = new StringRequest(BOAT_TYPE_URL, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                //loading.dismiss();
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    resultBoat = jsonObject.getJSONArray(JSON_ARRAY);
                    getDataBoatType(resultBoat);
                } catch (JSONException e) {
                    e.printStackTrace(); //N5397
                }
            }
        },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(NewRaceActivity.this,""+error.getMessage(),Toast.LENGTH_LONG).show();
                    }
                });

        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);
    }

    private void getDataBoatType(JSONArray j){
        db.recreateBoatTable();
        for (int i=0; i<j.length(); i++){
            try{
                JSONObject json = j.getJSONObject(i);
                String type = json.getString(KEY_TYPE);
                Float boatHandicap = Float.parseFloat(json.getString(KEY_HANDICAP));
                boatTypes.add(type);
                db.storeBoat(type, boatHandicap);
            }catch (JSONException e){
                e.printStackTrace();
            }
        }

        //spinner.setAdapter(new ArrayAdapter<String>(MainActivity.this, android.R.layout.simple_spinner_dropdown_item, members));

        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, boatTypes);

        //dropdown layout style-list view with radio button
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerBoat.setPrompt("Select member to add!");

        spinnerBoat.setAdapter(dataAdapter);
    }
}
