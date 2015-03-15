package com.google.android.glass.sample.klabinterface;

import com.google.android.glass.timeline.LiveCard;
import com.google.android.glass.timeline.LiveCard.PublishMode;

import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.util.Log;
import android.view.animation.Animation;

import com.google.android.glass.timeline.LiveCard;
import com.googlecode.charts4j.AxisLabelsFactory;
import com.googlecode.charts4j.Data;
import com.googlecode.charts4j.GCharts;
import com.googlecode.charts4j.LineChart;
import com.googlecode.charts4j.Plot;
import com.googlecode.charts4j.Plots;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.GraphViewSeries;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.LineGraphView;


import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.nio.channels.FileLock;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;


/**
 * Service owning the LiveCard living in the timeline.
 */
public class MainService extends Service {
    /** TAG associated to the LiveCard */
    private static final String LIVE_CARD_TAG = "BWH interface";
    /** TAG associated to the Menu view   */
    private static final String MENU_TAG = "Menu";
    /** TAG associated to the Temperature view  */
    private static final String TEMPERATURE_TAG = "Temperature";
    /** TAG associated to the PH view  */
    private static final String PH_TAG = "pH";
    /** TAG associated to the Video view   */
    private static final String VIDEO_TAG = "Video";
    /** TAG associated to the Beating view   */
    private static final String BEATING_TAG = "Beating";
    /** TAG associated to the Electrovalves view */
    private static final String ELECTROVALVES_TAG = "Electrovalve";

    private Bitmap bmp;

    /** URL where the image is stored   */
    private static final String URL_IMAGE = 
	                 "http://CENSURED/picture/view";



    /** Runnable which describes the task to download the Beating's graph */
    private final ImageDownloader mImageDownloader = 
	                     new ImageDownloader(URL_IMAGE, this);
    /** Action is an enumerate used to implement switch-case for the extra 
	  * text appended to the intent  */
    private static enum Action
    {
        Menu, Temperature, pH, Video, Beating, Electrovalves
    }

    private AppDrawer mCallback;
    private LiveCard mLiveCard;

    /** HandlerThread used to launch a background thread that manages the 
	  * Data updating */
    private HandlerThread mHandlerThread;
    /** Handler used to launch a background thread that manages the Data updating*/
    private Handler mHandler;

    /** INT associated to the Menu view request  */
    private static final int MENU = 0;
    /** INT associated to the PH view request  */
    private static final int PH = 1;
    /** INT associated to the Menu view request  */
    private static final int TEMPERATURE = 2;
    /** INT associated to the Video view request  */
    private static final int VIDEO = 3;
    /** INT associated to the Beating view request  */
    private static final int BEATING = 4;

    /** updating data period (in ms)  */
    private static final long DATA_UPDATE_DELAY_MILLIS = 500;
    /** updating graph period (in ms)  */
    private static final long GRAPH_UPDATE_DELAY_MILLIS = 500;
    /** updating video period (in ms)  */
    private static final long FRAME_TIME_MILLIS = 100;
    private static final long VIDEO_UPDATE_DELAY_MILLIS = 60*1000; //time for video

    /** Runnable which describes the task to update the pH and Temperature 
	  * sensors values */
    private final UpdateSensorValuesRunnable mUpdateSensorValuesRunnable = 
	                                        new UpdateSensorValuesRunnable();
    /** Runnable which describes the task to compute the pH and Temperature 
	  * graph (starting from their value)  */
    private final UpdateSensorGraphsRunnable mUpdateSensorGraphsRunnable = 
	                                        new UpdateSensorGraphsRunnable();

    private final UpdateMicroscopeVideoRunnable mUpdateMicroscopeVideoRunnable = 
	                                         new UpdateMicroscopeVideoRunnable();
											  
	 /** Runnable which describes the task to update the Electrovalves status */
    private final UpdateElectrovalvesStatus mUpdateElectrovalvesStatus = 
	                                             new UpdateElectrovalvesStatus();


