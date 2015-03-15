package com.google.android.glass.sample.stopwatch;

import android.content.Context;
import android.graphics.Color;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.RelativeLayout;
import android.widget.TextView;

/**
 * Subclass of RelativeLayout that is in charge to render the UI of live 
 * card when the user wants to drive the valves.
 */
public class ElectrovalvesView extends RelativeLayout {

    private Listener mListener;
    private boolean mRunning = false;

    /**
     * Array of TextView for each electrovalve
     */
    private final TextView[] electrovalveText = new TextView[8];

    /**
     * Interface to listen for changes on the view layout.
     */
    public interface Listener {
        /** Notified of a change in the view. */
        public void onChange();
    }

    /** Time delimiter specifying when the second component is fully shown. */
    private static final long DELAY_MILLIS = 40;

    private final Handler mHandler = new Handler();
    private final Runnable mUpdateViewRunnable = new Runnable() {

        @Override
        public void run() {
            if (mRunning) {
                updateView();
                postDelayed(mUpdateViewRunnable, DELAY_MILLIS);
            }
        }
    };

    public ElectrovalvesView(Context context) {
        this(context, null);
    }

    public ElectrovalvesView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ElectrovalvesView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        LayoutInflater.from(context).inflate(R.layout.electrovalves_layout, this);

        electrovalveText[0] = (TextView) findViewById(R.id.ev1);
        electrovalveText[1] = (TextView) findViewById(R.id.ev2);
        electrovalveText[2] = (TextView) findViewById(R.id.ev3);
        electrovalveText[3] = (TextView) findViewById(R.id.ev4);
        electrovalveText[4] = (TextView) findViewById(R.id.ev5);
        electrovalveText[5] = (TextView) findViewById(R.id.ev6);
        electrovalveText[6] = (TextView) findViewById(R.id.ev7);
        electrovalveText[7] = (TextView) findViewById(R.id.ev8);
    }

    /**
     * Sets a Listener.
     */
    public void setListener(Listener listener) {
        mListener = listener;
    }

    /**
     * Returns the set Listener.
     */
    public Listener getListener() {
        return mListener;
    }

    @Override
    public boolean postDelayed(Runnable action, long delayMillis) {
        return mHandler.postDelayed(action, delayMillis);
    }

    @Override
    public boolean removeCallbacks(Runnable action) {
        mHandler.removeCallbacks(action);
        return true;
    }

    /**
     * Starts the rendering of electrovalves status.
     */
    public void start() {
        if (!mRunning) {
            postDelayed(mUpdateViewRunnable, DELAY_MILLIS);
        }
        mRunning = true;
    }

    /**
     * Stops the rendering of electrovalves status.
     */
    public void stop() {
        if (mRunning) {
            removeCallbacks(mUpdateViewRunnable);
            // mStarted = false;
        }
        mRunning = false;
    }

    /**
     * Updates the view to reflect the current state of animation, visible for testing.
     */
    void updateView() {
        if(Electrovalves.isReady()){
           // electrovalves_status = mElectrovalvesStatus.getElectrovalvesStatus();
            for(int i = 0; i<Electrovalves.ELECTROVALVES_NUMBER; i++){
                if(Electrovalves.getElectrovalveStatus(i)){
                    electrovalveText[i].setText("EV"+Integer.toString(i+1)+": ON");
                    electrovalveText[i].setTextColor(Color.GREEN);
                }
                else{
                    electrovalveText[i].setText("EV"+Integer.toString(i+1)+": OFF");
                    electrovalveText[i].setTextColor(Color.RED);
                }
            }
        }
        if (mListener != null) {
            mListener.onChange();
        }
    }
}
