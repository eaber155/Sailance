package fragment;

import android.graphics.Point;
import android.graphics.Rect;
import android.support.v4.app.DialogFragment;
import android.os.Bundle;
import android.view.Display;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.evivian.admin.sailance.R;

import helper.SQLiteHandler;

public class AddGuestSailorFragment extends DialogFragment implements TextView.OnEditorActionListener {
    private EditText mEditTextName, mEditTextSailNumber, mEditTextPersonalHandicap;
    private Spinner mSpinnerBoatType;
    Button mAddGuestSailor;
    private ArrayList<String> boatTypes;
    SQLiteHandler db;
    TextView getGuestAdjFactor;


    public interface GuestSailorListener {
        void onFinishUserDialog(String user);
    }

    // Empty constructor required for DialogFragment
    public AddGuestSailorFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_add_guest_sailor, container);


        mEditTextName = (EditText) view.findViewById(R.id.guestSailorName);
        mEditTextSailNumber = (EditText)view.findViewById(R.id.guestSailorBoatID);
        mEditTextPersonalHandicap = (EditText)view.findViewById(R.id.guestPersonalHandicap);
        mSpinnerBoatType = (Spinner)view.findViewById(R.id.spinnerBoatType);
        mAddGuestSailor = (Button)view.findViewById(R.id.addGuestSailor);
        getGuestAdjFactor = (TextView)view.findViewById(R.id.guestAdjFactor);


        // set this instance as callback for editor action
        mEditTextName.setOnEditorActionListener(this);
        mEditTextName.requestFocus();

        boatTypes = new ArrayList<String>();

        //db
        db = new SQLiteHandler(getActivity());

        boatTypes = db.getBoatTypes();

        //spinner.setAdapter(new ArrayAdapter<String>(MainActivity.this, android.R.layout.simple_spinner_dropdown_item, members));

        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item, boatTypes);

        //dropdown layout style-list view with radio button
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mSpinnerBoatType.setPrompt("Select member to add!");

        mSpinnerBoatType.setAdapter(dataAdapter);

        getDialog().getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        getDialog().setTitle("Enter Details of Guest Sailor");

        //how spinner will work
        mSpinnerBoatType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                //on selecting a spinner item
                String item = parent.getItemAtPosition(position).toString();

                HashMap<String, Float> getBoatHandicap = db.getBoatHandicap(item);

                Float handicap = getBoatHandicap.get("boatHandicap");
                getGuestAdjFactor.setText(String.valueOf(handicap));

                //showing selected member
                //Toast.makeText(parent.getContext(), "Selected: "+item, Toast.LENGTH_LONG).show();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // TODO Auto-generated method stub
            }
        });

        mAddGuestSailor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String nameOfSailor = mEditTextName.getText().toString();
                String getSailNumber = mEditTextSailNumber.getText().toString();
                String boatType = mSpinnerBoatType.getSelectedItem().toString();
                Float getPersonalHandicap = Float.parseFloat(mEditTextPersonalHandicap.getText().toString());
                Float adjFactor = Float.parseFloat(getGuestAdjFactor.getText().toString());

                db.addSailorToRace(nameOfSailor, getSailNumber, boatType, getPersonalHandicap, adjFactor);
                //db.storeBoat(nameOfSailor, adjFactor);
                Toast.makeText(getActivity(), "Sailor added to race", Toast.LENGTH_SHORT).show();

                dismiss();
            }
        });

        return view;
    }

    @Override
    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        // Return input text to activity
        GuestSailorListener activity = (GuestSailorListener) getActivity();
        activity.onFinishUserDialog(mEditTextName.getText().toString());
        this.dismiss();
        return true;
    }

    @Override
    public void onStart() {
        super.onStart();

        // safety check
        if (getDialog() == null) {
            return;
        }

        Window window = getDialog().getWindow();
        Point size = new Point();

        Display display = window.getWindowManager().getDefaultDisplay();
        display.getSize(size);

        int width = size.x;
        int height = size.y;

        window.setLayout((int) (width * 0.75), (int)(height*0.75));
        window.setGravity(Gravity.CENTER);

        // ... other stuff you want to do in your onStart() method
    }

    public void onResume() {
        super.onResume();

        Window window = getDialog().getWindow();
        Point size = new Point();

        Display display = window.getWindowManager().getDefaultDisplay();
        display.getSize(size);

        int width = size.x;
        int height = size.y;

        window.setLayout((int) (width * 0.75), (int)(height*0.9));
        window.setGravity(Gravity.CENTER);

    }

    public void getBoatTypesFromDatabase(){
        //ArrayList<String> typesAvailable = new ArrayList<String>();

    }
}

