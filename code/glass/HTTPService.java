package com.google.android.glass.sample.stopwatch;

import android.app.Service;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.IBinder;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

public class HTTPService extends Service {

    String httpMethod;
    String httpLink;
    HttpURLConnection mUrlConnection;
    String mResult;
    String urlParameters;

    public HTTPService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        //from the intent I take which http method is it
        httpMethod = intent.getStringExtra("METHOD");
        Log.i("HTTP METHOD", httpMethod);
        httpLink = intent.getStringExtra("LINK");
        Log.i("HTTP LINK", httpLink);
        urlParameters = intent.getStringExtra("PARAM");
        Log.i("HTTP PARAM", urlParameters);
        new HttpRequest().execute();

        return START_NOT_STICKY;
    }

    // Async task class to make HTTP GET and POST
    private class HttpRequest extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void...arg0){
            try{
                if (httpMethod.equalsIgnoreCase("GET")){
                    URL url = new URL(httpLink);
                    mUrlConnection = (HttpURLConnection) url.openConnection();
                    InputStream in = new BufferedInputStream(
					                          mUrlConnection.getInputStream());
                    int ch;
                    StringBuffer b = new StringBuffer();
                    while ((ch = in.read()) != -1){
                        b.append((char) ch);
                    }
                    mResult = new String(b);
                }

                if (httpMethod.equalsIgnoreCase("POST")) {
                    URL url = new URL(httpLink);
                    mUrlConnection = (HttpURLConnection) url.openConnection();
                    mUrlConnection.setRequestMethod("POST");

                    OutputStreamWriter writer = new OutputStreamWriter(
					                         mUrlConnection.getOutputStream());
                    writer.write(urlParameters);
                    writer.flush();

                    InputStream in = new BufferedInputStream( 
					                          mUrlConnection.getInputStream());
                    int ch;
                    StringBuffer b = new StringBuffer();
                    while ((ch = in.read()) != -1){
                        b.append((char) ch);
                    }
                    mResult = new String(b);
                    in.close();
                    writer.close();
                }
            } catch (Exception e){}
            return null;
        }

    }
}
