package org.androidtown.sleeper;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.androidtown.sleeper.setting_fragment.SettingFragment;
import org.androidtown.sleeper.sleep_manage_activity.SleepManageFragment;
import org.androidtown.sleeper.statistic_manage_activity.StatisticManageSelectFragment;

/**
 * Created by Administrator on 2015-08-17.
 */
public class ViewPagerFragment extends Fragment {

    private Fragment[] fragmentTabs=null ;
    private String[] fragmentTabNames=null ;
    private View rootView=null ;
    private static int FragmentCount=2 ;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        rootView = inflater.inflate(R.layout.fragment_main, container, false);

        InitFragments();

        ViewPager pager = (ViewPager) rootView.findViewById(R.id.pager);
        pager.setAdapter(new MainFragmentPagerAdapter(getChildFragmentManager()));

        //Log.i(toString(),"On Create View called") ;

        return rootView ;
    }

    /**
     * initialize fragments, in which tab specs are added
     * you should put code for adding new tab spec
     */
    private void InitFragments(){

        //initialize fragments
        fragmentTabs=new Fragment[FragmentCount] ;
        fragmentTabs[0]=new SleepManageFragment() ;
        fragmentTabs[1]=new StatisticManageSelectFragment() ;
        //fragmentTabs[2]=new SettingFragment() ;

        //set fragments' name
        fragmentTabNames=new String[FragmentCount] ;
        fragmentTabNames[0]="Sleep" ;
        fragmentTabNames[1]="Statistics" ;
        //fragmentTabNames[2]="Setting" ;
    }

    /**
     * fragment adapter of view pager fragment
     */
    public class MainFragmentPagerAdapter extends FragmentPagerAdapter{


        public MainFragmentPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            return fragmentTabs[position] ;
        }


        @Override
        public int getCount() {
            return FragmentCount ;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return fragmentTabNames[position] ;
        }
    }


}
