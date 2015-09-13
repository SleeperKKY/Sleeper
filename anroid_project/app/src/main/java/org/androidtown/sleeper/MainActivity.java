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

/**
 * Main activity. It is nothing more than just central repository for many fragment. It currently
 * only offers access app and fragment manager.
 */
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

        //create view pager fragment and inflate in fragmentContainer
        //in fragmentContainer frame layout, different views are attached, including
        //table's view and also other views that need to cover all screen.
        viewPagerFragment=new ViewPagerFragment() ;
        getSupportFragmentManager().beginTransaction().replace(R.id.fragmentContainer, viewPagerFragment, ViewPagerFragment.Tag).commit();

        //create user defined dataprocessor and attach to app
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
     *Returns App model
     * @return App model
     */
    public clApp getApp(){

        return App;
    }

    @Override
    public void onBackPressed() {

        if(getSupportFragmentManager().findFragmentByTag(DuringSleepFragment.Tag)!=null) {
            App.stopSleepMode();
            Toast.makeText(this, "Sleep Mode Stopped", Toast.LENGTH_SHORT).show();
        }

        super.onBackPressed();
    }

}
