// This drawer has been designed using the chronometer example provided by Google
package com.google.android.glass.sample.klabinterface;

import com.google.android.glass.timeline.DirectRenderingCallback;
import com.jjoe64.graphview.GraphView;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.os.Environment;
import android.os.Handler;
import android.os.SystemClock;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.View;

import java.util.Map;

/**
 * {@link DirectRenderingCallback} used to draw the KlabInterface on the timeline 
 * {@link com.google.android.glass.timeline.LiveCard}.
 * Rendering requires that:
 * <ol>
 * <li>a {@link SurfaceHolder} has been created through monitoring the
 *     {@link SurfaceHolder.Callback#(SurfaceHolder)} and
 *     {@link SurfaceHolder.Callback#(SurfaceHolder)} callbacks.
 * <li>rendering has not been paused (defaults to rendering) through monitoring the
 *     {@link com.google.android.glass.timeline.DirectRenderingCallback#
 *     renderingPaused(SurfaceHolder, boolean)} callback.
 * </ol>
 * As this class uses an inflated {@link View} to draw on the 
 * {@link SurfaceHolder}'s
 * {@link Canvas}, monitoring the
 * {@link SurfaceHolder.Callback#(SurfaceHolder, int, int, int)} callback is also
 * required to properly measure and layout the {@link View}'s dimension.
 */
public class AppDrawer. implements DirectRenderingCallback {
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

    /** Bitmap in which the beating image is stored */
    private Bitmap bmp;
    private boolean mReady;

    private static final String TAG = AppDrawer..class.getSimpleName();
	/** View object of the GlassWear Main window */
    private final MainView mMainView;
	/** View object of the GlassWear Beating window */
    private final BeatingView mBeatingView;
    /** View object of the GlassWear pH window */
    private final PHViewer mPhViewer;
	/** View object of the GlassWear Temperature window */
    private final TemperatureView mTemperatureView;

    private SurfaceHolder mHolder;
    private boolean mRenderingPaused;

    /** Hash table in which the sensors graphs are stored */
    private Map<String,Bitmap> mCurrentSensorGraphs;
    /** Hash table in which the sensors values are stored  */
    private Map<String,Double> mCurrentSensorValues;
	/** Hash table in which the sensors average values are stored  */
    private Map<String,Double> mSensorAverage;
    private GraphView mGraphView;

    private final MainView.Listener mMainListener = new MainView.Listener() {


        @Override
        public void onChange() {
            updateRenderingState();
        }
    };

    /** Defines the Listener of pH viewer, it is used to communicate with
     * that viewer and allowing its viewing */
    private final PHViewer.Listener mPhListener = new PHViewer.Listener(){
        @Override
        public void onChange(){
                updateRenderingState();
        }
    };

	/** Defines the Listener of Beating viewer, it is used to communicate with
     * that viewer and allowing its viewing */
    private final BeatingView.Listener mBeatingListener = 
	                                 new BeatingView.Listener() {

        @Override
        public void onChange() {
            updateRenderingState();
        }
    };
	
	/** Defines the Listener of Temperature viewer, it is used to communicate with
     * that viewer and allowing its viewing */
    private final TemperatureView.Listener mTemperatureListener = 
	                                 new TemperatureView.Listener() {

        @Override
        public void onChange() {
            updateRenderingState();
        }
    };

    /** Defines the ManageBitmap of Beating viewer, it is used to communicate with
     * that viewer in order to update the bitmap to be displayed*/
    private final BeatingView.ManageBitmap mBeatingBitmap = 
	                                     new BeatingView.ManageBitmap(){
        @Override
        public Bitmap getBitmap() {
            return bmp;
        }

        public boolean isReady(){
            return mReady;
        }
    };

    /** Defines the ManageDataGraph of PH viewer, it is used to communicate with
     * that viewer in order to update the hash map which contains the graph to
	 * be displayed*/
    private final PHViewer.ManageBitmap mPHManageDataGraph = 
	                                     new PHViewer.ManageBitmap(){

        public Bitmap getBitmap() {
            return mCurrentSensorGraphs.get("pH");
        }

        public double getAvg(){return mSensorAverage.get("pH");}

        public boolean isReady(){
            return mReady;
        }
    };

    /** Defines the ManageDataGraph of PH viewer, it is used to communicate with
     * that viewer in order to update the hash map which contains the graph 
	 * to be displayed*/
    private final TemperatureView.ManageBitmap mTemperatureManageDataGraph = 
	                                     new TemperatureView.ManageBitmap(){

        public Bitmap getBitmap() {
            return mCurrentSensorGraphs.get("pH");
        }

        public double getAvg(){return mSensorAverage.get("Temperature");}

        public boolean isReady(){
            return mReady;
        }
    };


    public AppDrawer.(Context context) {
        this(new MainView(context), new BeatingView(context), 
		     new PHViewer(context), new TemperatureView(context));
    }

    public AppDrawer.(MainView countDownView, BeatingView chronometerView, 
	                  PHViewer phViewer, TemperatureView temperatureView) {
        mMainView = countDownView;
        mMainView.setListener(mMainListener);

        mBeatingView = chronometerView;
        mBeatingView.setListener(mBeatingListener);
        mBeatingView.setManageBPM(mBeatingBitmap);

        mPhViewer = phViewer;
        mPhViewer.setListener(mPhListener);
        mPhViewer.setManageBPM(mPHManageDataGraph);

        mTemperatureView = temperatureView;
        mTemperatureView.setListener(mTemperatureListener);
        mTemperatureView.setManageBPM(mTemperatureManageDataGraph);

        mReady = false;
    }

