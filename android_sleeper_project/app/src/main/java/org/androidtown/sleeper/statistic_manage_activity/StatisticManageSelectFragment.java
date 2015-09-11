package org.androidtown.sleeper.statistic_manage_activity;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import org.androidtown.sleeper.MainActivity;
import org.androidtown.sleeper.R;
import org.androidtown.sleeper.propclasses.dataprocessor_manager.clDataProcessor;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Administrator on 2015-08-11.
 */


    public class StatisticManageSelectFragment extends Fragment {

    private View rootView ;
    private ListView  listView;


        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState){

            //inflate layout_sleep_manage layout into its fragment
            rootView=inflater.inflate(R.layout.layout_statistic_manage_select,container,false) ;


            return rootView ;
        }

    @Override
    public void onStart() {

        // final Button btnStatview=(Button)rootView.findViewById(R.id.btnStatview) ;
        listView = (ListView) rootView.findViewById(R.id.listView);

        final  MainActivity mainActivity = (MainActivity) getActivity();
        final clDataProcessor.clDatabaseManager db = mainActivity.getApp().getDataProcessor().getDatabase();

        try {
            final Cursor cursor = db.getWritableDatabase().rawQuery("SELECT _id, Date, StartTime, EndTime FROM DataTable_summary", null);

            String[] columns = new String[]{"Date", "StartTime", "EndTime"};
            int[] to = new int[]{R.id.itemDateView, R.id.itemSleepView, R.id.itemWakeView};

            final CustomSimpleCursorAdapter adapter = new CustomSimpleCursorAdapter(mainActivity, R.layout.layout_sleepdata_item,
                    cursor, columns, to);
            listView.setAdapter(adapter);
            adapter.notifyDataSetInvalidated();

            AdapterView.OnItemClickListener mItemClickListener = new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                    cursor.moveToPosition(position);



                    try {

                        mainActivity.ViewStatistic(cursor.getInt(
                                        cursor.getColumnIndex(clDataProcessor.clDatabaseManager.colCnt)
                                ));
                    }
                    catch (OutOfMemoryError e) {

                    }
                }
            };


            View button=rootView.findViewById(R.id.btnRemoveAllData) ;

            button.setOnClickListener(new View.OnClickListener(){

                @Override
                public void onClick(View v) {

                    Cursor cur=adapter.getCursor() ;

                    if(cur.getCount()>0) {

                        cur.moveToFirst();

                        long startTime;

                        startTime = cur.getLong(cur.getColumnIndex(clDataProcessor.clDatabaseManager.colStartTime));
                        db.deleteSummaryTable(startTime);


                        while (cur.moveToNext()) {

                            startTime = cur.getLong(cur.getColumnIndex(clDataProcessor.clDatabaseManager.colStartTime));
                            db.deleteSummaryTable(startTime);

                        }

                        final Cursor cursor = db.getWritableDatabase().rawQuery("SELECT _id, Date, StartTime, EndTime FROM DataTable_summary", null);

                        adapter.swapCursor(cursor);
                    }
                }
            }) ;

            listView.setOnItemClickListener(mItemClickListener);


        }catch(IllegalStateException e){

            Log.i(toString(),e.getMessage()) ;
        }

        super.onStart();

    }

    private class CustomSimpleCursorAdapter extends SimpleCursorAdapter{

        private LayoutInflater vi=null ;


        public CustomSimpleCursorAdapter(Context context, int layout, Cursor c, String[] from, int[] to) {
            super(context, layout, c, from, to);
            vi=LayoutInflater.from(context) ;
        }

        public CustomSimpleCursorAdapter(Context context, int layout, Cursor c, String[] from, int[] to, int flags) {
            super(context, layout, c, from, to, flags);
            vi=LayoutInflater.from(context) ;
        }

        @Override
        public void bindView(View view, Context context, Cursor cursor) {
            super.bindView(view, context, cursor);

            long startTime=cursor.getLong(cursor.getColumnIndex(clDataProcessor.clDatabaseManager.colStartTime)) ;
            long endTime=cursor.getLong(cursor.getColumnIndex(clDataProcessor.clDatabaseManager.colEndTime)) ;

            SimpleDateFormat simpleDateFormat=new SimpleDateFormat("HH:mm") ;

            ((TextView)view.findViewById(R.id.itemSleepView)).setText(simpleDateFormat.format(new Date(startTime))) ;
            ((TextView)view.findViewById(R.id.itemWakeView)).setText(simpleDateFormat.format(new Date(endTime))) ;

        }

        @Override
        public View newView(Context context, Cursor cursor, ViewGroup parent) {
            return super.newView(context, cursor, parent);
        }
    }


    /*
    public class SleepDateItemAdapter extends BaseAdapter{

        ArrayList<SleepDateItem> items = new ArrayList<SleepDateItem>();

        @Override
        public int getCount() {

            return items.size();
        }

        public void addItem(SleepDateItem item){
            items.add(item);
        }

        @Override
        public Object getItem(int position) {

            return items.get(position);

        }

        @Override
        public long getItemId(int position) {


            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            SleepDataItemView view = null;
            if(convertView == null){
                view =new SleepDataItemView(getActivity());
            }else{
                view=(SleepDataItemView) convertView;
            }
            view.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View v) {

                    MainActivity mainActivity = (MainActivity) getParentFragment().getActivity();
                    mainActivity.ViewStatisticManage();
                    //get datatable using datatable_summary
                }
            });

            SleepDateItem curItem =items.get(position);

            view.setItemDate(curItem.getDate());
            view.setItemSleep(curItem.getSleep());
            view.setItemWake(curItem.getWake());

            return view;
        }
    }*/
    }
