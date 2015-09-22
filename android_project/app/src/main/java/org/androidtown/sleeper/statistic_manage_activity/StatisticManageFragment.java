package org.androidtown.sleeper.statistic_manage_activity;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.FrameLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.Series;

import org.androidtown.sleeper.MainActivity;
import org.androidtown.sleeper.R;
import org.androidtown.sleeper.propclasses.dataprocessor_manager.clStatManager;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * Created by Administrator on 2015-08-11.
 */
public class StatisticManageFragment extends Fragment{


    private  View rootView=null ;
    private clStatManager statManager=null ;
    private CheckBox[] checkboxes=null ;
    private GraphView graph=null ;
    private SeekBar  horizontalAxisSeekBar=null ;
    private MainActivity mainActivity;
    private List<GraphView> graphList;
    private List<String> dataNameList;
    private List<String> dataList;
    private TextView[] variableDataTextSeries1;
    private TextView[] variableDataTextSeries2;
    private TextView timeNameTextView;
    private TextView timeDataTextView;
    private TextView[] variableDataNameTextSeries1;
    private TextView[] variableDataNameTextSeries2;
    private RadioButton checkedRadioButton=null ;

    public static final String Tag="StatisticManageFragment" ;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        rootView=inflater.inflate(R.layout.layout_statistic_manage, container, false) ;

        Log.i(toString(), "On Create View called") ;

        //display only if statmanager is set
        if(statManager!=null) {
            Log.i("hello",toString()) ;
            InitDisplay();
        }

