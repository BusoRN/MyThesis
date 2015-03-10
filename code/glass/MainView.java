/*
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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

/**
 * Animated countdown going from {@code mTimeSeconds} to 0.
 *
 * The current animation for each second is as follow:
 *   1. From 0 to 500ms, move the TextView from {@code MAX_TRANSLATION_Y} to 0 and its alpha from
 *      {@code 0} to {@code ALPHA_DELIMITER}.
 *   2. From 500ms to 1000ms, update the TextView's alpha from {@code ALPHA_DELIMITER} to {@code 1}.
 * At each second change, update the TextView text.
 */
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



    /**
     * Updates the view to reflect the current state of animation, visible for testing.
     *
     * @return whether or not the count down is finished.
     */
    void updateView() {
        //if (mRunning) {
        timeText.setText( new SimpleDateFormat("hh:mm a").format(new Date()));
            // updateView(millisLeft);
            if (mListener != null) {
                mListener.onChange();
            }

        //}
    }
}
