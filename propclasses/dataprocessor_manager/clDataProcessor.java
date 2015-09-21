//
//
//  Generated by StarUML(tm) Java Add-In
//
//  @ Project : Sleeper
//  @ File Name : clDataProcessor, clDataProcessor.clSleepStateClassifier, clDataProcessor.clDatabaseManager
//  @ Date : 2015-09-06
//  @ Author : Kang Shin Wook, Kim Hyun Woong, Kim Hyun Woo
//  @ Email : rkdtlsdnr102@naver.com


package org.androidtown.sleeper.propclasses.dataprocessor_manager;

//
//
//  Generated by StarUML(tm) Java Add-In
//
//  @ Project : Sleeper
//  @ File Name : clDataProcessor.java
//  @ Date : 2015-09-06
//  @ Author : Kang Shin Wook
//  @ Email : rkdtlsdnr102@naver.com

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import org.androidtown.sleeper.propclasses.com_manager.clComManager;
import org.jtransforms.fft.DoubleFFT_1D;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Abstract processor that classifies sleep stage. User can implement this class to make their
 * sleep controlling equation and device controlling using sleep stage output from stage classifier.
 *
 */
public abstract class clDataProcessor implements clComManager.IMessageListener {

    //protected int ID;
    //public abstract void onEveryCalculateCompleteEvent();
    private clDataTimer DataTimer=null ;
    protected Context AttachedContext=null ;
    private boolean isRunning=false ;
    private clSleepStageClassifier sleepClassifier =null ;
    private clDatabaseManager Database=null ;

   // protected clComManager ComManager=null ;

    /**
     * Constructor
     * @param context context of this app
     */
    protected clDataProcessor(Context context){

        DataTimer=clDataTimer.getInstance() ;
        //sManager=(SensorManager)context.getSystemService(Context.SENSOR_SERVICE) ;
        //create sleep state classifier
        sleepClassifier =new clSleepStageClassifier(context) ;
        AttachedContext=context ;

        Database=new clDatabaseManager(AttachedContext)  ;
    }

    /**
     * Start sleep tracking
     */
    public void measureStart(){

        isRunning=true ;

        SimpleDateFormat dateFormat = new SimpleDateFormat("yy-MM-dd");

        //put attributes in relation dataSummary
        ContentValues cv = new ContentValues() ;
        cv.put(clDatabaseManager.colDate,dateFormat.format(System.currentTimeMillis()));
        cv.put(clDatabaseManager.colStartTime,System.currentTimeMillis()) ;
        cv.put(clDatabaseManager.colEndTime,0) ;

        Database.insertSummaryTable(cv);

        //call attach function in sleep classifier
        sleepClassifier.attach();
        DataTimer.start() ;

    }

    /**
     * Stop sleep tracking
     */
    public void measureStop(){

        DataTimer.stop() ;
        //DataTimer.unregisterDataTimerListener(sleepStateClassifier);
        sleepClassifier.detach() ;
        isRunning=false ;

        SQLiteDatabase db=Database.getWritableDatabase();

        Cursor cursor = db.rawQuery("SELECT StartTime FROM DataTable_summary where _id = (select _id from DataTable_summary order by _id desc limit 1)", null);
        //update end time to row of datatable_summary table that was added at the start of this measure
        //SimpleDateFormat endTimeFormat = new SimpleDateFormat("HH:mm");
        cursor.moveToFirst() ;
        long startTime=cursor.getLong(0) ;
        long endTime=System.currentTimeMillis() ;

        cursor.close() ;

        //if tracked time is less than sleep state retrieve period, which means no data inserted
       if(endTime-startTime<=clSleepStageClassifier.SleepStateRetrievePeriod)
       {
           Log.i("measureStop","delete") ;
           Database.deleteSummaryTable(startTime);

       }else {//else

           Log.i("measureStop","update end") ;
           ContentValues cv = new ContentValues();
           cv.put(clDatabaseManager.colEndTime, System.currentTimeMillis());

           cursor = db.rawQuery("SELECT _id FROM DataTable_summary where _id = (select _id from DataTable_summary order by _id desc limit 1)", null);
           cursor.moveToFirst();
           String[] whereArgs = {cursor.getString(0)};
           Database.updateSummaryTable(cv, " _id=? ", whereArgs);
       }

        db.close() ;
    }

