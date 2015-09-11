package org.androidtown.sleeper.sleep_manage_activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import org.androidtown.sleeper.R;

/**
 * Created by Administrator on 2015-07-24.
 */
public class SleepManageFragment extends Fragment implements View.OnClickListener{

    private AutoModeFragment automodeFragment=null ;
    private ManualModeFragment manualModeFragment=null ;

    private static final String autoModeFragmentTag="AutoModeFragment" ;
    private static final String manualModeFragmentTag="ManualModeFragment" ;

    View rootView=null ;
    private WifiReceiver wifiReceiver=null ;
    private String connectedDeviceName="" ;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState){

        //inflate layout_sleep_manage layout into its fragment
        rootView= inflater.inflate(R.layout.layout_sleep_manage,container,false) ;

        //Log.i(toString(), "On Create View called") ;

        //manually create auto mode fragment
        automodeFragment=new AutoModeFragment() ;

        getChildFragmentManager().beginTransaction().replace(R.id.flayoutMode,
                automodeFragment,autoModeFragmentTag).commit() ;

        //add register buttons and their listener
        //Button btnSelectAutoMode=(Button)rootView.findViewById(R.id.btnSelectAutoMode) ;
        //Button btnSelectManualMode=(Button)rootView.findViewById(R.id.btnSelectManualMode) ;

        //register onclick listener on automode fragment
        //btnSelectAutoMode.setOnClickListener(this) ;

        //register onclick listener on manual fragment
        //btnSelectManualMode.setOnClickListener(this);

        return rootView ;
    }


    @Override
    public void onResume() {

        //create wifireceiver to receive wifi state change broadcast
        IntentFilter filter=new IntentFilter() ;
        filter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION) ;
        wifiReceiver=new WifiReceiver() ;
        getActivity().registerReceiver(wifiReceiver, filter) ;
        //Log.i(toString(), "OnCreate called") ;
        super.onResume();
    }


    //it will be called when this fragment destroyed on view, especially when replaced by other
    @Override
    public void onPause() {

        getActivity().unregisterReceiver(wifiReceiver);
        super.onPause();
    }

    @Override
    public void onClick(View v) {

        /*
        if(v.getId()==R.id.btnSelectAutoMode)
        {
            automodeFragment=new AutoModeFragment() ;

            getChildFragmentManager().beginTransaction().replace(R.id.flayoutMode,
                    automodeFragment,autoModeFragmentTag).commit();

            manualModeFragment=null ;
        }
        */
        /*
        else if(v.getId()==R.id.btnSelectManualMode)
        {
            manualModeFragment=new ManualModeFragment() ;

            getChildFragmentManager().beginTransaction().replace(R.id.flayoutMode,
                    manualModeFragment,manualModeFragmentTag).commit();

            automodeFragment=null ;
        }
        */
    }

    /**
     * Wifi state change receiver
     */
    private class WifiReceiver  extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {


            if (intent.getAction().equals(WifiManager.NETWORK_STATE_CHANGED_ACTION)) {


                if (isConnectedViaWifi(context)) {
                    TextView textViewConnectedDevice = (TextView) rootView.findViewById(R.id.textViewConnectedDevice);

                    WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);


                    connectedDeviceName = wifiManager.getConnectionInfo().getSSID();

                    connectedDeviceName=connectedDeviceName.substring(1,connectedDeviceName.length()-1) ;
                    textViewConnectedDevice.setText(getResources().getString(R.string.connected_device) + connectedDeviceName);


                } else {
                    TextView textViewConnectedDevice = (TextView) rootView.findViewById(R.id.textViewConnectedDevice);
                    connectedDeviceName="" ;
                    textViewConnectedDevice.setText(getResources().getString(R.string.no_connected_device));
                }
            }
        }

        /**
         * checks if app is currently connected via wifi
         * @return true if connected, otherwise false
         */
        private boolean isConnectedViaWifi(Context context) {
            ConnectivityManager connectivityManager = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo mWifi = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);


            return mWifi.getState().equals(NetworkInfo.State.CONNECTED) ;
        }
    }
}
