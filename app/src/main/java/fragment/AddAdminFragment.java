package fragment;

import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.evivian.admin.sailance.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import app.AppConfig;
import app.AppController;
import helper.SQLiteHandler;
import helper.SessionManager;

import static app.AppConfig.FIRST_NAME;
import static app.AppConfig.JSON_ARRAY_ADMINS;
import static app.AppConfig.JSON_ARRAY_MEMBERS;
import static app.AppConfig.LAST_NAME;
import static app.AppConfig.PRIVILEGES;
import static app.AppConfig.URL_ALL_ADMINS;
import static app.AppConfig.URL_ALL_MEMBERS;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link AddAdminFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link AddAdminFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class AddAdminFragment extends Fragment {
    private static final String TAG =AddAdminFragment.class.getSimpleName();

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;

    EditText getFirstName, getLastName, getEmail, getDefaultPassword;
    Spinner getPrivilegesSpinner;

    ArrayList<String> privilegesArray,adminsAdded;
    Button registerAdminButton;
    SQLiteHandler db;
    private SimpleDateFormat dateFormatter;
    ProgressDialog progressDialog;
    ListView adminList;
    TextView adminRegistered, getAddAdminPrompt;
    private JSONArray result;

    String club_id;

    public AddAdminFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment AddAdminFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static AddAdminFragment newInstance(String param1, String param2) {
        AddAdminFragment fragment = new AddAdminFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_add_admin, container, false);

        getFirstName = (EditText)view.findViewById(R.id.firstNameOfAdmin);
        getLastName = (EditText)view.findViewById(R.id.lastNameOfAdmin);
        getEmail = (EditText)view.findViewById(R.id.emailOfAdmin);
        getDefaultPassword = (EditText)view.findViewById(R.id.adminPassword);
        getPrivilegesSpinner = (Spinner) view.findViewById(R.id.privilegesSpinner);


        adminRegistered = (TextView)view.findViewById(R.id.textViewAdmins);
        getAddAdminPrompt = (TextView)view.findViewById(R.id.addAdminPrompt);

        db = new SQLiteHandler(getActivity());

        progressDialog = new ProgressDialog(getActivity());
        progressDialog.setCancelable(false);

        //pick club id and country from the club from the club sqlite database created
        HashMap<String, String> clubInformation = db.getClubDetails();
        club_id = clubInformation.get("club_id");

        privilegesArray = new ArrayList<String>();
        privilegesArray.add(getString(R.string.manage_members));
        privilegesArray.add(getString(R.string.race_and_handicap));
        privilegesArray.add(getString(R.string.all_privileges));

        adminsAdded = new ArrayList<String>();

        adminList = (ListView)view.findViewById(R.id.list_view_admins);

        getAllAdmins();

        registerAdminButton = (Button)view.findViewById(R.id.addAdminButton);

        //spinner.setAdapter(new ArrayAdapter<String>(MainActivity.this, android.R.layout.simple_spinner_dropdown_item, members));

        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item, privilegesArray);

        //dropdown layout style-list view with radio button
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        getPrivilegesSpinner.setAdapter(dataAdapter);

        //how spinner will work
        getPrivilegesSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                //on selecting a spinner item
                String item = parent.getItemAtPosition(position).toString();

                //Toast.makeText(parent.getContext(), "Selected: "+item, Toast.LENGTH_LONG).show();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // TODO Auto-generated method stub
            }
        });

        registerAdminButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String firstName = getFirstName.getText().toString().trim();
                String lastName = getLastName.getText().toString().trim();
                String privileges = getPrivilegesSpinner.getSelectedItem().toString().trim();
                String email = getEmail.getText().toString().trim();
                String default_password = getDefaultPassword.getText().toString().trim();

                //check if values are empty
                if(!firstName.isEmpty()&& !lastName.isEmpty()&& !privileges.isEmpty() && !email.isEmpty()&& !default_password.isEmpty()){
                    storeAdminToClub(firstName, lastName, privileges, email, default_password);

                    getFirstName.setText(" ");
                    getLastName.setText(" ");
                    getEmail.setText(" ");
                    getPrivilegesSpinner.setSelection(0);
                    getEmail.setText(" ");
                    getDefaultPassword.setText(" ");
                }else {
                    //prompt user to enter credentials
                    Toast.makeText(getActivity(), "Please fill in all the fields", Toast.LENGTH_LONG).show();
                }
            }
        });
        return view;
    }

    private void getAllAdmins() {
        //loading = ProgressDialog.show(this,"Please wait...","Fetching...",false,false);

        String url = URL_ALL_ADMINS + club_id; //editTextId.getText().toString().trim();

        StringRequest stringRequest = new StringRequest(url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                //loading.dismiss();
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    result = jsonObject.getJSONArray(JSON_ARRAY_ADMINS);
                    getDataMembers(result);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(getActivity(),""+error.getMessage(),Toast.LENGTH_LONG).show();
                    }
                });

        RequestQueue requestQueue = Volley.newRequestQueue(getActivity());
        requestQueue.add(stringRequest);
    }

    private void getDataMembers(JSONArray j){
        for (int i=0; i<j.length(); i++){
            try{
                JSONObject json = j.getJSONObject(i);
                String first_name = json.getString(FIRST_NAME);
                String last_name = json.getString(LAST_NAME);
                String privileges = json.getString(PRIVILEGES);
                String adminDetail = first_name+ " " + last_name + " ->  " + privileges;
                adminsAdded.add(adminDetail);
            }catch (JSONException e){
                e.printStackTrace();
            }
        }

        //spinner.setAdapter(new ArrayAdapter<String>(MainActivity.this, android.R.layout.simple_spinner_dropdown_item, members));

        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_1, adminsAdded);

        adminList.setAdapter(dataAdapter);
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }

    private void showDialog(){
        if(!progressDialog.isShowing())
            progressDialog.show();
    }

    private void hideDialog(){
        if(progressDialog.isShowing()){
            progressDialog.dismiss();
        }
    }

    private void storeAdminToClub(final String firstName, final String lastName, final String privileges, final String email, final String password){
        //tag to cancel request
        String tag_string_req = "req_add_new_member";

        progressDialog.setMessage("Adding New Member ...");
        showDialog();

        StringRequest stringRequest = new StringRequest(Request.Method.POST,
                AppConfig.URL_REGISTER_ADMIN, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.d(TAG, "Register Response: " + response);
                hideDialog();

                try {
                    JSONObject jsonObject = new JSONObject(response);
                    boolean error = jsonObject.getBoolean("error");

                    //check for error node in json
                    if (!error) {
                        //Tell club member that club has been registered
                        Toast.makeText(getActivity(), "Admin Added! Add Another Admin", Toast.LENGTH_LONG).show();
                        //isResumed();
                    } else {
                        //error in registration. get error message
                        String errorMessage = jsonObject.getString("error_msg");
                        Toast.makeText(getActivity(), "" + errorMessage, Toast.LENGTH_LONG).show();
                    }
                } catch (JSONException e) {
                    //JSON error
                    e.printStackTrace();
                    Toast.makeText(getActivity(), "JSON error: " +
                            e.getMessage(), Toast.LENGTH_LONG).show();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, "Registration Error: "+error.getMessage());
                Toast.makeText(getActivity(), ""+error.getMessage(),
                        Toast.LENGTH_LONG).show();
                hideDialog();
            }
        }){
            @Override
            protected Map<String, String> getParams(){
                //posting parameters to registration url

                Map<String, String> params = new HashMap<>();
                params.put("first_name", firstName);
                params.put("last_name",lastName);
                params.put("privileges", privileges);
                params.put("email", email);
                params.put("password", password);
                params.put("club_id", club_id);
                return params;
            }
        };

        //adding request to request queue
        AppController.getInstance().addToRequestQueue(stringRequest, tag_string_req);
    }
}
