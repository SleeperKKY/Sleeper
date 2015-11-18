//
//
//  Generated by StarUML(tm) Java Add-In
//
//  @ Project : Sleeper
//  @ File Name : clDataProcessor, clDataProcessor.clSleepStateClassifier, clDataProcessor.clDatabaseManager
//  @ Date : 2015-09-06
//  @ Author : Kang Shin Wook, Kim Hyun Woong, Kim Hyun Woo
//  @ Email : rkdtlsdnr102@naver.com
package org.sleeper.propclasses.dataprocessor_manager;

//
//
//  Generated by StarUML(tm) Java Add-In
//
//  @ Project : Sleeper
//  @ File Name : clDataProcessor.java
//  @ Date : 2015-09-06
//  @ Author : Kang Shin Wook, Kim Hyun Woong
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
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.widget.ArrayAdapter;

import org.jtransforms.fft.DoubleFFT_1D;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Abstract processor that classifies sleep stage. User can implement this class to make their
 * sleep controlling equation and device controlling using sleep stage output from stage classifier.
 *
 */
public class clDataProcessor{

    private clDataTimer DataTimer=null ;
    protected Context AttachedContext=null ;
    private boolean isRunning=false ;
    private clSleepStageClassifier sleepClassifier =null ;

    private static clDataProcessor inst=null ;//data processor instance
    private List<IDataProcessor> dpListenerList=null ;//data processor interface list

    /**
     * Constructor
     * @param context context of this app
     */
    private clDataProcessor(Context context){

        DataTimer= clDataTimer.getInstance() ;
        //create sleep state classifier
        sleepClassifier =new clSleepStageClassifier(context) ;
        AttachedContext=context ;

        dpListenerList=new ArrayList<>();
    }

    public static clDataProcessor getInstance(Context context){

        if(inst==null)
            inst=new clDataProcessor(context) ;

        return inst ;
    }

    /**
     * Start sleep tracking
     */
    public void measureStart(){

        int i ;
        isRunning=true ;

        //call attach function in sleep classifier
        sleepClassifier.attach();

        for(i=0;i<dpListenerList.size();i++)
            dpListenerList.get(i).onMeasureStart(sleepClassifier);

        DataTimer.start() ;

    }

    /**
     * Stop sleep tracking
     */
    public void measureStop(){

        int i ;

        DataTimer.stop() ;
        sleepClassifier.detach() ;

        for(i=0;i<dpListenerList.size();i++)
            dpListenerList.get(i).onMeasureStop(sleepClassifier);
        isRunning=false ;
    }

    public void registerListener(IDataProcessor dpListener){

        if(!dpListenerList.contains(dpListener))
            dpListenerList.add(dpListener) ;
    }

    public void unregisterListener(IDataProcessor dpListener){

        if(dpListenerList.contains(dpListener))
            dpListenerList.remove(dpListener) ;
    }

    /**
     * Get data timer
     * @return data timer
     */
    public final clDataTimer getDataTimer(){

        return DataTimer ;
    }

    /**
     * Get sleep stage classifier
     * @return sleep stage classifier
     */
   public final clSleepStageClassifier getSleepStageClassifier(){

       return sleepClassifier;
   }

    /**
     * Interface for dataprocessor
     */
    public interface IDataProcessor{

        /**
         * Called when measure starts
         * @param sleepStageClassifier sleep stage classifier
         */
        void onMeasureStart(final clSleepStageClassifier sleepStageClassifier) ;
        /**
         * Called when measure stops
         * @param sleepStageClassifier sleep stage classifier
         */
        void onMeasureStop(final clSleepStageClassifier sleepStageClassifier) ;
    }

    /**
     * Classifies sleep stage using accelerometer sensor
     * it uses libsvm-androidjni, JTransform library
     */
    public static class clSleepStageClassifier{
        private List<ISleepStateListener> sleepStateListnerList;
        private List<Double> intensityList;
        //intensity array to avoid being incremented while calculate

