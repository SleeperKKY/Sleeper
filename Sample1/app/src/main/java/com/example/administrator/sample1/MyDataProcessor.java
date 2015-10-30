package com.example.administrator.sample1;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import org.sleeper.propclasses.dataprocessor_manager.clDataProcessor;
import org.sleeper.propclasses.dataprocessor_manager.clStatManager;

/**
 * Created by Administrator on 2015-10-29.
 */
public class MyDataProcessor extends clDataProcessor implements clDataProcessor.clSleepStageClassifier.ISleepStateListener{

    clSleepStageClassifier sleepStageClassifier=null ;
    protected MyDataProcessor(Context context) {
        super(context);

        sleepStageClassifier=getSleepStageClassifier() ;

        sleepStageClassifier.registerSleepStateListener(this);
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
    public clDatabaseManager getDatabase() {
        return super.getDatabase();
    }

    @Override
    public clStatManager createStatManager(int i) {
        return null;
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

        Log.i("SleepState: ",""+sleepState) ;
    }
}
