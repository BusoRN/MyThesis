package com.google.android.glass.sample.klabinterface;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import java.lang.Runnable;

/**
 * Activity showing the stopwatch options menu.
 */
public class MenuActivity extends Activity {

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
        inflater.inflate(R.menu.stopwatch, menu);
        return true;
    }

    @Override
    public boolean onPreparePanel(int featureId, View view, Menu menu) {
        AppManager appManager = AppManager.getInstance();

        boolean initialView = appManager.getState() == MENU;
        boolean imageView = appManager.getState() == PH ||
                appManager.getState() == TEMPERATURE ||
                appManager.getState() == BEATING ||
                appManager.getState() == VIDEO;

        setOptionsMenuState(menu, R.id.action_back, imageView);
        setOptionsMenuState(menu, R.id.action_view_ph, initialView);
        setOptionsMenuState(menu, R.id.action_view_temperature, initialView);
        setOptionsMenuState(menu, R.id.action_view_video, initialView);
        setOptionsMenuState(menu, R.id.action_view_beating, initialView);
        setOptionsMenuState(menu, R.id.action_stop, true);

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
                        stopService(new Intent(MenuActivity.this, StopwatchService.class));
                    }
                });
                return true;
            case R.id.action_view_beating:
                handleViewBeating();
                return true;
            case R.id.action_back:
                handleViewBack();
                return true;
            case R.id.action_view_temperature:
                handleViewTemperature();
                return true;
            case R.id.action_view_ph:
                handleViewPh();
                return true;
            case R.id.action_view_video:
                handleViewVideo();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void handleViewVideo() {
        Toast.makeText(this, "Video", Toast.LENGTH_SHORT).show();
        // launch a new thread for starting a new live card
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(MenuActivity.this, StopwatchService.class);
                intent.putExtra(Intent.EXTRA_TEXT, "Video");
                startService(intent);
            }
        });
    }

    private void handleViewTemperature() {
        Toast.makeText(this, "Temperature", Toast.LENGTH_SHORT).show();
        // launch a new thread for starting a new live card
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(MenuActivity.this, StopwatchService.class);
                intent.putExtra(Intent.EXTRA_TEXT, "Temperature");
                startService(intent);
            }
        });
    }

    private void handleViewPh() {
        Toast.makeText(this, "Ph", Toast.LENGTH_SHORT).show();
        // launch a new thread for starting a new live card
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(MenuActivity.this, StopwatchService.class);
                intent.putExtra(Intent.EXTRA_TEXT, "pH");
                startService(intent);
            }
        });
    }

    private void handleViewBack() {
        Toast.makeText(this, "Menu", Toast.LENGTH_SHORT).show();
        // launch a new thread for starting a new live card
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(MenuActivity.this, StopwatchService.class);
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

    /**
     * The function handle the request to view the beating plot
     */
    private void handleViewBeating() {
        Toast.makeText(this, "Beating", Toast.LENGTH_SHORT).show();
        // launch a new thread for starting a new live card
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(MenuActivity.this, StopwatchService.class);
                intent.putExtra(Intent.EXTRA_TEXT, "Beating");
                startService(intent);
            }
        });
    }

    private static void setOptionsMenuState(Menu menu, int menuItemId, boolean enabled){
        MenuItem menuItem = menu.findItem(menuItemId);
        menuItem.setVisible(enabled);
        menuItem.setEnabled(enabled);
    }

}