        //to remove sensor noise
        private static final double Intensity_dev_Thresh =4;
        //intensity retrieve preiod in micro second
        private static final int IntensityValueRetrievePeriod =50 ;
        private static final long SleepStateRetrievePeriod=60000 ;
        private static final long FFTFeatureSampleRetrievePeriod=2000 ;

        private static final String dirPath= Environment.getExternalStorageDirectory()+"/MeasureResult" ;
        private static final String finename="result.txt" ;
        private static final String svmFilePath=Environment.getExternalStorageDirectory()+"/SvmFile" ;
        private static final String svmModelFilename="model" ;
        private static final String svmlibName="svmLib" ;

        //calculate fft feature at every 2 second, sleeptype classify at every 1 minute
        private clDataTimer.IDataTimerListener ClassifySleepType=null ;

        private Context AttachedContext=null ;

        //fft feature containser
        private List<double[]> fftMagnitudeList;
        private double[] fft_stddiv;
        private double[] fft_max;
        private static final int FFTBinSize =5 ;

        //sleep state
        public static final int AWAKE = 2 ;
        public static final int REM = 1;
        public static final int DEEP = 0 ;

        private int resultSleepState=-1 ;

        //private int intensityList_offset=0 ;
        private SensorEventListener sensorEventListener=null ;

        //declare svm interface function
        // svm native

        // Load the native library
        static {
            System.loadLibrary(svmlibName);
        }

        // svm native, classification function
        private native int doClassificationNative(double values[][], int indices[][],
                                                  int isProb, String modelFile, int labels[], double probs[]);

        /**
         * Constructor
         * @param context context to attach
         */
        private clSleepStageClassifier(Context context) {

            AttachedContext=context ;
            sleepStateListnerList= new ArrayList<>() ;
        }

        /**
         * Performed at every one minute. Remove intensity values in epoch where statndard deviation of that epoch
         * is bigger than certain value C.
         * @param intensityArray intensity array to remove noise
         */
        private void removeSensorNoise(Double[] intensityArray) {

            double avg=0.0 ;
            double sum=0 ;
            double intensityValue=0 ;
            double intensityListSize=intensityArray.length ;
            //double std_deviation=0.0 ;

            for(int i=0;i<intensityListSize;i++)
            {
                intensityValue=intensityArray[i];

                sum+=intensityValue ;
            }

            avg=sum/intensityListSize ;

            //remove noise between intensities if deviation is higher than standard deviation
            for(int i=0;i<intensityListSize;i++)
            {
                if(Math.abs(intensityArray[i]-avg)> clSleepStageClassifier.Intensity_dev_Thresh)
                    intensityArray[i]=0.0 ;
            }

        }


        /**
         * calculate fast fourier transform
         * @return double type array that contains transformed data
         */
        private void calcFFT(double[] tofft){

            DoubleFFT_1D doubleFFT_1D=new DoubleFFT_1D(tofft.length) ;

            //perform fast fourier transform on intensity values
            doubleFFT_1D.realForward(tofft);

        }

        /**
         * calculated every 2 second
         *
         * @param intensityArray*/
        private void calcFFTFeature(Double[] intensityArray, int start, int end) {

            double[] tofft= new double[end-start] ;

            for(int i=start;i<end;i++)
            {
                tofft[i-start]=intensityArray[i] ;
            }

            calcFFT(tofft) ;

            double magnitude ;
            //int magnitudeArraySize = FFTBinSize ;
            double[] magnitudeArray=new double[FFTBinSize] ;
            int incrementBy=(int)Math.ceil((double)(end-start)/FFTBinSize) ;

            for(int i=0,k=0;i<FFTBinSize;i++,k+=incrementBy)
            {
                magnitude=Math.sqrt(Math.pow(tofft[k], 2) + Math.pow(tofft[k + 1], 2)) ;

                magnitudeArray[i]=magnitude ;
            }

            //add each magnitude array into fft magnitude list
            fftMagnitudeList.add(magnitudeArray) ;
        }

