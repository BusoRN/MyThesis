package com.google.android.glass.sample.klabinterface;

/**
 * Created by Buso on 28/10/2014.
 */
class DataPoint {
    private final double mTimestamp;
    private final double mValue;
    DataPoint(double timestamp, double value) {
        mTimestamp = timestamp;
        mValue = value;
    }
    public double getTimestamp() {
        return mTimestamp;
    }
    public double getValue() {
        return mValue;
    }
}
