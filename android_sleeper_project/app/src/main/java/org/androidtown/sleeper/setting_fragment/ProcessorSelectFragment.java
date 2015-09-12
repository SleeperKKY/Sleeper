package org.androidtown.sleeper.setting_fragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;


import org.androidtown.sleeper.R;


import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;

import dalvik.system.DexFile;

/**
 * Created by Administrator on 2015-08-31.
 */
@Deprecated
public class ProcessorSelectFragment extends Fragment implements View.OnClickListener, AdapterView.OnItemClickListener{


    private static String PROCESSOR_FILE_DIRECTORY= Environment.getExternalStorageDirectory()+"/SleeperLib/custom-proc" ;
    private static String EXPLANATION_FILE_NAME="explanation" ;
    private static String SUPPORTED_DEVICE_LIST_FILE_NAME="supported-device-list" ;

    private String connectedDeviceName ="" ;
    private View rootView=null ;
    private ArrayAdapter<String> arrayAdapter=null ;
    private WifiReceiver wifiReceiver=null ;
    private String selectedProcessorName="" ;



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        rootView=inflater.inflate(R.layout.layout_processor_select,container,false) ;

        ListView supportedProcessorList=(ListView)rootView.findViewById(R.id.listViewProcessorList) ;

        //int[] to={R.id.textViewSupportedProcessor,R.id.radioBtnSupportedProcessor} ;
        //create list view adapter
        arrayAdapter=new ArrayAdapter<>(getActivity(),
                R.layout.layout_processor_item,R.id.textViewSupportedProcessor) ;

        supportedProcessorList.setAdapter(arrayAdapter) ;
        supportedProcessorList.setChoiceMode(ListView.CHOICE_MODE_SINGLE) ;
        supportedProcessorList.setOnItemClickListener(this);

        //set button listener
        Button btnShowSelectedProcessor=(Button)rootView.findViewById(R.id.btnShowSelectedProcessor) ;
        Button btnConfirm=(Button)rootView.findViewById(R.id.btnConfirmProcessorSelect) ;
        Button btnCancel=(Button)rootView.findViewById(R.id.btnCloseProcessorSelect) ;

        btnShowSelectedProcessor.setOnClickListener(this);
        btnConfirm.setOnClickListener(this);
        btnCancel.setOnClickListener(this);

