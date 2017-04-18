package timer;

import android.content.Intent;
import android.os.Handler;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Chronometer;
import android.widget.GridView;
import android.widget.TextView;

import com.evivian.admin.sailance.R;


import java.util.ArrayList;

import helper.SQLiteHandler;

public class RecordRaceActivity extends AppCompatActivity {

    GridView gridView;
    ArrayList<Sailors> gridArray = new ArrayList<>();
    RecordRaceAdapter recordRaceAdapter;

    ArrayList<String> registeredSailorsList;

    //declare time variables
    long timeInMilliseconds = 0L;
    long updatedTime = 0L;
    long timeSwapBuff = 0L;
    String time;
    private long startTime = 0L;

    private Handler customHandler = new Handler();
    TextView textView;

    SQLiteHandler db;

    Runnable updateTimerThread = new Runnable() {
        @Override
        public void run() {
            timeInMilliseconds = SystemClock.elapsedRealtime() - startTime;
            updatedTime = timeSwapBuff + timeInMilliseconds;

            int secs = (int)(updatedTime/1000);
            int mins = secs/60;
            secs = secs%60;
            int milliseconds = (int)(updatedTime%1000);
            time = "" + String.format("%02d", mins) + ":" + String.format("%02d", secs) + ":" + String.format("%03d", milliseconds);
            textView.setText(time);
            customHandler.postDelayed(this, 0);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record_race);

        //get registered sailors from database
        db = new SQLiteHandler(getApplicationContext());

        registeredSailorsList = new ArrayList<String>();

        registeredSailorsList = db.getSailorsRegistered();

        for(int i=0; i<registeredSailorsList.size();i++){
            gridArray.add(new Sailors(registeredSailorsList.get(i)));
        }

        //mydb.onUpgrade(openOrCreateDatabase(DBHandler.DATABASE_NAME, MODE_PRIVATE, null), 1, 2);

        //mydb.onUpgrade(openOrCreateDatabase(DBHandler.DATABASE_NAME, MODE_PRIVATE, null), 1, 2);

        gridView = (GridView)findViewById(R.id.gridView);
        recordRaceAdapter = new RecordRaceAdapter(this, R.layout.activity_record_race_adapter, gridArray);
        gridView.setAdapter(recordRaceAdapter);
    }

    //Button allStart = (Button)findViewById(R.id.allstartbutton);
    public void AllStart(View view){
        Chronometer myChronometer;

        for(int i=0; i<=gridView.getLastVisiblePosition(); i++){
            //TODO Auto-generated method stub
            myChronometer = (Chronometer)gridView.findViewWithTag(registeredSailorsList.get(i));
            myChronometer.setBase(SystemClock.elapsedRealtime());
            myChronometer.start();
        }

        textView = (TextView)findViewById(R.id.timeValue);
        startTime = SystemClock.elapsedRealtime();
        timeSwapBuff = 0L;
        customHandler.postDelayed(updateTimerThread, 0);
    }

    public void AllStop(View view){
        Chronometer myChronometer;
        for(int i=0; i<=gridView.getLastVisiblePosition();i++){
            myChronometer = (Chronometer)gridView.findViewWithTag(registeredSailorsList.get(i));
            myChronometer.setBase(SystemClock.elapsedRealtime());
            myChronometer.stop();
        }
        timeSwapBuff += timeInMilliseconds;
        customHandler.removeCallbacks(updateTimerThread);
    }

    public void OpenDatabase(View view){
        /**mydb.getData(1);
         Bundle dataBundle = new Bundle();
         dataBundle.putInt("id", 1);**/

        Intent intent = new Intent(getApplicationContext(), ViewRaceResults.class);
        //intent.putExtras(dataBundle);
        startActivity(intent);
    }
}
