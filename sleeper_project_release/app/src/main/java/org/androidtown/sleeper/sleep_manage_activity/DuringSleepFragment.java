//
//
//  Generated by StarUML(tm) Java Add-In
//
//  @ Project : Sleeper
//  @ File Name : clDataProcessor.java
//  @ Date : 2015-09-06
//  @ Author : Kang Shin Wook, Kim Hyun Woong
//  @ Email : rkdtlsdnr102@naver.com
//
//
// Copyright (C) 2015  Kang Shin Wook, Kim Hyun Woong
//
//  This program is free software; you can redistribute it and/or modify
//  it under the terms of the GNU General Public License as published by
//  the Free Software Foundation; either version 2 of the License, or
//  (at your option) any later version.
//
//  This program is distributed in the hope that it will be useful,
//  but WITHOUT ANY WARRANTY; without even the implied warranty of
//  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//  GNU General Public License for more details.
//
//  You should have received a copy of the GNU General Public License along
//  with this program; if not, write to the Free Software Foundation, Inc.,
//  51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.

package org.androidtown.sleeper.sleep_manage_activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.androidtown.sleeper.MainActivity;
import org.androidtown.sleeper.R;
import org.sleeper.propclasses.app_manager.clApp;

import java.text.SimpleDateFormat;
import java.util.Date;

public class DuringSleepFragment extends Fragment {

    private long ringTimeMillis=0 ;
    private AlarmReceiver alarmReceiver=null ;
    private boolean isRegistered=false ;
    private View rootView=null ;

    private static final String status_sleeping="Sleeping..." ;
    private static final String status_wakeup="Wake up!!" ;

    public static final String Tag="DuringSleepFragment" ;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        if(!isRegistered) {
            IntentFilter filter = new IntentFilter();
            filter.addAction(clApp.APP_ACTION_ALARM_TRIGGERED);
           alarmReceiver = new AlarmReceiver();
           getActivity().registerReceiver(alarmReceiver, filter);

            Log.i("", "alarm registered");
            isRegistered = true;
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState){

        //inflate layout_during_sleep layout into its fragment
        rootView= inflater.inflate(R.layout.layout_during_sleep,container,false) ;

       // Log.i(toString(), "On Create View called") ;
       Button btnStopSleepMeasure=(Button)rootView.findViewById(R.id.btnStopSleepMeasure) ;
        TextView txtViewAlarmTime=(TextView)rootView.findViewById(R.id.textViewAlarmTime) ;

        //set ring time on alarm time text view
        SimpleDateFormat dateFormat=new SimpleDateFormat("h:mm a") ;
        Date date=new Date(System.currentTimeMillis()+ringTimeMillis) ;
        txtViewAlarmTime.setText(txtViewAlarmTime.getText() + dateFormat.format(date)) ;

        TextView textViewSleepModeStatus=(TextView)rootView.findViewById(R.id.textViewSleepModeStatus) ;

        textViewSleepModeStatus.setText(getResources().getString(R.string.sleep_mode_status)+status_sleeping) ;

        //set stop measure button click listener
        btnStopSleepMeasure.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v){

                MainActivity mainActivity=(MainActivity)getActivity() ;

                //mainActivity.stopSleepMode();

                mainActivity.getSupportFragmentManager().popBackStack();
                // getSupportFragmentManager().beginTransaction().replace(R.id.fragmentContainer,viewPagerFragment,"ViewPagerFragment").commit() ;
                mainActivity.getApp().stopSleepMode();

                Toast.makeText(mainActivity, "Sleep Mode Stopped", Toast.LENGTH_SHORT).show() ;
            }
        });

        return rootView ;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if(isRegistered)
            getActivity().unregisterReceiver(alarmReceiver);
    }

    public void setAlarmTime(long ringTimeMillis){

        this.ringTimeMillis=ringTimeMillis ;
    }

    /**
     * Receives alarm intent
     */

    private class AlarmReceiver extends BroadcastReceiver {

        public AlarmReceiver() {
            super();
        }

        //it receives only alarm related intent
        @Override
        public void onReceive(Context context, Intent intent) {

            //else if alarm ring intent received
            //you should put condition to check if intent is correct
            /*
            if(intent.getAction().equals(clApp.APP_ACTION_ALARM_TRIGGERED)) {

                MainActivity mainActivity=(MainActivity)context ;

                try {
                    mainActivity.getSupportFragmentManager().popBackStack();

                }catch(IllegalStateException e){

                }
                Log.i("", "alarm received") ;
                //getSupportFragmentManager().beginTransaction().replace(R.id.fragmentContainer,viewPagerFragment,"ViewPagerFragment").commit() ;
            }
            */
            if(intent.getAction().equals(clApp.APP_ACTION_ALARM_TRIGGERED)) {


                TextView textViewSleepModeStatus=(TextView)rootView.findViewById(R.id.textViewSleepModeStatus) ;

                textViewSleepModeStatus.setText(getResources().getString(R.string.sleep_mode_status)+status_wakeup) ;
                //getSupportFragmentManager().beginTransaction().replace(R.id.fragmentContainer,viewPagerFragment,"ViewPagerFragment").commit() ;
            }

        }
    }


}