        /**
         * performed at every one minute.
         * calculate standard deviation and max of each coefficient of filtered fft which were retrieved at every 2 second.
         *
         * For example, if low-pass-filter threshold was 2.5Hz then it will have 0.1~2.4Hz frequency, which results in 24 coefficients.
         * It will be added to the list at every 2 second until it is 1 minute so there will be 30 samples for each coefficient.Finally Calculate standard deviation and max values of each coefficients at every 1 minute.
         **/
        private void estimateFFT_SIGMA_MAX() {

            double avg=0.0 ;
            double sum=0 ;
            double sumP2=0 ;
            double magnitude=0 ;
            int fftMagnitudeListSize=fftMagnitudeList.size() ;
            double std_deviation=0.0 ;
            double maxCoefficient=0 ;


            for(int i=0;i< FFTBinSize;i++) {

                //Log.i("fftMagnitudeListSize",Integer.toString(fftMagnitudeListSize)) ;

                for (int j = 0; j < fftMagnitudeListSize; j++) {


                    magnitude=fftMagnitudeList.get(j)[i] ;

                    sumP2 += Math.pow(magnitude, 2);
                    sum += magnitude;

                    //change if magnitude which bigger than current max is found
                    if (maxCoefficient < magnitude)
                        maxCoefficient = magnitude;

                }

                avg = sum / fftMagnitudeListSize;

                //calculate standard deviation
                std_deviation = Math.sqrt(sumP2 / fftMagnitudeListSize - Math.pow(avg, 2));

                fft_stddiv[i]=std_deviation ;
                fft_max[i]=maxCoefficient ;

                //set maxCoefficient,sumP2,sum to 0
                maxCoefficient=0 ;
                sumP2=0 ;
                sum=0 ;

            }
        }

        /**
         * Calculate fft frequency feature of intensity array.
         *
         * Deprecated since svm doesn't classify sleep stage well when this feature is practiced along
         * pim, intensity average. Maybe there is some mistake on how we extract this feature so we will
         * keep looking into it.
         * @param intensityArray intensity array to calculate fft feature.
         */
        private void estimateFFTFeature(Double[] intensityArray){

            int fftFrameSize=intensityArray.length/(int)(SleepStateRetrievePeriod/FFTFeatureSampleRetrievePeriod) ;
            int i=0 ;

            //coordinate intensity length, discard ramaning after dividing with fftFrameSize
            int fftLength=intensityArray.length-(intensityArray.length%fftFrameSize) ;



            //calculate each fft feature for fftFrameSize
            for(i=0;i<fftLength;i+=fftFrameSize)
            {
                calcFFTFeature(intensityArray, i, i + fftFrameSize);
            }

            //estimate fft sigma max feature
            estimateFFT_SIGMA_MAX();
        }

        /**
         * classify sleep state using fft, pim, zcm features
         * row of double array is 1 since features are input at a same time
         * @return
         */
        private int classify() {

            //int feature_value_length=FFTBinSize*2+1+1+1 ;//2 fft feature array: max, standard deviation , PIM 1, ZCM 1, IntensityAvg 1
            int feature_value_length=FFTBinSize*2 ;
            double[][] values=new double[1][feature_value_length] ;
            int[][] indices=new int[1][feature_value_length] ;
            int[] labels=new int[1] ;
            double[] probs = new double[4];
            int isProb = 0; // Not probability prediction

            for(int i=0;i<FFTBinSize*2;i+=2){

                values[0][i]=fft_stddiv[i/2] ;
                indices[0][(i)]=(i+1) ;
                values[0][(i+1)]=fft_max[i/2] ;
                indices[0][(i+1)]=(i+2) ;
            }

            doClassificationNative(values, indices, isProb, svmFilePath + "/" + svmModelFilename, labels, probs) ;

            return labels[0] ;
        }

