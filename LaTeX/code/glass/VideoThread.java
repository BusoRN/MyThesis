package com.google.android.glass.sample.klabinterface;

import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;
import android.view.SurfaceHolder;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;

/**
 * Created by Buso on 28/10/2014.
 */

public class VideoThread extends Thread implements MediaPlayer.OnCompletionListener {
    private static final String VIDEO_TAG = "Video";
    private static final String VIDEO_FILE_NAME = Environment.getExternalStorageDirectory()+"/microscope_video.mp4";
    private static final String TEMP_VIDEO_FILE_NAME = Environment.getExternalStorageDirectory()+"/temp_microscope_video.mp4";

    private SurfaceHolder mSurfaceHolder;
    private boolean mShouldRun;
    private MediaPlayer mMediaPlayer;
    private MediaMetadataRetriever mFrameGetter;
    private UpdateGraphRunnable mUpdateGraphRunnable;
    private HandlerThread mGraphHandlerThread;
    private Handler mGraphHandler;

    public VideoThread(SurfaceHolder surfaceHolder) {
        Log.i(VIDEO_TAG, "Video Thread creato");
        mShouldRun = true;
        mGraphHandlerThread = new HandlerThread("graphHandlerThread");
        mGraphHandlerThread.start();
        mGraphHandler = new Handler(mGraphHandlerThread.getLooper());
        mUpdateGraphRunnable = new UpdateGraphRunnable();
        mSurfaceHolder = surfaceHolder;
    }

    private synchronized boolean shouldRun() {
        return mShouldRun;
    }

    public synchronized void setShouldRun(boolean shouldRun){
        this.mShouldRun = shouldRun;
    }

    public synchronized void quit() {
        mShouldRun = false;
    }



    @Override
    public void run() {
        while (shouldRun()) {
            try {
                Log.i(VIDEO_TAG, "copio il file");
                copyFile(new File(TEMP_VIDEO_FILE_NAME), new File(VIDEO_FILE_NAME));
                mMediaPlayer = new MediaPlayer();
               // mDataPoints = new ArrayList<DataPoint>();
                mMediaPlayer.setDataSource(VIDEO_FILE_NAME);
                Log.i(VIDEO_TAG,"avvio il video");
                mMediaPlayer.setDisplay(mSurfaceHolder);
                mMediaPlayer.setScreenOnWhilePlaying(true);
                mMediaPlayer.setOnCompletionListener(this);
                mMediaPlayer.prepare();
                mFrameGetter = new MediaMetadataRetriever();
                mFrameGetter.setDataSource(VIDEO_FILE_NAME);
                mMediaPlayer.start();
                Log.i(VIDEO_TAG,"dovrebbe essere partito");


                if (mMediaPlayer.isPlaying()) {
                    mMediaPlayer.stop();
                }
                mMediaPlayer.reset();
                mMediaPlayer.release();
            } catch (IOException e) {
                if (mMediaPlayer.isPlaying()) {
                    mMediaPlayer.stop();
                }
                mMediaPlayer.reset();
                mMediaPlayer.release();
            }
        }

    }

    @Override
    public void onCompletion(MediaPlayer mp) {

    }

    private class UpdateGraphRunnable implements Runnable {
        private boolean mIsStopped = false;

        public void run() {
            if (!isStopped()) {
                mGraphHandler.post(mUpdateGraphRunnable);
            }
        }

        public boolean isStopped() {
            return mIsStopped;
        }

        public void setStop(boolean isStopped) {
            this.mIsStopped = isStopped;
        }
    }

    // Copy a file from <src> to <dst>
    public void copyFile(File src, File dst) {
        // Acquire i/o streams for each of the files
        RandomAccessFile inFile;
        FileOutputStream outStream;
        try {
            inFile = new RandomAccessFile(src, "rw");
            outStream = new FileOutputStream(dst);
            Log.i(VIDEO_TAG, "File trovato");
        }
        catch (FileNotFoundException e) {
            Log.e(VIDEO_TAG, "File not found", e);
            return;
        }
        FileChannel inChannel = inFile.getChannel();
        FileChannel outChannel = outStream.getChannel();
        FileLock inLock = null;
        FileLock outLock = null;
        try {
            Log.i(VIDEO_TAG, "provo a mettere dei lock ai file");
            inLock = inChannel.lock();
            outLock = outChannel.lock();
            inChannel.transferTo(0, inChannel.size(), outChannel);
        }
        catch (IOException e) {
            Log.e(VIDEO_TAG,"Failed to acquire locks and copy file", e);
        }
        try {
            assert inLock != null;
            inLock.release();
            inFile.close();
            assert outLock != null;
            outLock.release();
            outStream.close();
            Log.i(VIDEO_TAG, "File copiato");
        }
        catch (Exception e) {
            Log.e(VIDEO_TAG,"An error occurred while cleaning up",e);
        }
    }
}
