package fragment;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.os.Process;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.text.InputType;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
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
import com.evivian.admin.sailance.ClubAccountHome;
import com.evivian.admin.sailance.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import activity.ClubRegistration;
import activity.OpenClubAccount;
import app.AppConfig;
import app.AppController;
import clubactivity.NewRaceActivity;
import helper.SQLiteHandler;
import helper.SessionManager;

import static app.AppConfig.DATA_URL;
import static app.AppConfig.ELAPSEDTIME_COLUMN_ID;
import static app.AppConfig.FIRST_NAME;
import static app.AppConfig.JSON_ARRAY;
import static app.AppConfig.JSON_ARRAY_MEMBERS;
import static app.AppConfig.KEY_CLUB;
import static app.AppConfig.KEY_HANDICAP;
import static app.AppConfig.KEY_NAME;
import static app.AppConfig.LAST_NAME;
import static app.AppConfig.URL_ALL_MEMBERS;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link AddNewMembersFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link AddNewMembersFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class AddNewMembersFragment extends Fragment implements View.OnClickListener{
    private static final String TAG = AddNewMembersFragment.class.getSimpleName();
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    /**
     * instantiating view variable
     */
    EditText getFirstName, getLastName, getDateOfBirth, getEmail;
    Spinner sexSpinner;
    ArrayList<String> sexArray, membersRegisteredToClub;
    Button registerMemberButton;
    private OnFragmentInteractionListener mListener;
    SQLiteHandler db;
    private SimpleDateFormat dateFormatter;
    //private Calendar calendar;
    //private int year, month, day;
    private DatePickerDialog datePickerDialog;
    ProgressDialog progressDialog;
    SessionManager sessionManager;
    ListView clubMembersList;
    TextView membersRegistered, getAddNewMembersPrompt;
    private JSONArray result;

    int ageOfMember;
    String ageCategoryOfMember, ageForDatabase, club_id, country;

    public AddNewMembersFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment AddNewMembersFragment.
     */

    // TODO: Rename and change types and number of parameters
    public static AddNewMembersFragment newInstance(String param1, String param2) {
        AddNewMembersFragment fragment = new AddNewMembersFragment();
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
        View view = inflater.inflate(R.layout.fragment_add_new_members, container, false);

        getFirstName = (EditText)view.findViewById(R.id.firstNameOfMember);
        getLastName = (EditText)view.findViewById(R.id.lastNameOfMember);
        getDateOfBirth = (EditText)view.findViewById(R.id.dateOfBirth);
        getEmail = (EditText)view.findViewById(R.id.emailOfMember);
        sexSpinner = (Spinner) view.findViewById(R.id.sexSpinner);


        membersRegistered = (TextView)view.findViewById(R.id.textViewAddNewMembers);
        getAddNewMembersPrompt = (TextView)view.findViewById(R.id.addNewMembersPrompt);

        dateFormatter = new SimpleDateFormat("dd/MM/yyyy", Locale.US);

        db = new SQLiteHandler(getActivity());

        progressDialog = new ProgressDialog(getActivity());
        progressDialog.setCancelable(false);

        //pick club id and country from the club from the club sqlite database created
        HashMap<String, String> clubInformation = db.getClubDetails();
        club_id = clubInformation.get("club_id");
        country = clubInformation.get("club_country");

        setDateTimeField();

        sexArray = new ArrayList<String>();
        sexArray.add(getString(R.string.male));
        sexArray.add(getString(R.string.female));
        db = new SQLiteHandler(getContext());
        sessionManager = new SessionManager(getActivity());

        membersRegisteredToClub = new ArrayList<String>();

        clubMembersList = (ListView)view.findViewById(R.id.list_view_members);

        getAllMembers();

        registerMemberButton = (Button)view.findViewById(R.id.addNewMemberButton);

        //spinner.setAdapter(new ArrayAdapter<String>(MainActivity.this, android.R.layout.simple_spinner_dropdown_item, members));

        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item, sexArray);

        //dropdown layout style-list view with radio button
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        sexSpinner.setAdapter(dataAdapter);

        //how spinner will work
        sexSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
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

        registerMemberButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String firstName = getFirstName.getText().toString().trim();
                String lastName = getLastName.getText().toString().trim();
                String sex = sexSpinner.getSelectedItem().toString().trim();
                String dateOfBirth = getDateOfBirth.getText().toString().trim();
                String email = getEmail.getText().toString().trim();
                //check if values are empty
                if(!firstName.isEmpty()&& !lastName.isEmpty()&& !sex.isEmpty() && !dateOfBirth.isEmpty()&& !email.isEmpty()){
                    storeMembersToClub(firstName, lastName, sex, dateOfBirth, email);

                    getFirstName.setText(" ");
                    getLastName.setText(" ");
                    getEmail.setText(" ");
                    sexSpinner.setSelection(0);
                    getDateOfBirth.setText(" ");
                    getDateOfBirth.setHint(R.string.date_hint);
                }else {
                    //prompt user to enter credentials
                    Toast.makeText(getActivity(), "Please fill in all the fields", Toast.LENGTH_LONG).show();
                }
            }
        });
        return view;
    }

    private void getAllMembers() {
        //loading = ProgressDialog.show(this,"Please wait...","Fetching...",false,false);

        String url = URL_ALL_MEMBERS + club_id; //editTextId.getText().toString().trim();

        StringRequest stringRequest = new StringRequest(url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                //loading.dismiss();
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    result = jsonObject.getJSONArray(JSON_ARRAY_MEMBERS);
                    getDataMembers(result);
                    areMembersRegistered(result);
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
                String fullName = first_name+ " " + last_name;
                membersRegisteredToClub.add(fullName);
            }catch (JSONException e){
                e.printStackTrace();
            }
        }

        //spinner.setAdapter(new ArrayAdapter<String>(MainActivity.this, android.R.layout.simple_spinner_dropdown_item, members));

        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_1, membersRegisteredToClub);

        clubMembersList.setAdapter(dataAdapter);
    }

    public void areMembersRegistered(JSONArray x){
        if(x==null){
            membersRegistered.setText(R.string.no_members);
            getAddNewMembersPrompt.setText(R.string.add_new_members_empty);

        }else {
            membersRegistered.setText(R.string.member_registered);
            getAddNewMembersPrompt.setText(R.string.add_new_members);
        }
    }

    private void setDateTimeField() {
        getDateOfBirth.setOnClickListener(this);
        getDateOfBirth.setInputType(InputType.TYPE_NULL);

        Calendar newCalendar = Calendar.getInstance();

        datePickerDialog = new DatePickerDialog(getContext(), new DatePickerDialog.OnDateSetListener() {

            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                Calendar newDate = Calendar.getInstance();
                newDate.set(year, monthOfYear, dayOfMonth);
                getDateOfBirth.setText(dateFormatter.format(newDate.getTime()));

                StringBuilder stringBuilder = new StringBuilder();
                ageOfMember = CalculateDate(newDate);
                ageCategoryOfMember = AgeCategory(ageOfMember);
                ageForDatabase = (stringBuilder.append(ageOfMember).toString());
            }

        },newCalendar.get(Calendar.YEAR), newCalendar.get(Calendar.MONTH), newCalendar.get(Calendar.DAY_OF_MONTH));
    }

    @Override
    public void onClick(View view) {
        if(view == getDateOfBirth) {
            datePickerDialog.show();
        } else {
            datePickerDialog.hide();
        }
    }

    //for calculating the age
    private int CalculateDate(Calendar dateOfBirth){
        Calendar now = Calendar.getInstance();

        if(dateOfBirth.after(now)){
            Toast.makeText(getActivity(), "No one can be born in the future", Toast.LENGTH_LONG).show();
            //throw new IllegalArgumentException("Can't be born in the future");
        }
        int yearNow = now.get(Calendar.YEAR);
        int yearOfBirth = dateOfBirth.get(Calendar.YEAR);
        int age = yearNow-yearOfBirth;

        int monthNow = now.get(Calendar.MONTH);
        int monthOfBirth = dateOfBirth.get(Calendar.MONTH);
        if(monthOfBirth > monthNow){
            age--;
        }else if(monthNow==monthOfBirth){
            int dayNow = now.get(Calendar.DAY_OF_MONTH);
            int dayOfBirth = dateOfBirth.get(Calendar.DAY_OF_MONTH);
            if(dayOfBirth>dayNow){
                age--;
            }
        }
        return age;
    }

    //calculating the age category
    private String AgeCategory(int age){
        String ageCategory = "age_category";
        if(age<=38){
            ageCategory = "Y";
        }else if(age>=39 && age<=54){
            ageCategory="A";
        }else if(age>=55){
            ageCategory="S";
        }else{
            Toast.makeText(getActivity(), "Age not in eligible category", Toast.LENGTH_SHORT).show();
        }
        return ageCategory;
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

    private void storeMembersToClub(final String firstName, final String lastName, final String sex, final String dateOfBirth, final String email){
        //tag to cancel request
        String tag_string_req = "req_add_new_member";

        progressDialog.setMessage("Adding New Member ...");
        showDialog();

        StringRequest stringRequest = new StringRequest(Request.Method.POST,
                AppConfig.URL_REGISTER_MEMBER, new Response.Listener<String>() {
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
                        Toast.makeText(getActivity(), "Member Added! Add Another Member", Toast.LENGTH_LONG).show();
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
                params.put("sex", sex);
                params.put("date_of_birth", dateOfBirth);
                params.put("age", ageForDatabase);
                params.put("age_category", ageCategoryOfMember);
                params.put("email", email);
                params.put("country", country);
                params.put("club_id", club_id);
                return params;
            }
        };

        //adding request to request queue
        AppController.getInstance().addToRequestQueue(stringRequest, tag_string_req);
    }
}
