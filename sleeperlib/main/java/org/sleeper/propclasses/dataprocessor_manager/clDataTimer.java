package org.sleeper.propclasses.dataprocessor_manager;
//
//  Generated by StarUML(tm) Java Add-In
//
//  @ Project : Sleeper
//  @ File Name : clDataTimer.java
//  @ Date : 2015-09-06
//  @ Author : Kang Shin Wook
//  @ Email : rkdtlsdnr102@naver.com
//
//
// Copyright (C) 2015  Kang Shin Wook, Kim Hyun Woong
//
//  This program is free software; you can redistribute it and/or modify
//  it under the terms of the GNU General Public License as published by
//  the Free Software Foundation; either version 2 of the License, or
//  (at your option) any later version.
//
//  This program is distributed in the hope that it will be useful,
//  but WITHOUT ANY WARRANTY; without even the implied warranty of
//  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//  GNU General Public License for more details.
//
//  You should have received a copy of the GNU General Public License along
//  with this program; if not, write to the Free Software Foundation, Inc.,
//  51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.


import android.util.Log;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Provides interface to synchronized timer tasks. This class uses one timer thread and every
 * listener is processed in that thread. It synchronize timer period with least-common-devisor period
 * between periods of listeners so users are recommended to have their listeners have common divisor
 * between their periods.
 */
public class clDataTimer {

	private List<IDataTimerListener> dataTimerListenerList ;
	private List<Long> dataTimerListenerPeriodList ;
	private long[] dataTimerListenerPeriodCheckList ;
	private boolean isRunning=false ;
    private long minimumRequiredPeriod ;


	private TimerTask RepeatTask ;
	private Timer RepeatTimer ;

	private static clDataTimer instance=null ;

    /**
     * Constructor
     */
	private clDataTimer(){

		dataTimerListenerList=new ArrayList<>() ;
		dataTimerListenerPeriodList=new ArrayList<>() ;
	}

    /**
     * Get instance
     * @return DataTimer instance
     */
	public static clDataTimer getInstance(){

		if(instance==null)
			instance=new clDataTimer() ;

		return instance ;
	}

	/**
	 * Register datatimer listener. Listener cannot be registered when timer is already running
	 * @param listener listener to register
	 * @param periodMillisec period of event to occur
	 * @return true if register succesful, otherwise false
	 */
	public boolean registerListener(IDataTimerListener listener, long periodMillisec){

		if(!isRunning) {

			if(!dataTimerListenerList.contains(listener)) {
				dataTimerListenerList.add(listener);
				dataTimerListenerPeriodList.add(periodMillisec);
			}

			return true ;
		}

		return false ;

	}


    /**
     * Unregister listener
     * @param listener listener to unregister
     */
	public void unregisterListener(IDataTimerListener listener){

		for(int i=0;i<dataTimerListenerList.size();i++)
		{
			if(listener.equals(dataTimerListenerList.get(i)))
			{
				dataTimerListenerList.remove(i) ;
				dataTimerListenerPeriodList.remove(i) ;
			}
		}
	}

    /**
     * Start timing task.
     */
	public void start() {

        long minimumRequiredPeriod=adjustPeriod();//adjust period

        Log.i("Minimum Required Period",Long.toString( minimumRequiredPeriod)+"ms") ;

		dataTimerListenerPeriodCheckList=new long[dataTimerListenerList.size()] ;


		RepeatTask = new TimerTask() {
			@Override
			public void run() {

                for (int i = 0; i < dataTimerListenerPeriodList.size(); i++) {
                    //check if current count matches certain listener's event time
                    if (dataTimerListenerPeriodList.get(i) == dataTimerListenerPeriodCheckList[i]) {
                        dataTimerListenerList.get(i).onEveryElapseEvent();
                        dataTimerListenerPeriodCheckList[i] = 1;
                    } else
                        dataTimerListenerPeriodCheckList[i]++;


                }

            }
        } ;


		//create timer
		RepeatTimer = new Timer();
		//start task
		RepeatTimer.schedule(RepeatTask, 0, minimumRequiredPeriod) ;

		isRunning=true ;
	}

    /**
     * Stop timing task.
     */
	public void stop() {

		//cancel measuring task
		RepeatTimer.cancel() ;

		isRunning=false ;

		dataTimerListenerPeriodCheckList=null ;

	}

    /**
     * Adjust period and returns least common devisor
     */
    private long adjustPeriod(){

        minimumRequiredPeriod=findMinimumRequiredPeriod() ;
        //maximumRequiredPeriod=minimumRequiredPeriod ;

        //change datatimer period list
        for(int i=0;i<dataTimerListenerPeriodList.size();i++){

            dataTimerListenerPeriodList.set(i,dataTimerListenerPeriodList.get(i)/minimumRequiredPeriod) ;
        }

        return minimumRequiredPeriod ;
    }

    /**
     * find minimum required period
     * @return minimum required period
     */
	private long findMinimumRequiredPeriod(){

		int size=dataTimerListenerPeriodList.size() ;

		if(size>1)
			return gcd(dataTimerListenerPeriodList.toArray(new Long[size])) ;
        else if(size==1)
            return dataTimerListenerPeriodList.get(0) ;
        else
            return 0 ;

	}

    /**
     * find great common devisor among elements in array
     */

	private long gcd(Long[] array){

        //sort array in ascending order
		Arrays.sort(array) ;

        //if value before last value is not 0, which means gcd has been found
		if(array[array.length-2]!=0){

            int index=0 ;
            long min=-1 ;

            //find minimum index of value which is minumum and not 0
            for(int i=0;i<array.length;i++){

                if(array[i]!=0 && (min==-1 || array[i]<min))
                {
                    min=array[i] ;
                    index=i ;
                }
            }

			for(int i=index+1;i<array.length;i++){
				array[i]%=array[index] ;
			}

			return gcd(array) ;
		}

		return array[array.length-1] ;
	}

	public interface IDataTimerListener {
        void onEveryElapseEvent();
    }
}