    private static final String VIDEO_FILE_NAME = 
	            Environment.getExternalStorageDirectory()+"/microscope_video.mp4";
    private static final String TEMP_VIDEO_FILE_NAME = 
	       Environment.getExternalStorageDirectory()+"/temp_microscope_video.mp4";

    /** String array for the sensors (PH and Temperature) */
    private String[] mSensors = new String[]{PH_TAG, TEMPERATURE_TAG};

    //          Hash table used for creating the graphs                             */
    /** Hash table in which the sensors values are stored  */
    private Map<String,Double> mCurrentSensorValues;
    /** Hash table in which the graphs points are stored  */
    private Map<String, ArrayList<DataPoint>> mSensorGraphData;
    /** Hash table in which the sensors graphs are stored */
    private Map<String,Bitmap> mCurrentSensorGraphs;

    private Map<String,Double> mSensorAverage;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (mLiveCard == null) {
            AppManager.getInstance().setState(MENU);
            mLiveCard = new LiveCard(this, LIVE_CARD_TAG);

            // Keep track of the callback to remove it before unpublishing.
            mCallback = new AppDrawer(this);
            mLiveCard.setDirectRenderingEnabled(true).getSurfaceHolder().
			                                         addCallback(mCallback);

            Intent menuIntent = new Intent(this, MenuActivity.class);
            menuIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | 
			                     Intent.FLAG_ACTIVITY_CLEAR_TASK);
            mLiveCard.setAction(PendingIntent.getActivity(this, 0, menuIntent, 0));
            mLiveCard.attach(this);
            mLiveCard.publish(PublishMode.REVEAL);