    /**
     * Get data timer
     * @return data timer
     */
    protected final clDataTimer getDataTimer(){

        return DataTimer ;
    }

    /**
     * Get sleep stage classifier
     * @return sleep stage classifier
     */
   protected final clSleepStageClassifier getSleepStageClassifier(){

       return sleepClassifier;
   }

    /**
     * Set user's own database manager. User should implement clDatabaseManager class and set instance
     * of your own database manager.
     * @param db database manager
     */
    /*
    protected void setDatabaseManager(clDatabaseManager db){

        Database=db ;
    }
    */

    /**
     *
     * @param position
     * @return
     */
    public abstract clStatManager createStatManager(int position) ;

    /**
     * Get database manager
     * @return database manager
     */
    public clDatabaseManager getDatabase(){

        return Database ;
    }

    /**
     * Classifies sleep stage using accelerometer sensor
     * it uses libsvm-androidjni, JTransform library
     */
    public static class clSleepStageClassifier{
        private List<ISleepStateListener> sleepStateListnerList;
        private List<Double> intensityList;
        //intensity array to avoid being incremented while calculate
        private List<double[]> fftMagnitudeList;
        //private float filtered_intensity;
        //private static final double lowpass_thr=2.5;
        //private static final double lowpass_dim=5;
        private static final double Intensity_Stddev_Thresh =4;
        //private static final double ZCM_Thresh=0.15 ;
        //private static final double SMALL_MOVEMENT=0.16 ;
       // private static final double LARGE_MOVEMENT=0.25 ;
        //private static final double LPF_WEIGHT=0.5 ;
        private static final int FFTBinSize =5 ;
        //intensity retrieve preiod in micro second
        private static final int IntensityValueRetrievePeriod =50 ;
        private static final long SleepStateRetrievePeriod=60000 ;
        private static final long FFTFeatureSampleRetrievePeriod=2000 ;

        private static final String dirPath= Environment.getExternalStorageDirectory()+"/MeasureResult" ;
        private static final String finename="result.txt" ;
        private static final String svmFilePath=Environment.getExternalStorageDirectory()+"/SvmFile" ;
        private static final String svmModelFilename="model" ;
        private static final String svmlibName="svmLib" ;
        // private static final String SvmTrainingFilename="train_set" ;

        //calculate fft feature at every 2 second, sleeptype classify at every 1 minute
        //private IDataTimerListener CalcFFTFeature=null ;
        private clDataTimer.IDataTimerListener ClassifySleepType=null ;

        private Context AttachedContext=null ;

        private double pim;
        //private double zcm;
        private double IntensityAvg ;
        private double[] fft_stddiv;
        private double[] fft_max;
        private int resultSleepState=-1 ;
       // private int intensityArrayLength=0 ;
        //private int smallMovementCnt=0 ;
       // private int largeMovementCnt=0 ;
        //private int slightMovementCnt=0 ;
        public static final int AWAKE = 2 ;
        public static final int REM = 1;
        public static final int DEEP = 0 ;

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
            double sumP2=0 ;
            double intensityValue=0 ;
            double intensityListSize=intensityArray.length ;
            //double std_deviation=0.0 ;

            for(int i=0;i<intensityListSize;i++)
            {
                intensityValue=intensityArray[i];

                sumP2=Math.pow(intensityValue,2) ;
                sum+=intensityValue ;
            }

            avg=sum/intensityListSize ;

            //calculate standard deviation
            //std_deviation=Math.sqrt(sumP2/intensityListSize-Math.pow(avg,2)) ;


