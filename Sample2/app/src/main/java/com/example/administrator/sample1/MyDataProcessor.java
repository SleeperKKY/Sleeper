package com.example.administrator.sample1;

import android.content.Context;
import android.widget.Toast;

import org.sleeper.propclasses.com_manager.clComManager;
import org.sleeper.propclasses.com_manager.clRequestMessage;
import org.sleeper.propclasses.com_manager.clResponseMessage;
import org.sleeper.propclasses.dataprocessor_manager.clDataProcessor;

/**
 * Created by Administrator on 2015-10-29.
 */
public class MyDataProcessor extends clDataProcessor implements clDataProcessor.clSleepStageClassifier.ISleepStateListener,
                                                                    clComManager.IMessageListener
{

    private clSleepStageClassifier sleepStageClassifier=null ;
    private clComManager comManager=null ;
    private static final byte TEMP_SENSOR=0x10 ;//virtual temperature sensor id
    private static final byte TEMP_GET_TEMP=0x11 ;//temperature value retrieve command


    protected MyDataProcessor(Context context) {
        super(context);

        sleepStageClassifier=getSleepStageClassifier() ;

        sleepStageClassifier.registerSleepStateListener(this);

        comManager=new clComManager(this) ;
        comManager.setTimeoutUnit(5000);//set connection timeout
        comManager.setTimeoutCount(2);//set how many timeout to be tolerable

        clComManager.setPort(8090) ;//set port
    }

    @Override
    public void measureStart() {
        super.measureStart();
    }

    @Override
    public void measureStop() {
        super.measureStop();
    }

    @Override
    public void onSleepStateRetrievedEvent(int sleepState, Double[] doubles) {

        switch(sleepState){

            case clSleepStageClassifier.AWAKE :
                Toast.makeText(AttachedContext,"AWAKE",Toast.LENGTH_SHORT).show() ;
                break ;
            case clSleepStageClassifier.DEEP:
                Toast.makeText(AttachedContext,"DEEP",Toast.LENGTH_SHORT).show() ;
                break ;
            case clSleepStageClassifier.REM:
                Toast.makeText(AttachedContext,"REM",Toast.LENGTH_SHORT).show() ;
                break ;
        }

        //send 'get temperature' command
        clRequestMessage reqMsg=new clRequestMessage() ;

        reqMsg.setDeviceID(TEMP_SENSOR) ;
        reqMsg.setCommand(TEMP_GET_TEMP) ;
        comManager.connect() ;
        comManager.send(reqMsg,true) ;

    }

    /**
     * Called when response message received
     * @param ResponseMessage response message
     */
    @Override
    public void onReceiveMessageEvent(clResponseMessage ResponseMessage) {

        byte devId=ResponseMessage.getDeviceID() ;

        if(devId==TEMP_SENSOR){
            byte[] data=ResponseMessage.getData() ;

            Toast.makeText(AttachedContext,"Temperature Retrieved: "+data[0],Toast.LENGTH_SHORT) ;
        }
    }
}