             /* Launch the task to update the data in another thread  */
            mHandlerThread = new HandlerThread("myHandlerThread");
            mHandlerThread.start();
            mHandler = new Handler(mHandlerThread.getLooper());
            DataTask task = new DataTask();
            task.execute(this);
        } else {
            if(intent.getStringExtra(Intent.EXTRA_TEXT)!= null) {
                Action action = Action.valueOf(intent.getStringExtra
				                                  (Intent.EXTRA_TEXT));
                switch (action) {
                    case Menu:
                        Log.i(LIVE_CARD_TAG, "State = Menu");
                        AppManager.getInstance().setState(MENU);
                        break;
                    case Beating:
                        Log.i(LIVE_CARD_TAG, "State = Beating");
                        AppManager.getInstance().setState(BEATING);
                        break;
                    case pH:
                        Log.i(LIVE_CARD_TAG, "State = pH");
                        AppManager.getInstance().setState(PH);
                        break;
                    case Temperature:
                        Log.i(LIVE_CARD_TAG, "State = Temperature");
                        AppManager.getInstance().setState(TEMPERATURE);
                        break;
                    case Video:
                        Log.i(LIVE_CARD_TAG, "State = Video");
                        AppManager.getInstance().setState(VIDEO);
                        break;
					case Electrovalves:
					    Log.i(LIVE_CARD_TAG, "State = Electrovalves");
						AppManager.getInstance().setState(ELECTROVALVES);
						break;
                    default:
                        mLiveCard.navigate();
                        break;
                }
            }
            else
            {
                mLiveCard.navigate();
            }
        }

        // Return START_NOT_STICKY to prevent the system from restarting the 
        // service if it is killed (e.g., due to an error). 
        // It doesn't make sense to restart automatically because the
        // stopwatch state will have been lost.
        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        // Stop the Task which update the beating image
        if(!mImageDownloader.isStopped())
        {
            mImageDownloader.setIsStopped(true);
            Log.i(BEATING_TAG,"Removed Task");
        }
        // Stop the Task which update the sensors Graphs
        if(!mUpdateSensorGraphsRunnable.isStopped())
        {
            mUpdateSensorGraphsRunnable.setStop(true);
            Log.i(PH_TAG + " " + TEMPERATURE_TAG,"Removed Task of Graphs");
        }
        // Stop the Task which update the sensors value
        if(!mUpdateSensorValuesRunnable.isStopped())
        {
            mUpdateSensorValuesRunnable.setStop(true);
            Log.i(PH_TAG + " " + TEMPERATURE_TAG,"Removed Task of Values");
        }
        if (!mUpdateMicroscopeVideoRunnable.isStopped())
        {
            mUpdateMicroscopeVideoRunnable.setStop(true);
            Log.i(VIDEO_TAG, "Removed Task of Uploading values");
        }
		if (!mUpdateElectrovalvesStatus.isStopped())
        {
            mUpdateElectrovalvesStatus.setStop(true);
            Log.i(ELECTROVALVES_TAG, "Removed Task for getting 
			                          Electrovalves status");
        }
        if (mLiveCard != null && mLiveCard.isPublished()) {
            mLiveCard.unpublish();
            mLiveCard = null;
        }
        super.onDestroy();
    }

    /**
     * Asynchronous Task for downloading the graphs and video in background
     */
    private class DataTask extends AsyncTask<MainService,Void,Void>
    {

        @Override
        protected Void doInBackground(MainService... params) {

            Log.i(LIVE_CARD_TAG, "Loading initial data");

            // create the three hash tables
            mCurrentSensorValues = new HashMap<String, Double>();
            mSensorGraphData = new HashMap<String, ArrayList<DataPoint>>();
            mCurrentSensorGraphs = new HashMap<String, Bitmap>();
            mSensorAverage = new HashMap<String, Double>();

            // initializes each hash map with dummy values
            for (String mSensor : mSensors)
            {
                mCurrentSensorValues.put(mSensor, 0.0);
                mSensorAverage.put(mSensor, 0.0);
                mSensorGraphData.put(mSensor, new ArrayList<DataPoint>());
                mCurrentSensorGraphs.put(mSensor, null);
            }

            Log.i(LIVE_CARD_TAG, "Download the data");

            mUpdateSensorValuesRunnable.run();
            // give the hash table to the Callback
            // mCallback.setSensorValues(mCurrentSensorValues);
            // compute the graphs with previous values and give them to 
		    // the AppDrawer
            mUpdateSensorGraphsRunnable.run();

            // give the hash table to the Callback
            // mCallback.setSensorGraphs(mCurrentSensorGraphs);
            // download the beating image and give this to the AppDrawer
            mImageDownloader.run();

            mUpdateMicroscopeVideoRunnable.run();
			
			mUpdateElectrovalvesStatus.run();
            return null;
        }
    }

    /**
     * This runnable updates the sensors values taking them from the google engine
     */
    private class UpdateSensorValuesRunnable implements Runnable
    {
        private boolean mIsStopped = false;

        /** It implements the task of runnable
         *
         * @see UpdateSensorValuesRunnable
         */
        @Override
        public void run()
        {
            if (!isStopped())
            {
                /** JavaScript object, where the sensor values are 
				  * temporary stored */
                JSONObject values = getSensorValues();
                if (values != null)
                {
                    for (String mSensor : mSensors)
                    {
                        try
                        {
                            // update the hash table of sensor values
                            mCurrentSensorValues.put(mSensor, 
							                 values.getDouble(mSensor));

                        }
                        catch (JSONException ignored)
                        {}
                    }
                }

                // restart the Runnable after a given amount of time
                mHandler.postDelayed(mUpdateSensorValuesRunnable,
                        				DATA_UPDATE_DELAY_MILLIS);
            }
        }



        /** It is the getter function that shows the status of runnable
         *
         * @return mIsStopped, true if the runnable is stopped, false otherwise
         */
        public boolean isStopped()
        {
            return mIsStopped;
        }

        /** It is the setter that allows to stop the runnable
         *
         * @param isStopped, true if the user wishes to stop the runnable, 
		 * false otherwise
         */
        public void setStop(boolean isStopped)
        {
            this.mIsStopped = isStopped;
        }
    }

    /** It gets the values of all of the sensors
     *
     * @return JSONOObject which contains all the values of sensors
     */
    private JSONObject getSensorValues()
    {
        try
        {
            String values = 
			      getURL("http://CENSURED/sensor_values");
            // return a JSONObject constructed by JSONTokener, which takes 
			// a source string and extracts characters and tokens from it
            return new JSONObject(new JSONTokener(values));
        }
        catch (Exception e)
        {
            Log.e(LIVE_CARD_TAG,"Failed to get sensor values",e);
            return null;
        }
    }


    // Get the new data points that we will need to graph

    /** This function computes the points that have to be plotted
     *
     * @param sensor , the name of sensor (pH or Temperature)
     * @param last_timestamp , the previous value of timestamp
     * @return JSONArray, return a list of values that corresponds to the 
	 *   points that have to be plotted
     */
    private JSONArray getDataPoints(String sensor, double last_timestamp)
    {
        try
        {
            String url = 
			  "http://CENSURED/graphing_data?sensor=" + 
			   sensor + "&last_timestamp=" + String.valueOf(last_timestamp);
            String values = getURL(url);
            return new JSONArray(new JSONTokener(values));
        }
        catch (Exception e)
        {
            Log.e(LIVE_CARD_TAG,"Failed to get data points",e);
            return null;
        }
    }

    /** This method gets data from the given url website
     *
     * @param _url, url in which the data are contained
     * @return String, contained data from the given url
     * @throws IOException if an IO exception occurred during the download
     */
    private String getURL(String _url) throws IOException
    {
        URL url = new URL(_url);
        InputStream is = url.openStream();
        int ptr;
        StringBuffer buffer = new StringBuffer();
        while ((ptr = is.read()) != -1)
        {
            buffer.append((char)ptr);
        }
        return buffer.toString();
    }

    /** This runnable updates the graphs of pH and Temperature    */
    private class UpdateSensorGraphsRunnable implements Runnable {
        private boolean mIsStopped = false;
        public void run()
        {
            if (!isStopped())
            {
                // Loop through each of the sensors
                for (String curr_sensor : mSensors) {
                    ArrayList<DataPoint> curr_data = 
					        mSensorGraphData.get(curr_sensor);
                    double lastTimestamp = curr_data.size() > 0 ? 
					  curr_data.get(curr_data.size() - 1).getTimestamp() : 0.;
                    // Get a list of the new data points for this sensor
                    JSONArray newPoints = getDataPoints(curr_sensor, lastTimestamp);
                    for (int j = 0; j < newPoints.length(); j++) {
                        // Save each data point
                        try {
                            JSONObject point = newPoints.getJSONObject(j);
                            double timestamp = (Double) point.get("timestamp");
                            double value = (Double) point.get("value");
                            curr_data.add(new DataPoint(timestamp, value));
                        } catch (JSONException e) {
                            Log.e(LIVE_CARD_TAG, "JSON error", e);
                        }
                    }
                    // Clear out points that are over an hour old
                    while (true) {
                        if (curr_data.size() > 0 && curr_data.get(0).getTimestamp()
							         < (curr_data.get(curr_data.size() - 1)
								     .getTimestamp()) - 3600) {
                            Log.i(LIVE_CARD_TAG, "Deleting old data point");
                            curr_data.remove(0);
                        } else {
                            break;
                        }
                    }
                    // If there are no points don't show a graph
                    if (curr_data.size() == 0) {
                        continue;
                    }
                    // Store the timestamps and values in separate arrays to graph
                    ArrayList<Double> timestamps = new ArrayList<Double>();
                    ArrayList<Double> values = new ArrayList<Double>();
                    for (DataPoint curr_point : curr_data) {
                        timestamps.add(curr_point.getTimestamp());
                        values.add(curr_point.getValue());
                    }

                    mSensorAverage.put(curr_sensor,computeAverage(values));
                    // Scale the timestamp data
                    double maxTimestamp = Collections.max(timestamps);
                    double minTimestamp = Collections.min(timestamps);
                    maxTimestamp *= 1.2;
                    minTimestamp *= 0.8;
                    double intervalSize = maxTimestamp - minTimestamp;
                    for (int i1 = 0; i1 < timestamps.size(); i1++) {
                        double currTimestamp = timestamps.get(i1);
                        currTimestamp -= minTimestamp;
                        currTimestamp /= intervalSize;
                        currTimestamp *= 100;
                        timestamps.set(i1, currTimestamp);
                    }
                    // Scale the value data
                    double maxVal = Collections.max(values);
                    double minVal = Collections.min(values);
                    maxVal *= 1.2;
                    minVal *= 0.8;
                    intervalSize = maxVal - minVal;
                    for (int i1 = 0; i1 < values.size(); i1++) {
                        double currVal = values.get(i1);
                        currVal -= minVal;
                        currVal /= intervalSize;
                        currVal *= 100;
                        values.set(i1, currVal);

                    }

                    // Data xData = Data.newData(timestamps);
                    Data yData = Data.newData(values);
                    Plot plot = Plots.newPlot(yData);
                    LineChart lineChart = GCharts.newLineChart(plot);
                    lineChart.setSize(400, 200);
                    lineChart.addYAxisLabels(AxisLabelsFactory
					          .newNumericRangeAxisLabels(minVal, maxVal));


                    mCurrentSensorGraphs.put(curr_sensor, 
					           getBitmapFromURL(lineChart.toURLString()));

                }

                mCallback.SetSensorAvg(mSensorAverage);
                mCallback.setSensorGraphs(mCurrentSensorGraphs);
                Log.i("Main Service", "Graphs updated");

                // restart it after a given amount of time
                mHandler.postDelayed(mUpdateSensorGraphsRunnable,
				         GRAPH_UPDATE_DELAY_MILLIS);
            }
        }

        private double computeAverage(ArrayList<Double> values) {
            double sum = 0.0;
            if(!values.isEmpty()){
                for(Double value : values){
                    sum += value;
                }
                return sum/values.size();
            }
            return sum;
        }

        /** It is the getter function that shows the status of runnable
         *
         * @return mIsStopped, true if the runnable is stopped, false otherwise
         */
        public boolean isStopped()
        {
            return mIsStopped;
        }

        /** It is the setter that allows to stop the runnable
         *
         * @param isStopped, true if the user wishes to stop the runnable,
		 *  false otherwise
         */
        public void setStop(boolean isStopped)
        {
            this.mIsStopped = isStopped;
        }
    }

    /** Runnable that implements the task for downloading beating image   */
    public class ImageDownloader implements Runnable {
        private String url;
        private Context c;
        private boolean mIsStopped = false;

        /** Class constructor of ImageDownloader runnable
         *
         * @param url, url link of image
         * @param c, is the context in which the request of downloading image 
		 *       has been sent
         */
        public ImageDownloader(String url,Context c )
        {
            this.url = url;
            this.c = c;
        }

        /** It implements the task of ImageDownloader runnable
         *
         * @see
         */
        @Override
        public void run()
        {
            if(!isStopped())
            {
                bmp = getBitmapFromURL(url);

                mCallback.setBMP(bmp);
                Log.i(BEATING_TAG,"bmp settato");
            }
        }

        /** It is the getter function that shows the status of ImageDownloader 
		 * runnable
         *
         * @return mIsStopped, true if the runnable is stopped, false otherwise
         */
        public boolean isStopped()
        {
            return mIsStopped;
        }

        /** It is the setter that allows to stop the runnable
         *
         * @param isStopped, true if the user wishes to stop the runnable,
         * false otherwise
         */
        public void setIsStopped(boolean isStopped)
        {
            this.mIsStopped = isStopped;
        }
    }
    /** This method pulls an image from the given url
     *
     * @param urlLink where the image is stored
     * @return Bitmap which contains the image of the graph
     * @throws java.io.IOException if an IO exception occurred during the download
     */
    public static Bitmap getBitmapFromURL(String urlLink){
        try{
            Log.i(BEATING_TAG, "start downloading image ");
            long startTime = System.currentTimeMillis();
            URL url = new URL(urlLink);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoInput(true);
            connection.connect();
            InputStream inputStream = connection.getInputStream();
            Log.i(BEATING_TAG,"download completed in "
                    + ((System.currentTimeMillis() - startTime) / 1000)
                    + " sec");
            return BitmapFactory.decodeStream(inputStream);
        } catch (IOException e) {
            e.printStackTrace();
            Log.e(BEATING_TAG, e.getMessage());
            return null;
        }
    }


    // Runnable that updates the microscope video
    private class UpdateMicroscopeVideoRunnable implements Runnable {
        private boolean mIsStopped = false;
        public void run() {
            if (!isStopped()) {
                getMicroscopeVideo();
                mHandler.postDelayed(mUpdateMicroscopeVideoRunnable, 
				               VIDEO_UPDATE_DELAY_MILLIS);
            }
        }
        public boolean isStopped() {
            return mIsStopped;
        }
        public void setStop(boolean isStopped) {
            this.mIsStopped = isStopped;
        }
    }

    // Pull the microscope video from a URL
    private void getMicroscopeVideo() {
        try {
            URL url = new URL("http://CENSURED/video/view");
            long startTime = System.currentTimeMillis();
            Log.i(VIDEO_TAG, "video download beginning: "+url);
            URLConnection ucon = url.openConnection();
            ucon.setReadTimeout(0);
            ucon.setConnectTimeout(0);
            // Define InputStreams to read from the URLConnection.
            InputStream is = ucon.getInputStream();
            BufferedInputStream inStream = new BufferedInputStream(is, 1024*5);
            File file = new File(TEMP_VIDEO_FILE_NAME);

            FileOutputStream outStream = new FileOutputStream(file);

            FileLock lock = outStream.getChannel().lock();
            byte[] buff = new byte[1024*5];
            // Read bytes (and store them) until there is nothing more to read(-1)
            int len;
            while ((len = inStream.read(buff)) != -1) {
                outStream.write(buff,0,len);
            }
            // Clean up

            outStream.flush();
            lock.release();
            outStream.close();
            inStream.close();
            Log.i(VIDEO_TAG, "download completed in "
                    + ((System.currentTimeMillis() - startTime) / 1000)
                    + " sec");
        }
        catch (IOException e) {
            Log.e(VIDEO_TAG, "Failed to download microscope video", e);
        }
    }
	
    private class UpdateElectrovalvesStatus implements Runnable {
        private boolean mIsStopped = false;

        public void run(){
            if (!isStopped()){
                try {
                    getElectrovalvesStatus();
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                Log.i(LIVE_CARD_TAG, "Status Updated");
            }
        }
        public boolean isStopped(){
            return mIsStopped;
        }
        public void setStop(boolean isStopped){
            this.mIsStopped = isStopped;
        }
    }

    public static void getElectrovalvesStatus() throws IOException, 
	                                                   InterruptedException {
        for(int index = 0; index < Electrovalves.ELECTROVALVES_NUMBER; index++){
            String res = HTTPget(Electrovalves.URL_ELECTROVALVES_BASE + "EV" + 
			             Integer.toString(index+1));
            if(res.equals(" True ")){
                Electrovalves.setElectrovalvesStatus(index,true);
            }
            else if(res.equals(" False ")) {
                Electrovalves.setElectrovalvesStatus(index,false);
            }
            Log.i("EV"+Integer.toString(index+1), res);

        }
    }


    private static String HTTPget(String link) throws IOException, 
	                                                  InterruptedException {
        URL url = new URL(link);
        HttpURLConnection mUrlConnection = (HttpURLConnection) url.openConnection();
        InputStream in = new BufferedInputStream(mUrlConnection.getInputStream());
        int ch;
        StringBuffer b = new StringBuffer();
        while ((ch = in.read()) != -1){
            b.append((char) ch);
        }

        String mResult = new String(b);
        return mResult;

    }

    // convert inputstream to String
    private static String convertInputStreamToString(InputStream inputStream) 
	                                                      throws IOException{
        BufferedReader bufferedReader = new BufferedReader( 
		                                 new InputStreamReader(inputStream));
        String line = "";
        String result = "";
        while((line = bufferedReader.readLine()) != null)
            result += line;

        inputStream.close();
        return result;

    }


}
