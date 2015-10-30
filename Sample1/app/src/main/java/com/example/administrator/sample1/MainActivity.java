package com.example.administrator.sample1;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import org.sleeper.propclasses.app_manager.clApp;
import org.sleeper.propclasses.dataprocessor_manager.clDataProcessor;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    private clApp myApp=null ;
    private View startBtn=null ;
    private View stopBtn=null ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        startBtn=findViewById(R.id.startBtn) ;
        stopBtn=findViewById(R.id.stopBtn) ;

        startBtn.setOnClickListener(this);
        stopBtn.setOnClickListener(this);

        clDataProcessor dataProcessor=new MyDataProcessor(this) ;
        myApp=new clApp(this,dataProcessor) ;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();

        myApp.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();

        myApp.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        myApp.onDestroy();
    }

    @Override
    public void onClick(View v) {

        int vid=v.getId() ;

        if(vid==R.id.startBtn) {

            myApp.startSleepMode(600000);
        }
        else if(vid==R.id.stopBtn){
            myApp.stopSleepMode();
        }
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
}
