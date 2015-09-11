package org.androidtown.sleeper;

//import org.openintents.sensorsimulator.hardware.* ;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.widget.Toast;

import org.androidtown.sleeper.endclasses.clAccelTempDataProcessor;
import org.androidtown.sleeper.propclasses.app_manager.clApp;
import org.androidtown.sleeper.propclasses.dataprocessor_manager.clDataProcessor;
import org.androidtown.sleeper.propclasses.com_manager.clComManager;
import org.androidtown.sleeper.sleep_manage_activity.DuringSleepFragment;
import org.androidtown.sleeper.statistic_manage_activity.StatisticManageFragment;


public class MainActivity extends FragmentActivity {

    /**
     * App model that controls basic app performance(e.g wifi, alarm service)
     */
   private clApp App =null ;//

    /**
     * View pager fragment which is main fragment
     */
    private Fragment viewPagerFragment=null ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        viewPagerFragment=new ViewPagerFragment() ;
        getSupportFragmentManager().beginTransaction().replace(R.id.fragmentContainer, viewPagerFragment, "ViewPagerFragment").commit();

        clDataProcessor dataProcessor = new clAccelTempDataProcessor(this, new clComManager());
        App = new clApp(this,dataProcessor) ;

    }

    /*
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
    */

    @Override
    protected void onResume() {

        App.onResume() ;
        super.onResume();
    }

    @Override
    protected void onPause() {

        App.onPause() ;
        super.onPause();
    }

    @Override
    protected void onDestroy() {

        App.onDestroy();
        super.onDestroy();
    }

    /**
     * Start sleep mode, which measures sleep levels to control device
     * @param ringTimeMillis time in millisecond that alarm will ring
     *
     */
    @Deprecated
    public void startSleepMode(long ringTimeMillis) {


        DuringSleepFragment duringSleepFragment=new DuringSleepFragment() ;
        duringSleepFragment.setAlarmTime(ringTimeMillis);
        getSupportFragmentManager().beginTransaction().replace(R.id.fragmentContainer,duringSleepFragment,"DuringSleepFragment").addToBackStack(null).commit() ;
        //start sleep mode
        App.startSleepMode(ringTimeMillis);

        Toast.makeText(this, "Sleep Mode Started", Toast.LENGTH_SHORT).show() ;

    }

    /**
     *Returns App model
     * @return App model
     */
    public clApp getApp(){

        return App;
    }

    /**
     * Stop sleep mode
     */
    @Deprecated
    public void stopSleepMode(){

        getSupportFragmentManager().popBackStack();
       // getSupportFragmentManager().beginTransaction().replace(R.id.fragmentContainer,viewPagerFragment,"ViewPagerFragment").commit() ;
        App.stopSleepMode();

        Toast.makeText(this,"Sleep Mode Stopped",Toast.LENGTH_SHORT).show() ;
    }

    @Override
    public void onBackPressed() {

        if(getSupportFragmentManager().findFragmentByTag("DuringSleepFragment")!=null) {
            App.stopSleepMode();
            Toast.makeText(this, "Sleep Mode Stopped", Toast.LENGTH_SHORT).show();
        }

        super.onBackPressed();
    }

    @Deprecated
    public void ViewStatistic(int position) {


        StatisticManageFragment statisticManageFragment=new StatisticManageFragment() ;
        statisticManageFragment.setTablePosition(position);

        //getSupportFragmentManager().popBackStack();
        getSupportFragmentManager().beginTransaction().replace(R.id.fragmentContainer,statisticManageFragment,"StatisticManageFragment").addToBackStack(null).commit() ;
    }

}
