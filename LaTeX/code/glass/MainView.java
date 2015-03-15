package com.google.android.glass.sample.klabinterface;

import android.content.Context;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Handler;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.FrameLayout;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;

public class MainView extends FrameLayout {

    /**
     * Interface to listen for changes in the countdown.
     */
    public interface Listener {

        /**
         * Notified when the countdown is finished.
         */
        public void onChange();
    }

    /** Time delimiter specifying when the second component is fully shown. */
    private static final long DELAY_MILLIS = 40;

    private final TextView timeText;

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

    private Listener mListener;
    private boolean mRunning = false;

    public MainView(Context context) {
        this(context, null, 0);
    }

    public MainView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MainView(Context context, AttributeSet attrs, int style) {
        super(context, attrs, style);
        LayoutInflater.from(context).inflate(R.layout.live_card_layout, this);
        timeText = (TextView) findViewById(R.id.timestamp);
    }

    /**
     * Sets a {@link Listener}.
     */
    public void setListener(Listener listener) {
        mListener = listener;
    }

    /**
     * Returns the set {@link Listener}.
     */
    public Listener getListener() {
        return mListener;
    }

    @Override
    public boolean postDelayed(Runnable action, long delayMillis) {
        return mHandler.postDelayed(action, delayMillis);
    }

    /**
     * Starts the countdown animation if not yet started.
     */
    public void start() {
        if (!mRunning) {
            postDelayed(mUpdateViewRunnable, 0);
        }
        mRunning = true;
    }

    /**
     * Stops the chronometer.
     */
    public void stop() {
        if (mRunning) {
            removeCallbacks(mUpdateViewRunnable);
           // mStarted = false;
        }
        mRunning = false;
    }

    void updateView() {
        if (mRunning) {
			timeText.setText( new SimpleDateFormat("hh:mm a")
			        .format(new Date()));
            if (mListener != null) {
                mListener.onChange();
            }

        }
    }
}
