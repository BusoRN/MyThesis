package com.google.android.glass.sample.klabinterface;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;


public class ImageMenuActivity extends Activity {

    private final Handler mHandler = new Handler();
    private int state = 0;

    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        openOptionsMenu();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.beating_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection.
        switch (item.getItemId()) {
            case R.id.action_stop:
                // Stop the service at the end of the message queue for proper options menu
                // animation. This is only needed when starting a new Activity or stopping a Service
                // that published a LiveCard.
                post(new Runnable() {

                    @Override
                    public void run() {
                        stopService(new Intent(ImageMenuActivity.this, StopwatchService.class));
                    }
                });
                return true;
            case R.id.action_back:
                handleViewBack();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void handleViewBack() {
        Toast.makeText(this, "Menu", Toast.LENGTH_SHORT).show();
        // launch a new thread for starting a new live card
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(ImageMenuActivity.this, StopwatchService.class);
                intent.putExtra(Intent.EXTRA_TEXT, "Menu");
                startService(intent);
            }
        });
    }

    @Override
    public void onOptionsMenuClosed(Menu menu) {
        // Nothing else to do, closing the Activity.
        finish();
    }

    /**
     * Posts a {@link Runnable} at the end of the message loop, overridable for testing.
     */
    protected void post(Runnable runnable) {
        mHandler.post(runnable);
    }



}