        /**
         * attach data timer and accelerometer sensor
         */
        private void attach(){

            //set all settings
            intensityList= new CopyOnWriteArrayList<>() ;
            fftMagnitudeList=new ArrayList<>() ;

            //create SvmFile directory, svmFile
            fft_stddiv=new double[FFTBinSize] ;
            fft_max=new double[FFTBinSize] ;


            //create classify timer listener. you can put extra preprocess function here
            //to add more feature to classify
            ClassifySleepType=new clDataTimer.IDataTimerListener() {

                @Override
                public void onEveryElapseEvent() {
                    //get values collected so far

                    final Double[] intensityArray = intensityList.toArray(new Double[intensityList.size()]);

                    //remove sensor noise
                    removeSensorNoise(intensityArray);

                    estimateFFTFeature(intensityArray);

                    resultSleepState=classify() ;
                    //classify sleep type from pim, zcm, fft feature

                    for (final ISleepStateListener listener : sleepStateListnerList) {

                        /**
                         * create new handler on main thread's message queue since it it should not
                         * delay timer task
                         */
                        new Handler(Looper.getMainLooper()).post(new Runnable() {

                            @Override
                            public void run() {

                                listener.onSleepStateRetrievedEvent(resultSleepState, intensityArray);
                            }
                        });
                    }

                    //clear all arrays
                    intensityList.clear() ;
                    fftMagnitudeList.clear() ;

                    //set all properties in fft_stddiv, fft_max to zero
                    Arrays.fill(fft_stddiv,0);
                    Arrays.fill(fft_max, 0) ;
                }

            } ;

            //register sensor event

            SensorManager sManager=(SensorManager)AttachedContext.getSystemService(Context.SENSOR_SERVICE) ;
            Sensor sensor=sManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION) ;

            clDataTimer.getInstance().registerListener(ClassifySleepType, SleepStateRetrievePeriod) ;

            //register accelerometer sensor listener
            sensorEventListener=new SensorEventListener() {

                @Override
                public void onSensorChanged(SensorEvent event) {

                    intensityList.add(Math.sqrt(
                            Math.pow(event.values[0], 2) +
                                    Math.pow(event.values[1], 2) +
                                    Math.pow(event.values[2], 2)
                    )) ;
                }

                @Override
                public void onAccuracyChanged(Sensor sensor, int accuracy) {

                }
            } ;

            sManager.registerListener(sensorEventListener, sensor, IntensityValueRetrievePeriod * 1000) ;
        }

        /**
         * Detach this movement detector from data timer and other sensor
         */
        private void detach(){

            clDataTimer.getInstance().unregisterListener(ClassifySleepType);

            SensorManager sManager=(SensorManager)AttachedContext.getSystemService(Context.SENSOR_SERVICE) ;
            sManager.unregisterListener(sensorEventListener);
            sensorEventListener=null ;
            ClassifySleepType=null ;
        }

        /**
         * Register to listen to sleep stage classification event.
         * @param sleepStateListener listener to listen classification event.
         */
        public void registerListener(clSleepStageClassifier.ISleepStateListener sleepStateListener){

            if(!sleepStateListnerList.contains(sleepStateListener))
                sleepStateListnerList.add(sleepStateListener) ;
        }

        /**
         * Unregister from sleep stage classification event.
         * @param sleepStateListener listener that is registered.
         */
        public void unregisterListener(clSleepStageClassifier.ISleepStateListener sleepStateListener){

            if(sleepStateListnerList.contains(sleepStateListener))
                sleepStateListnerList.remove(sleepStateListener) ;
        }

        /**
         * Sleep state listener interface
         */
        public interface ISleepStateListener {

            /**
             * Fired when sleep state is retrieved.
             * @param sleepState classified sleep stage
             * @param accelerometerValues accelerometer values array gathered for certain times.
             */
            void onSleepStateRetrievedEvent(int sleepState, final Double[] accelerometerValues);
        }
    }
}