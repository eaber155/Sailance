package timer;

import android.content.Context;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.TextView;
import android.widget.Toast;

import com.evivian.admin.sailance.R;
import timer.Sailors;

import java.util.ArrayList;

import helper.SQLiteHandler;

public class RecordRaceAdapter extends BaseAdapter {

    private Context context;
    private int layoutResourceId;
    private ArrayList<Sailors> data = new ArrayList<Sailors>();
    private SQLiteHandler db;

    public RecordRaceAdapter(Context context, int layoutResourceId, ArrayList<Sailors> data){
        super();
        this.context = context;
        this.layoutResourceId = layoutResourceId;
        this.data = data;
    }

    public void UpdateChronometer(ArrayList<Sailors> data){
        this.data = data;
        notifyDataSetChanged();
    }


    public int getCount(){
        return data.size();
    }

    //getitem returns oobject but we can override
    @Override
    public Sailors getItem(int position){
        return data.get(position);
    }

    //getitemid is often useless but it shou;ld be there
    public long getItemId(int position){
        return position;
    }

    public View getView(final int position, View convertView, ViewGroup parent){
        View row = convertView;
        final RecordHolder holder;

        if(row == null){
            LayoutInflater inflater = ((AppCompatActivity) context).getLayoutInflater();
            row = inflater.inflate(layoutResourceId, parent, false);

            holder = new RecordHolder();
            holder.nameOfParticipant = (TextView)row.findViewById(R.id.textView1);
            holder.myChronometer = (Chronometer)row.findViewById(R.id.myChronometer);
            holder.start = (Button)row.findViewById(R.id.startButton);
            holder.stop = (Button)row.findViewById(R.id.stopButton);
            holder.reset = (Button)row.findViewById(R.id.resetButton);
            row.setTag(holder);
        }else{
            holder = (RecordHolder)row.getTag();
        }

        db = new SQLiteHandler(context);
        final Sailors participants = data.get(position);
        holder.nameOfParticipant.setText(participants.getNameOfParticipants());
        holder.myChronometer.setTag(participants.getNameOfParticipants());
        final Chronometer thisChronometer = (Chronometer)row.findViewWithTag(holder.nameOfParticipant.getText());
        holder.start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //TODO Auto-generated method stub
                if (holder.myChronometer.getTag().toString().equals(holder.nameOfParticipant.getText().toString())) {
                    thisChronometer.setBase(SystemClock.elapsedRealtime());
                    Toast.makeText(context, "" + position, Toast.LENGTH_LONG).show();
                    thisChronometer.start();
                    holder.stop.setVisibility(View.VISIBLE);
                    holder.reset.setVisibility(View.GONE);
                }
            }
        });

        holder.stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //TODO Auto-generated method stub
                if (holder.myChronometer.getTag().toString().equals(holder.nameOfParticipant.getText().toString())) {
                    thisChronometer.stop();
                    Long timeElapsed=SystemClock.elapsedRealtime()-thisChronometer.getBase();
                    int hours =(int)(timeElapsed/3600000);
                    int minutes = (int)(timeElapsed-hours*3600000)/60000;
                    int seconds = (int)(timeElapsed-hours*3600000-minutes*60000)/1000;
                    String timeTaken = ""+minutes+":"+String.format("%02d",seconds);

                    if(db.recordElapsedTime(holder.nameOfParticipant.getText().toString(), timeTaken)){
                        Toast.makeText(context, "done ", Toast.LENGTH_LONG).show();
                    }else {
                        Toast.makeText(context, "not done", Toast.LENGTH_LONG).show();
                    }
                    holder.stop.setVisibility(View.GONE);
                    holder.reset.setVisibility(View.VISIBLE);
                }
            }
        });

        holder.reset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //TODO Auto-generated method stub
                if (holder.myChronometer.getTag().toString().equals(holder.nameOfParticipant.getText().toString())) {
                    thisChronometer.setBase(SystemClock.elapsedRealtime());

                    Toast.makeText(context, ""+position, Toast.LENGTH_LONG).show();
                    holder.reset.setVisibility(View.GONE);
                    holder.stop.setVisibility(View.VISIBLE);
                }
            }
        });
        return row;
    }

    private static class RecordHolder{
        TextView nameOfParticipant;
        Chronometer myChronometer;
        Button start, stop, reset;
    }
}