        return rootView ;
    }

    private void InitDisplay() {

        //get main activity for context
        mainActivity = (MainActivity) getActivity();

        graphList=statManager.getGraphList() ;//get stored list of graph
        dataNameList=statManager.getStaticDataNameList();//get list of static data name
        dataList = statManager.getStaticDataList();//get list of static data


        ViewGroup graphRadioGroup = (ViewGroup) rootView.findViewById(R.id.radioGroup);
        final FrameLayout frameLayout = (FrameLayout)rootView.findViewById(R.id.frameLayout);
        final TableLayout tableLayout = (TableLayout)rootView.findViewById(R.id.tableLayout);

        for (int i = 0; i <graphList.size(); i++) {

            //Log.i("graph", Integer.toString(i)) ;
            //create radio button per graph in graph list
            RadioButton button = new RadioButton(mainActivity.getApplicationContext());
            button.setId(i);//set button id as index of graph's list
            button.setText(graphList.get(i).getTitle());
            button.setTextColor(0xFF909090);
            button.setChecked(i == 0);//check first radio button as default
            graphRadioGroup.addView(button);//add button on radio button group

            //register each radio button to show different graph
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    frameLayout.removeAllViews();//clear frame where previous graph is drawn
                    tableLayout.removeAllViews();//clear table layout where feature of graph is written
                    ((RadioGroup) view.getParent()).check(view.getId());
                    checkedRadioButton=(RadioButton)view ;//get reference to check radio button
                    int v = view.getId();
                    graph = graphList.get(v);

                    graph.getViewport().setYAxisBoundsManual(true);
                    graph.getViewport().setMinY(0);
                    graph.getViewport().setMaxY(2);

                    graph.getSecondScale().setMinY(15);
                    graph.getSecondScale().setMaxY(35);

                    graph.onDataChanged(false, false);
                    graph.getViewport().setScrollable(false);
                    //graph.getViewport().setScalable(true);

                    TextView[] dataNameText = new TextView[dataNameList.size()];
                    TextView[] dataText = new TextView[dataList.size()];

                    //add graph on frame layout
                    frameLayout.addView(graph);


                    //add graph's features in tableLayout
                    for (int i = 0; i < dataNameList.size(); i++) {
                        TableRow tableRow = new TableRow(mainActivity.getApplicationContext());

                        dataNameText[i] = new TextView(mainActivity.getApplicationContext());
                        dataNameText[i].setText(dataNameList.get(i));
                        dataNameText[i].setTextColor(0xFF909090);
                        dataNameText[i].setTextSize(30);


                        dataText[i] = new TextView(mainActivity.getApplicationContext());
                        dataText[i].setText(dataList.get(i));
                        dataText[i].setTextColor(0xFF909090);
                        dataText[i].setTextSize(30);

                        tableRow.addView(dataNameText[i]);
                        tableRow.addView(dataText[i]);

                        tableLayout.addView(tableRow);
                    }


                    //�ð��� ǥ������ textview�� �ϳ� �����Ѵ�.

                    timeNameTextView = new TextView(mainActivity.getApplicationContext());
                    timeDataTextView = new TextView(mainActivity.getApplicationContext());
                    timeNameTextView.setText("Time:");
                    timeNameTextView.setTextColor(0xFF909090);
                    timeNameTextView.setTextSize(30);
                    timeDataTextView.setText("Move SeekBar");
                    timeDataTextView.setTextColor(0xFF909090);
                    timeDataTextView.setTextSize(30);

                    TableRow timetableRow = new TableRow(mainActivity.getApplicationContext());

                    timetableRow.addView(timeNameTextView);
                    timetableRow.addView(timeDataTextView);

                    tableLayout.addView(timetableRow);

                    //�ø���1, �ø���2�� ���� textview �迭���� ���� �����Ѵ�.
                    variableDataTextSeries1 = new TextView[graph.getSeries().size()];
                    variableDataTextSeries2 = new TextView[graph.getSecondScale().getSeries().size()];
                    variableDataNameTextSeries1 = new TextView[graph.getSeries().size()];
                    variableDataNameTextSeries2 = new TextView[graph.getSecondScale().getSeries().size()];

                    //������1 �� textview�� tablerow�� ����Ѵ�.
                    for (int i = 0; i < graph.getSeries().size(); i++) {

                        TableRow tableRowScale1 = new TableRow(mainActivity.getApplicationContext());

                        variableDataNameTextSeries1[i] = new TextView(mainActivity.getApplicationContext());
                        variableDataTextSeries1[i] = new TextView(mainActivity.getApplicationContext());
                        variableDataNameTextSeries1[i].setText(graphList.get(0).getSeries().get(i).getTitle());
                        variableDataNameTextSeries1[i].setTextColor(0xFF909090);
                        variableDataNameTextSeries1[i].setTextSize(30);
                        variableDataTextSeries1[i].setText("move SeekBar");
                        variableDataTextSeries1[i].setTextColor(0xFF909090);
                        variableDataTextSeries1[i].setTextSize(30);
                        tableRowScale1.addView(variableDataNameTextSeries1[i]);
                        tableRowScale1.addView(variableDataTextSeries1[i]);

                        tableLayout.addView(tableRowScale1);
                    }

                    for (int i = 0; i < graph.getSecondScale().getSeries().size(); i++) {

                        TableRow tableRowScale2 = new TableRow(mainActivity.getApplicationContext());

                        variableDataNameTextSeries2[i] = new TextView(mainActivity.getApplicationContext());
                        variableDataTextSeries2[i] = new TextView(mainActivity.getApplicationContext());
                        variableDataNameTextSeries2[i].setText(graph.getSecondScale().getSeries().get(i).getTitle());
                        variableDataNameTextSeries2[i].setTextColor(0xFF909090);
                        variableDataNameTextSeries2[i].setTextSize(30);
                        variableDataTextSeries2[i].setText("move SeekBar");
                        variableDataTextSeries2[i].setTextColor(0xFF909090);
                        variableDataTextSeries2[i].setTextSize(30);
                        tableRowScale2.addView(variableDataNameTextSeries2[i]);
                        tableRowScale2.addView(variableDataTextSeries2[i]);

                        tableLayout.addView(tableRowScale2);
                    }

                    horizontalAxisSeekBar = (SeekBar) rootView.findViewById(R.id.seekBar);

                    horizontalAxisSeekBar.setMax(statManager.getDataSizeList().get(v) - 2);
                    horizontalAxisSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                        @Override
                        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

                            //change data display as seekbar moves
                            GraphView currentGraph=graphList.get(checkedRadioButton.getId()) ;
                            Series<DataPoint> series1 ;
                            Series<DataPoint> series2 ;

                            //change series 1's feature in tableLayout
                            for(int i=0;i<currentGraph.getSeries().size();i++) {

                                series1 = currentGraph.getSeries().get(i);


                                variableDataTextSeries1[i].setText(String.valueOf(series1.getValues(statManager.getXDataList().get(checkedRadioButton.getId())[progress + 1],
                                        statManager.getXDataList().get(checkedRadioButton.getId())[progress]).next().getY()));
                            }

                            //change series 1's feature in tableLayout
                            for(int i=0;i<currentGraph.getSecondScale().getSeries().size();i++) {

                                series2 =currentGraph.getSecondScale().getSeries().get(i);

                                variableDataTextSeries2[i].setText(String.valueOf(series2.getValues(statManager.getXDataList().get(checkedRadioButton.getId())[progress + 1],
                                        statManager.getXDataList().get(checkedRadioButton.getId())[progress]).next().getY()));


                            }

                            SimpleDateFormat transFormat = new SimpleDateFormat("HH:mm");
                            //timeDataTextView.setText(String.valueOf(transFormat.format(series1.getValues(statManager.getXData()[progress + 1], statManager.getXData()[progress]).next().getX())));
                            timeDataTextView.setText(transFormat.format(new Date((long)(statManager.getXDataList().get(checkedRadioButton.getId())[progress])))) ;
                        }

                        @Override
                        public void onStartTrackingTouch(SeekBar seekBar) {

                        }

                        @Override
                        public void onStopTrackingTouch(SeekBar seekBar) {

                        }
                    });

                }
            });
        }
    }

    //set statManager
    public void setStatManager(clStatManager statManager){

        this.statManager=statManager ;
    }

}