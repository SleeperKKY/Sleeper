package org.androidtown.sleeper.statistic_manage_activity;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.androidtown.sleeper.R;

/**
 * Created by Administrator on 2015-08-19.
 */
@Deprecated
public class SleepDataItemView extends LinearLayout {
    TextView itemDateView;
    TextView itemSleepView;
    TextView itemWakeView;


    public SleepDataItemView(Context context) {
        super(context);
        init(context);
    }

    public SleepDataItemView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    private void init(Context context){
        LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.layout_sleepdata_item,this,true);

        itemDateView = (TextView)findViewById(R.id.itemDateView);
        itemSleepView = (TextView)findViewById(R.id.itemSleepView);
        itemWakeView = (TextView)findViewById(R.id.itemWakeView);

    }   public void setItemDate(String itemDate){
        itemDateView.setText(itemDate);
    }
    public void setItemSleep(String itemSleep){
        itemSleepView.setText(itemSleep);
    }
    public void setItemWake(String itemWake){
        itemWakeView.setText(itemWake);
    }


}