    /**
     * Uses the provided the width and height to measure and layout the 
     * inflated View.
     */
    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width,
                             	int height) {
        // Measure and layout the view with the canvas dimensions.
        int measuredWidth = View.MeasureSpec.makeMeasureSpec(width, 
		                    View.MeasureSpec.EXACTLY);
        int measuredHeight = View.MeasureSpec.makeMeasureSpec(height, 
		                    View.MeasureSpec.EXACTLY);

        mMainView.measure(measuredWidth, measuredHeight);
        mMainView.layout(0, 0, mMainView.getMeasuredWidth(), 
		                 mMainView.getMeasuredHeight());

        mBeatingView.measure(measuredWidth, measuredHeight);
        mBeatingView.layout(0, 0, mBeatingView.getMeasuredWidth(), 
		                    mBeatingView.getMeasuredHeight());

        mPhViewer.measure(measuredWidth, measuredHeight);
        mPhViewer.layout(0, 0, mPhViewer.getMeasuredWidth(), 
		                 mPhViewer.getMeasuredHeight());

        mTemperatureView.measure(measuredWidth, measuredHeight);
        mTemperatureView.layout(0, 0, mTemperatureView.getMeasuredWidth(), 
		                        mTemperatureView.getMeasuredHeight());
    }

    /**
     * Keeps the created SurfaceHolder and updates this class' rendering state.
     */
    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        // The creation of a new Surface implicitly resumes the rendering.
        mRenderingPaused = false;
        mHolder = holder;
        updateRenderingState();
    }

    /**
     * Removes the SurfaceHolder used for drawing and stops rendering.
     */
    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        mHolder = null;
        updateRenderingState();
    }

    /**
     * Updates this class rendering state according to the provided paused flag.
     */
    @Override
    public void renderingPaused(SurfaceHolder holder, boolean paused) {
        mRenderingPaused = paused;
        updateRenderingState();
    }

    /**
     * Starts or stops rendering according to the LiveCard's state.
     */
    private void updateRenderingState() {
        if (mHolder != null && !mRenderingPaused) {
            switch (AppManager.getInstance().getState())
            {
                case MENU:
                    draw(mMainView);
                    mBeatingView.stop();
                    mPhViewer.stop();
                    mTemperatureView.stop();
                    mMainView.start();
                    break;
                case BEATING:
                    draw(mBeatingView);
                    mMainView.stop();
                    mPhViewer.stop();
                    mTemperatureView.stop();
                    mBeatingView.start();
                    break;
                case PH:
                    draw(mPhViewer);
                    mMainView.stop();
                    mBeatingView.stop();
                    mTemperatureView.stop();
                    mPhViewer.start();
                    break;
                case TEMPERATURE:
                    draw(mTemperatureView);
                    mMainView.stop();
                    mBeatingView.stop();
                    mPhViewer.stop();
                    mTemperatureView.start();
                    break;
                case VIDEO:
                    mTemperatureView.stop();
                    mBeatingView.stop();
                    mMainView.stop();
                    mPhViewer.stop();
                    break;
                default:
                    draw(mMainView);
                    mPhViewer.stop();
                    mBeatingView.stop();
                    mTemperatureView.stop();
                    mMainView.start();
                    break;

            }

        } else {
            mMainView.stop();
            mBeatingView.stop();
            mPhViewer.stop();
            mTemperatureView.stop();
        }

    }

    /**
     * Draws the view in the SurfaceHolder's canvas.
     */
    private void draw(View view) {

        Canvas canvas;

        try {
            canvas = mHolder.lockCanvas();
        } catch (Exception e) {
            Log.e(TAG, "Unable to lock canvas: " + e);
            return;
        }
        if (canvas != null) {

            view.draw(canvas);
            mHolder.unlockCanvasAndPost(canvas);
        }
    }
    /** Setter for the beating graph
     *
     * @param bmp ,  Bitmap which contains the beating graph
     */
    public void setBMP(Bitmap bmp){

        this.bmp = bmp;
        this.mReady = true;
        Log.i("DRAWER", "bmp settato");
    }

	/** Setter for the sensors Graphs hash map
     *
     * @param sensorGraphs ,  Map<String,Double> sensorGraphs 
	 *                            which contains the hash
     *                            map with graphs of the sensors
     */
    public void setSensorGraphs(  Map<String,Bitmap> sensorGraphs ){
        this.mCurrentSensorGraphs = sensorGraphs;
    }

	/** Setter for the sensors viewer hash map
     *
     * @param graphView ,  Map<String,Double> sensorGraphs 
	 *                            which contains the hash
     *                            map with viewer of the sensors
     */
    public void setPHGraphViewer(  GraphView graphView ){
        this.mGraphView = graphView;
    }

    /** Setter for the sensors value hash map
     *
     * @param currentSensorValues ,  Map<String,Double> currentSensorValues 
	 *                               which contains the hash
     *                               map with the current value of the sensors
     */
    public void setSensorValues(  Map<String,Double> currentSensorValues ){
        this.mCurrentSensorValues = currentSensorValues;
    }

    /** Setter for the sensors average values hash map
     *
     * @param sensorAvg ,  Map<String,Double> currentSensorValues 
	 *                               which contains the hash
     *                               map with the average values of the sensors
     */
    public  void SetSensorAvg (Map<String,Double> sensorAvg){
        this.mSensorAverage = sensorAvg;
    }



}
