package com.google.android.glass.sample.klabinterface;

import android.app.Activity;
import android.content.Intent;
import android.content.res.AssetManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.MediaController;
import android.widget.VideoView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
public class VideoPlayerActivity extends Activity {
   private static final int VIDEO_PLAY_REQUEST_CODE = 200;
   private static final String TAG = "VIDEO_TAG";
   public void onCreate(Bundle savedInstanceState) {
       super.onCreate(savedInstanceState);
       String filepath;
       Bundle extras = getIntent().getExtras();
       if (extras != null)
           filepath = extras.getString("filepath");
       else {
           filepath = copyAsset(VIDEO_FILE_NAME);
       }

       Intent i = new Intent();
       i.setAction("com.google.glass.action.VIDEOPLAYER");
       i.putExtra("video_url", filepath);
       startActivityForResult(i, VIDEO_PLAY_REQUEST_CODE);
   }
   protected void onActivityResult(int requestCode, int resultCode, Intent data) {
       if (requestCode == VIDEO_PLAY_REQUEST_CODE )
           finish();
   }
   String copyAsset(String filename) {
       final String PATH = Environment.getExternalStorageDirectory().toString() + "/myvideoapps/";
       File dir = new File(PATH);
       if (!dir.exists()) {
           if (!dir.mkdirs()) {
               Log.v(TAG, "ERROR: Creation of directory " + PATH + " on sdcard failed");
               return null;
           } else {
               Log.v(TAG, "Created directory " + PATH + " on sdcard");
           }
       }
       if (!(new File( PATH + filename).exists())) {
           try {
               AssetManager assetManager = getAssets();
               InputStream in = assetManager.open(filename);
               OutputStream out = new FileOutputStream(PATH + filename);
               byte[] buf = new byte[1024];
               int len;
               while ((len = in.read(buf)) > 0) {
                   out.write(buf, 0, len);
               }
               in.close();
               out.close();
           } catch (IOException e) {
               Log.e(TAG, "Was unable to copy " + filename + e.toString());
               return null;
           }
       }
       return PATH + filename;
    }
}
