package org.androidtown.sleeper.sleep_manage_activity;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.androidtown.sleeper.R;

/**
 * Created by Administrator on 2015-07-28.
 */
@Deprecated
public class ManualModeFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState){

       //Log.i(toString(), "On Create View called") ;
        //inflate layout_manual_manage layout into its fragment
        return inflater.inflate(R.layout.layout_manual_mode,container,false) ;
    }
}
