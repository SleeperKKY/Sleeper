package org.androidtown.sleeper.sleep_manage_activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TimePicker;
import android.widget.Toast;

import org.androidtown.sleeper.MainActivity;
import org.androidtown.sleeper.R;
import org.androidtown.sleeper.propclasses.app_manager.clApp;

import java.util.Calendar;

/**
 * Created by Administrator on 2015-07-28.
 */
public class AutoModeFragment extends Fragment{

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState){

        //inflate layout_sleep_manage layout into its fragment
        final View rootView=inflater.inflate(R.layout.layout_auto_mode,container,false) ;

        final Button startSleepMeasureButton=(Button)rootView.findViewById(R.id.btnStartSleepMeasure) ;

       // Log.i(toString(),"On Create View called") ;

        //set main activity's listener since it has to cover all window
        startSleepMeasureButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                MainActivity mainActivity = (MainActivity) getParentFragment().getActivity();

                Calendar calendar = Calendar.getInstance();

                //get current time in 24hour
                int currentHour = calendar.get(Calendar.HOUR_OF_DAY);
                int currentMinute = calendar.get(Calendar.MINUTE);

                TimePicker timePicker = (TimePicker) rootView.findViewById(R.id.timepickerTimeChooser);
                long ringTimeMillis = ((timePicker.getCurrentHour() - currentHour) * 3600 + (timePicker.getCurrentMinute() - currentMinute) * 60) * 1000;

                //if current time and selected alarm time is not on same am_pm
                if (ringTimeMillis <= 0) {
                    ringTimeMillis += (24 * 3600 * 1000);
                }

                //Log.i(toString(),Long.toString(ringTimeMillis)) ;

                //mainActivity.startSleepMode(ringTimeMillis);

                mainActivity.getApp().startSleepMode(ringTimeMillis);

                //start sleep mode
                DuringSleepFragment duringSleepFragment = new DuringSleepFragment();
                duringSleepFragment.setAlarmTime(ringTimeMillis);
                mainActivity.getSupportFragmentManager().beginTransaction().
                        replace(R.id.fragmentContainer, duringSleepFragment, "DuringSleepFragment").
                        addToBackStack(null).commit();
                //start sleep mode

                Toast.makeText(mainActivity, "Sleep Mode Started", Toast.LENGTH_SHORT).show();

                long ringHour = (ringTimeMillis / 3600000);
                long ringMinute = (ringTimeMillis % 3600000) / 60000;

                Toast.makeText(getActivity(), "Alarm will ring in " + ringHour + " hours " + ringMinute + " minutes", Toast.LENGTH_SHORT).show();
            }
        });

        return rootView ;
    }
}