            //remove noise between intensities if deviation is higher than standard deviation
            for(int i=0;i<intensityListSize;i++)
            {
                if(Math.abs(intensityArray[i]-avg)> clSleepStageClassifier.Intensity_Stddev_Thresh)
                    intensityArray[i]=0.0 ;
            }

        }

        /**
         * Performed at every one minute. Perform low-pass-filter on intensity values and replace elements
         * in intensity array.
         *
         * Deprecated since we already perform low-pass-filter like pre-processing when getting sensor
         * value. So intentsityArray is already filtered.
         * @param intensityArray intensity array to remove outlier
         * */
        /*
        @Deprecated
        private void removeOutlier(Double[] intensityArray) {

            //apply low pass filter concept
            double prevValue=0 ;
            double currentValue ;

            for(int i=0;i<intensityArray.length;i++){

                currentValue=intensityArray[i] ;

                intensityArray[i]=prevValue-LPF_WEIGHT*(currentValue-prevValue) ;

                prevValue=intensityArray[i] ;
            }

        }
        */


        /**
         * Performed at every one minute. Calculate sum of filtered intensity values.
         * @param intensityArray intensity array to calculate pim value.
         */
        private void estimatePIM(Double[] intensityArray) {

            pim=0 ;
            for(int i=0;i<intensityArray.length;i++)
                pim+=intensityArray[i] ;
        }

        /**
         * Performed at every one minute. Calculate count of zero crossing mode by using difference equation
         * on filtered intensity values.
         *
         * Deprecated since there is no zero crossing between intensity values since we treate x^2+y^2+z^2
         * value of accelerometer. And after we observed difference of this value between each sleep stage,
         * there was not much difference so we removed this feature. Maybe there's some mistake when we
         * calculate this feature and we will keep looking into it.
         * @param intensityArray intensity to get zcm value
         */
        /*
        @Deprecated
        private void estimateZCM(Double[] intensityArray) {

            zcm =0 ;
            double diff ;
            for(int i=1;i<intensityArray.length;i++){

                //get current sign of differnce of i,i-1 value
                diff=intensityArray[i]-intensityArray[i-1] ;

                //if 0 crossing occur
                if(Math.abs(diff)>ZCM_Thresh)
                {
                    zcm++ ;
                }

                //update prevDiffSign to current one
                //prevDiffSign=currentDiffSign ;
            }
        }
        */

