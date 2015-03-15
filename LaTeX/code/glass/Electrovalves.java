package com.google.android.glass.sample.stopwatch;

import android.util.Log;

import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.lang.Object;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.concurrent.Semaphore;

/**
 * Static class where the electrovalves status are memorized
 */
public class Electrovalves {


    private static boolean mReady;

    /** Number of Electrovalves */
    public static final int ELECTROVALVES_NUMBER = 8;

    /** URL where the electrovalves information is stored */
    public static final String URL_ELECTROVALVES_BASE = 
	                                                 "http://CENSURED/show/";
    /** URL where the electrovalves information has to be post */
    public static final String URL_ELECTROVALVES_POST_BASE = 
	                                        "http://CENSURED/add/electrovalve";
    /** URL to set the value of the electrovalves */
    public static final String URL_SET_ELECTROVALVES_BASE = 
	                                                 "http://CENSURED/add/EV" ; 

    /** Array where the electrovalves status is stored */
    public static boolean[] electrovalves_status = 
	                                          new boolean[ELECTROVALVES_NUMBER];

    public static boolean getElectrovalveStatus(int index)
    {
        return electrovalves_status[index];
    }

    /** Setter for the Electrovalves Status
     *
     * @param index , position in the electrovalves_status array
     * @param value , boolean, true if the electrovalves is on, false otherwise
     */
    public static void setElectrovalvesStatus(int index, boolean value){
        electrovalves_status[index] = value;
        Log.i("DRAWER", "EV" + Integer.toString(index + 1) + " has been set");
        if(index == ELECTROVALVES_NUMBER-1) {
            setReady(true);
            Log.i("ELECTROVALVES", "status ready");
        }
    }

    public static boolean isReady(){
        return mReady;
    }

    public static void setReady(boolean value){
        mReady = value;
    }
}