        return rootView ;

    }

    @Override
    public void onResume() {

        IntentFilter intentFilter=new IntentFilter(WifiManager.NETWORK_STATE_CHANGED_ACTION) ;
        wifiReceiver=new WifiReceiver() ;
        getActivity().registerReceiver(wifiReceiver, intentFilter) ;

        super.onResume();
    }

    @Override
    public void onPause() {

        getActivity().unregisterReceiver(wifiReceiver);
        super.onPause();
    }

    /**
     * list processors that supports device that is currently connected via wifi
     * @param devName device that is currently connected via wifi
     */
    private void listProcessors(String devName){

        //list new processors
        File customProcDir=new File(PROCESSOR_FILE_DIRECTORY) ;
        String[] supportingDeviceNameArray=null ;

        if(customProcDir.exists()) {
            //list all directories in custom-proc directory
            File[] processorDirArray = customProcDir.listFiles();

            //search all files inside each directory in custom-proc directory
            for (int i = 0; i < processorDirArray.length; i++) {

                //if processorDirArray[i] is directory
                if (processorDirArray[i] != null) {

                    supportingDeviceNameArray=readSupportedDeviceFile(processorDirArray[i].getAbsolutePath());

                    if(supportingDeviceNameArray!=null) {

                        for (int j = 0; j < supportingDeviceNameArray.length; j++){

                            //if i'th processor supports currently connected device
                            if(supportingDeviceNameArray[j].equals(connectedDeviceName))
                            {
                                //add that processor's name on list view's adapter
                                arrayAdapter.add(processorDirArray[i].getName()) ;
                            }
                        }

                    }

                }
            }
        }else
            Log.d(toString(),PROCESSOR_FILE_DIRECTORY+"/"+" doesn't exist") ;

    }

    private void showDescription(String processorName){


    }

    /**
     * read supporting devices from 'supported-device-list' file of given processor
     * @param processorDir processor library directory
     * @return supporting devices
     */
    private String[] readSupportedDeviceFile(String processorDir){


        File explanationFile=null ;
        //file input stream to read 'supportedDeviceList' file
        BufferedInputStream binStream = null;
        ArrayList<String> supportingDeviceList=null ;


        //*there should always be 1 explanationFile
        try{

            //read this processor's support device name
           explanationFile=new File(processorDir+"/"+SUPPORTED_DEVICE_LIST_FILE_NAME) ;
            binStream=new BufferedInputStream(new FileInputStream(explanationFile)) ;

            int data ;
            supportingDeviceList=new ArrayList<>() ;
            String supportingDeviceName="" ;


            while(true)
            {
                if((data=(binStream.read()))!=-1) {//if file not reached its end, which return -1

                    if (data == ' ' || data == '\n') {//if read byte is delimiter

                        supportingDeviceList.add(supportingDeviceName);//add supporting device name to the list
                        Log.i("devName: ", supportingDeviceName);
                        supportingDeviceName = "";

                    } else {
                        supportingDeviceName += (char)(data);
                    }
                }
                else//if not
                {
                    //add last device name to the device name list
                    supportingDeviceList.add(supportingDeviceName);
                    Log.i("devName: ", supportingDeviceName);
                    break ;
                }
            }

        }catch(IOException e){

            Log.e("Wrong getting file",e.getMessage()) ;
        }

        return supportingDeviceList.toArray(new String[supportingDeviceList.size()]) ;
    }

    String readExplanationFile(String processorName){


        return null ;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

        setChecked(view);

        //get selected item

        selectedProcessorName=(String)parent.getAdapter().getItem(position) ;
    }

    private void changeProcessor(String processorName){

        File processorjarDir=new File(PROCESSOR_FILE_DIRECTORY+"/"+processorName) ;
        FilenameFilter fileFilter=new FilenameFilter(){

            @Override
            public boolean accept(File dir, String filename) {

                if(filename.contains(".jar"))
                    return true ;

                return false;
            }
        } ;

        File[] processorJarFile=processorjarDir.listFiles(fileFilter) ;

        //File dexFile=new File(Environment.getExternalStorageDirectory().toString()+"/classes.dex") ;

        Log.i("Found jar file name:", processorJarFile[0].getName()) ;
        //Log.i("Found jar file name:", dexFile.getName()) ;

        //there will be one jar file in each folder


                //URL[] url = {new URL("jar:"+ processorJarFile[0].toURI().toURL()+"!/")};
                //URLConnection c = url[0].openConnection();
                // c.setUseCaches(true);

        try {
            DexFile dexFile = DexFile.loadDex(processorJarFile[0].toString(),getActivity().getDir("outdexDir", Context.MODE_PRIVATE).getAbsolutePath()+"/output",0) ;

            //PathClassLoader child=new PathClassLoader(jarFile.toString(),null,getActivity().getClass().getClassLoader()) ;
            //URLClassLoader child = new URLClassLoader(url,ClassLoader.getSystemClassLoader()) ;
           // DexClassLoader child = new DexClassLoader(processorJarFile[0].toString(), getActivity().getDir("outdex", Context.MODE_PRIVATE).getAbsolutePath(),
                //    null, ClassLoader.getSystemClassLoader());

            //DexClassLoader child=new DexClassLoader(dexFile.toString(),getActivity().getDir("outdex",Context.MODE_PRIVATE).getAbsolutePath(),
            //      null,ClassLoader.getSystemClassLoader()) ;

            try {

                //jar file name should contain package name + processor name
                Class class2Load = dexFile.loadClass("org.androidtown.sleeper.endclasses.clFanMessageConverter", ClassLoader.getSystemClassLoader()) ;
                //Constructor<clAccelTempDataProcessor> constructor = class2Load.getDeclaredConstructor(Context.class, clComManager.class);

               // Object inst = constructor.newInstance(getActivity(),new clComManager());
                Object inst=class2Load.newInstance() ;

                //cast instance to com.example.AccelTempProcessor

                //for testing
                Toast.makeText(getActivity(), inst.toString(), Toast.LENGTH_SHORT).show();

                //close opened jar file
                //((JarURLConnection)c).getJarFile().close() ;


                dexFile.close() ;

            } catch (IllegalAccessException e) {

                Log.e(e.toString(), e.getMessage());

            } catch (java.lang.InstantiationException e) {

                Log.e(e.toString(), e.getMessage());
            } /*catch (NoSuchMethodException e) {

                Log.e(e.toString(), e.getMessage());

            } catch (InvocationTargetException e) {
                Log.e(e.toString(), e.getMessage());
            }
            */

        }catch(IOException e){

            Log.e(e.toString(), e.getMessage());
        }
    }

    private void setChecked(View v){

        ViewGroup viewGroup=(ViewGroup)v.getParent() ;
        View cv=null;


        for(int i=0;i<viewGroup.getChildCount();i++){

            cv=viewGroup.getChildAt(i) ;

            ((RadioButton)cv.findViewById(R.id.radioBtnSupportedProcessor)).setChecked(false);
        }

        ((RadioButton)(v.findViewById(R.id.radioBtnSupportedProcessor))).setChecked(true) ;
    }

    @Override
    public void onClick(View v) {

        if(v.getId()==R.id.btnConfirmProcessorSelect){

            //change app's dataprocessor to new one
            changeProcessor(selectedProcessorName);
            getActivity().getSupportFragmentManager().popBackStack();
        }
        else if(v.getId()==R.id.btnCloseProcessorSelect){

            getActivity().getSupportFragmentManager().popBackStack();
        }
        else if(v.getId()==R.id.btnShowSelectedProcessor){

            Toast.makeText(getActivity(),connectedDeviceName,Toast.LENGTH_SHORT).show();

            //clear listview first
            arrayAdapter.clear() ;

            if(!connectedDeviceName.isEmpty())//if some device is connected
                listProcessors(connectedDeviceName);
        }
    }

    private class WifiReceiver  extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {

            if (intent.getAction().equals(WifiManager.NETWORK_STATE_CHANGED_ACTION)) {


                if(isConnectedViaWifi(context)) {
                    TextView textViewConnectedDevice = (TextView) rootView.findViewById(R.id.textViewConnectedDevice2) ;

                    WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);


                    connectedDeviceName=wifiManager.getConnectionInfo().getSSID();
                    connectedDeviceName=connectedDeviceName.substring(1,connectedDeviceName.length()-1) ;
                    textViewConnectedDevice.setText(getResources().getString(R.string.connected_device) + connectedDeviceName);

                }
                else
                {
                    TextView textViewConnectedDevice = (TextView) rootView.findViewById(R.id.textViewConnectedDevice2);
                    connectedDeviceName="" ;
                    textViewConnectedDevice.setText(getResources().getString(R.string.no_connected_device)) ;
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