        /**
         * Calculated every one minute. Estimate average of intensity values gathered for 1 minute.
         *
         * @param intensityArray intensity array to calculate average.
         */
        private void estimateIntensityAvg(Double[] intensityArray){

            IntensityAvg = 0;

            //calculate intensity average and store in file
            for (int i = 0; i < intensityArray.length; i++) {
                IntensityAvg += intensityArray[i];
            }
            IntensityAvg/=intensityArray.length ;

            Log.i("Intensity Average:", Double.toString(IntensityAvg)) ;
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
         * inverse fourier-transformed data
         * @return double type inverse data
         */
        /*
        @Deprecated
        private void calcIFFT(double[] toifft){

            DoubleFFT_1D doubleFFT_1D = new DoubleFFT_1D(toifft.length) ;

            doubleFFT_1D.realInverse(toifft, false);
        }
        */


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

            double magnitude=0.0 ;
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
            double[] eachMagnitudeArray=null ;
            double std_deviation=0.0 ;
            double maxCoefficient=0 ;


            for(int i=0;i< FFTBinSize;i++) {

                Log.i("fftMagnitudeListSize",Integer.toString(fftMagnitudeListSize)) ;

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

            Log.i("intensityArraySize",Integer.toString(intensityArray.length)) ;
            Log.i("fftFrameSize",Integer.toString(fftFrameSize)) ;
            //coordinate intensity length, discard ramaning after dividing with fftFrameSize
            int fftLength=intensityArray.length-(intensityArray.length%fftFrameSize) ;



            //calculate each fft feature for fftFrameSize
            for(i=0;i<fftLength;i+=fftFrameSize)
            {
                calcFFTFeature(intensityArray, i, i + fftFrameSize);
                Log.i("real fftFrameSize", Integer.toString(i)) ;

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


            //put features into vector
            //values[0][0]=IntensityAvg ;
            //indices[0][0]=1 ;
            //values[0][1]=pim ;
            //indices[0][1]=2 ;


            //values[0][22]=IntensityAvg ;
            //indices[0][22]=23 ;


            /*
            for(int i=0;i<FFTBinSize*2;i+=2){

                values[0][i+2]=fft_stddiv[i/2] ;
                indices[0][(i)+2]=(i+1)+2 ;
                values[0][(i+1)+2]=fft_max[i/2] ;
                indices[0][(i+1)+2]=(i+2)+2 ;
            }
            */

            for(int i=0;i<FFTBinSize*2;i+=2){

                values[0][i]=fft_stddiv[i/2] ;
                indices[0][(i)]=(i+1) ;
                values[0][(i+1)]=fft_max[i/2] ;
                indices[0][(i+1)]=(i+2) ;
            }



            /*
            String feature="" ;
            for(int i=0;i<feature_value_length;i++)
            {
                feature+=Double.toString(values[0][i]) ;
                feature+=" " ;
            }

            Log.i("Calculated Feature: ",feature) ;

            */
            doClassificationNative(values, indices, isProb, svmFilePath + "/" + svmModelFilename, labels, probs) ;

            return labels[0] ;
        }

        /**
         * attach data timer and accelerometer sensor
         */
        private void attach(){

            //set all settings
            set() ;


            //create classify timer listener. you can put extra preprocess function here
            //to add more feature to classify
            ClassifySleepType=new clDataTimer.IDataTimerListener() {

                @Override
                public void onEveryElapseEvent() {
                    //get values collected so far

                    final Double[] intensityArray = intensityList.toArray(new Double[intensityList.size()]);

                    //remove sensor noise
                    removeSensorNoise(intensityArray);

                    //estimate pim
                    estimatePIM(intensityArray);

                    estimateFFTFeature(intensityArray);


                    estimateIntensityAvg(intensityArray);

                    resultSleepState=classify() ;

                    makeResultFile(intensityArray);
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

                    reset() ;
                }

            } ;

            //register sensor event

            SensorManager sManager=(SensorManager)AttachedContext.getSystemService(Context.SENSOR_SERVICE) ;
            Sensor sensor=sManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION) ;
            //DataTimer.registerDataTimerListener(CalcFFTFeature,FFTFeatureSampleRetrievePeriod) ;

            clDataTimer.getInstance().registerDataTimerListener(ClassifySleepType, SleepStateRetrievePeriod) ;

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
         * Set settings
         */
        private void set(){

            intensityList= new CopyOnWriteArrayList<>() ;
            fftMagnitudeList=new ArrayList<>() ;

            //create SvmFile directory, svmFile
            fft_stddiv=new double[FFTBinSize] ;
            fft_max=new double[FFTBinSize] ;
        }

        /**
         * Detach this movement detector from data timer and other sensor
         */
        private void detach(){

           // DataTimer.unregisterDataTimerListener(CalcFFTFeature);
            clDataTimer.getInstance().unregisterDataTimerListener(ClassifySleepType);

            SensorManager sManager=(SensorManager)AttachedContext.getSystemService(Context.SENSOR_SERVICE) ;
            sManager.unregisterListener(sensorEventListener);
            sensorEventListener=null ;

            //reset all settings
            reset() ;

        }

        /**
         * Reset all settings
         */
        private void reset(){

            intensityList.clear() ;
            fftMagnitudeList.clear() ;

            //set all properties in fft_stddiv, fft_max to zero
            Arrays.fill(fft_stddiv,0);
            Arrays.fill(fft_max,0) ;
        }

        /**
         * Make result file after extracting each feature.
         *
         * This is for test code. When people use this class, this method will be removed.
         * @param intensityArray
         */
        private void makeResultFile(Double[] intensityArray) {

            File file = null;

            boolean isSuccess = false;

            File dir = new File(dirPath);
            //if directory not exists
            if (!dir.exists())
                dir.mkdir();

            file = new File(dirPath +"/"+ finename);

            try {

                FileOutputStream fos;
                fos = new FileOutputStream(file,true);

                //if file not exists
                if(!file.exists()) {
                    isSuccess = file.createNewFile();
                }

                boolean result;

                String file_content = null;

                SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");

                try {

                    //write storing time
                    fos.write((dateFormat.format(System.currentTimeMillis()) + "   ").getBytes());

                    //calculate intensity average and store in file
                    fos.write((Double.toString(IntensityAvg)+"   ").getBytes()) ;

                    fos.write((Integer.toString(intensityArray.length)+"   ").getBytes()) ;

                    fos.write((Integer.toString(resultSleepState)+"   ").getBytes()) ;

                    //write pim value
                    fos.write((Double.toString(pim) + "   ").getBytes());

                    //write zcm value
                    //fos.write((Double.toString(zcm) + "   ").getBytes());

                    //write move count of each level
                    //fos.write((Integer.toString(slightMovementCnt)+" "+Integer.toString(smallMovementCnt)+" "+Integer.toString(largeMovementCnt)+" ").getBytes()) ;

                    //write fft value, interleaving between fft_stddiv, fft_max
                    //length of both array is same


                    for (int i = 0; i < fft_stddiv.length; i++) {
                        fos.write((Double.toString(fft_stddiv[i]) + " ").getBytes());
                        fos.write((Double.toString(fft_max[i]) + " ").getBytes());
                    }

                    fos.write("\n".getBytes());

                    fos.flush();
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }

            } catch (IOException e) {
                e.printStackTrace();
            }

        }

        /**
         * Register to listen to sleep stage classification event.
         * @param sleepStateListener listener to listen classification event.
         */
        public void registerSleepStateListener(clSleepStageClassifier.ISleepStateListener sleepStateListener){

            if(!sleepStateListnerList.contains(sleepStateListener))
                sleepStateListnerList.add(sleepStateListener) ;
        }

        /**
         * Unregister from sleep stage classification event.
         * @param sleepStateListener listener that is registered.
         */
        public void unregisterSleepStateListener(clSleepStageClassifier.ISleepStateListener sleepStateListener){

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
            void onSleepStateRetrievedEvent(int sleepState, Double[] accelerometerValues);
        }
    }

    /**
     * Database manager that wraps up SQLite db helper. It has dataSummary table. New row is added
     * to this table when measure start, measure stop. Its '_id' column is used by user to identify
     * each table which would be named 'table1' when row with id 1 is inserted in dataSummary table
     * when measure started.
     */
    public static class clDatabaseManager extends SQLiteOpenHelper {


        private static final String dbName="SleeperDB";
        public static final String summarydataTable ="DataTable_summary" ;//@@@
        public static final String colCnt ="_id";//@@@
        public static final String colDate ="Date";//@@@
        public static final String colStartTime ="StartTime";//@@@
        public static final String colEndTime="EndTime" ;//@@@

        private List<IDbChangeListener> listenerList=null ;
        private  int count ;

        /**
         * Constructor
         * @param context context to attach
         */
        protected clDatabaseManager(Context context) {

            super(context, dbName, null, 1);

            listenerList=new ArrayList<>() ;
        }

        @Override
        public void onCreate(SQLiteDatabase db) {

            //create datatable_summary
            db.execSQL("CREATE TABLE " + summarydataTable + " (" + colCnt + " INTEGER PRIMARY KEY AUTOINCREMENT, " + //@@@
                    colDate + " TEXT, " + colStartTime + " INTEGER, " + colEndTime + " INTEGER " + ")"); //@@@
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        }

        @Override
        public void onOpen(SQLiteDatabase db) {
            super.onOpen(db);

            initialize(db) ;
        }

        /**
         * Initialize database. It keeps track of summary data table.
         * @param db SQLite database object
         */
        private void initialize(SQLiteDatabase db){


            String query = "SELECT  * FROM " + clDatabaseManager.summarydataTable + " order by "+ clDatabaseManager.colCnt +" desc limit 1";
            Cursor cursor = db.rawQuery(query, null);

            if(cursor.moveToFirst())//if query fails, when there is no column made in dataTable_summary
            {
                count = cursor.getInt(0) ;
            }
            else
                count=0;
        }

        /**
         * Add main database listener
         * @param listener listener to add
         */
        public void addListener(IDbChangeListener listener){

            if(!listenerList.contains(listener)){

                listener.setDB(this) ;
                listenerList.add(listener) ;
            }
        }

        /**
         * Remove listener from main database
         * @param listener listener to remove
         */
        public void removeListener(IDbChangeListener listener){

            if(listenerList.contains(listener)){

                listenerList.remove(listener) ;
            }
        }

        /**
         * Insert into Datatable_Summary table
         * @param cv content values to put in new row of datatable summary table
         */
        public void insertSummaryTable(ContentValues cv){

            SQLiteDatabase db=this.getWritableDatabase() ;

            count++ ;//increment count since count indicate current end row of table

            cv.put(colCnt, count) ;//put incremented count
            db.insert(clDatabaseManager.summarydataTable, null, cv) ;

            //increment count after inserting new row into datatable summary table

            for(IDbChangeListener listener : listenerList)
                listener.onInsertSummaryTable(count);

            db.close() ;
        }

        /**
         * Update summary table
         * @param cv content values
         * @param where where clause
         * @param whereArgs where arguments
         * @return number of rows affected from update query
         */
        public int updateSummaryTable(ContentValues cv, String where, String[] whereArgs) {

            int upColCnt ;

            SQLiteDatabase db=this.getWritableDatabase();

            upColCnt=db.update(clDatabaseManager.summarydataTable,cv,where,whereArgs) ;

            db.close() ;

            return upColCnt ;
        }

        /**
         * Delete one row by unique startTime
         * @param startTime start time to identify row
         */
        public void deleteSummaryTable(long startTime){

            SQLiteDatabase db=getWritableDatabase() ;
            String[] colNames={colCnt} ;
            String where=colStartTime+"=?" ;
            String[] whereArgs={Long.toString(startTime)} ;
            Cursor cursor=db.query(summarydataTable, colNames, where, whereArgs,null,null,null) ;

            cursor.moveToFirst() ;

            int rowId=cursor.getInt(0) ;

            db.delete(summarydataTable, where, whereArgs) ;

            Log.i("To delete Row id", Integer.toString(rowId)) ;

            for(IDbChangeListener listener : listenerList)
                listener.onDeleteSummaryTable(rowId);
        }

        /**
         * Listener to main database change
         */
        public interface IDbChangeListener{

            void setDB(SQLiteOpenHelper db) ;

            /**
             * Event occurs when inserting new row into datatable_summary table. Inserting to dataSummary table
             * occurs when tracking starts. So user's database will implement this method to make their own database table
             * synchronize with dataSummary table.
             *
             * For example, every time sleep tracking mode starts, user have to create new table to store information
             * of that each tracking. So when this event is called, user can create table with number that is equal to
             * number of summary table's last inserted row, like your_table_name1 when row number 1 is newly added.
             * @param rowId row index that is newly added to datatable_summary table
             */
            void onInsertSummaryTable(int rowId) ;

            /**
             * Event occurs when deleting row. It is same as onInsertSummaryTable except it occurs when
             * deleting.
             * @param rowId row id that was deleted from dataSummary table
             */
            void onDeleteSummaryTable(int rowId) ;
        }

    }
}