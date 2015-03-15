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
	/** INT associated to the Electrovalves view request  */
    private static final int ELECTROVALVES = 5;

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
				appManager.getState() == ELECTROVALVES ||
                appManager.getState() == VIDEO;

        setOptionsMenuState(menu, R.id.action_back, imageView);
        setOptionsMenuState(menu, R.id.action_view_ph, initialView);
        setOptionsMenuState(menu, R.id.action_view_temperature, initialView);
        setOptionsMenuState(menu, R.id.action_view_video, initialView);
        setOptionsMenuState(menu, R.id.action_view_beating, initialView);
		    setOptionsMenuState(menu, R.id.action_drive_electrovalves, initialView);
        setOptionsMenuState(menu, R.id.action_toggle_EV1, electrovalveView);
        setOptionsMenuState(menu, R.id.action_toggle_EV2, electrovalveView);
        setOptionsMenuState(menu, R.id.action_toggle_EV3, electrovalveView);
        setOptionsMenuState(menu, R.id.action_toggle_EV4, electrovalveView);
        setOptionsMenuState(menu, R.id.action_toggle_EV5, electrovalveView);
        setOptionsMenuState(menu, R.id.action_toggle_EV6, electrovalveView);
        setOptionsMenuState(menu, R.id.action_toggle_EV7, electrovalveView);
        setOptionsMenuState(menu, R.id.action_toggle_EV8, electrovalveView);
        setOptionsMenuState(menu, R.id.action_stop, true);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection.
        switch (item.getItemId()) {
            case R.id.action_stop:
                // Stop the service at the end of the message queue for proper 
                // options menu animation. This is only needed when starting 
                // a new Activity or stopping a Service that published a LiveCard.
                post(new Runnable() {
                    @Override
                    public void run() {
                        stopService(new Intent(MenuActivity.this, 
						            StopwatchService.class));
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
				case R.id.action_drive_electrovalves:
                handleViewElectrovalves();
                return true;
            case R.id.action_toggle_EV1:
                handleToggleEV1();
                return true;
            case R.id.action_toggle_EV2:
                handleToggleEV2();
                return true;
            case R.id.action_toggle_EV3:
                handleToggleEV3();
                return true;
            case R.id.action_toggle_EV4:
                handleToggleEV4();
                return true;
            case R.id.action_toggle_EV5:
                handleToggleEV5();
                return true;
            case R.id.action_toggle_EV6:
                handleToggleEV6();
                return true;
            case R.id.action_toggle_EV7:
                handleToggleEV7();
                return true;
            case R.id.action_toggle_EV8:
                handleToggleEV8();
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
                Intent intent = new Intent(MenuActivity.this, 
				                StopwatchService.class);
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
                Intent intent = new Intent(MenuActivity.this, 
				                StopwatchService.class);
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
                Intent intent = new Intent(MenuActivity.this, 
				                StopwatchService.class);
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
                Intent intent = new Intent(MenuActivity.this, 
				                StopwatchService.class);
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
     * Posts a Runnable at the end of the message loop, overridable for testing.
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
                Intent intent = new Intent(MenuActivity.this, 
				                StopwatchService.class);
                intent.putExtra(Intent.EXTRA_TEXT, "Beating");
                startService(intent);
            }
        });
    }
	
	/**
     * The function handle the request to toggle the electrovalve number 8
     */
	private void handleToggleEV8()  {
        Toast.makeText(this, "Toggle EV8", Toast.LENGTH_SHORT).show();
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(MenuActivity.this, HTTPService.class);
                intent.putExtra("METHOD", "POST");
                if(Electrovalves.getElectrovalveStatus(7)) {
                    intent.putExtra("LINK", 
					                Electrovalves.URL_ELECTROVALVES_POST_BASE);
                    intent.putExtra("PARAM", "name=EV8&status=off");
                    Electrovalves.setElectrovalvesStatus(7, false);
                }
                else {
                    intent.putExtra("LINK", 
					                Electrovalves.URL_ELECTROVALVES_POST_BASE);
                    intent.putExtra("PARAM", "name=EV8&status=on");
                    Electrovalves.setElectrovalvesStatus(7, true);
                }
                startService(intent);
            }
        });

    }

	/**
     * The function handle the request to toggle the electrovalve number 7
     */
    private void handleToggleEV7() {
        Toast.makeText(this, "Toggle EV7", Toast.LENGTH_SHORT).show();
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(MenuActivity.this, HTTPService.class);
                intent.putExtra("METHOD", "POST");
                if(Electrovalves.getElectrovalveStatus(6)) {
                    intent.putExtra("LINK", 
					                Electrovalves.URL_ELECTROVALVES_POST_BASE);
                    intent.putExtra("PARAM", "name=EV7&status=off");
                    Electrovalves.setElectrovalvesStatus(6, false);
                }
                else {
                    intent.putExtra("LINK", 
					                Electrovalves.URL_ELECTROVALVES_POST_BASE);
                    intent.putExtra("PARAM", "name=EV7&status=on");
                    Electrovalves.setElectrovalvesStatus(6, true);
                }
                startService(intent);
            }
        });
    }

	/**
     * The function handle the request to toggle the electrovalve number 6
     */
    private void handleToggleEV6() {
        Toast.makeText(this, "Toggle EV6", Toast.LENGTH_SHORT).show();
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(MenuActivity.this, HTTPService.class);
                intent.putExtra("METHOD","POST");
                if(Electrovalves.getElectrovalveStatus(5)) {
                    intent.putExtra("LINK", 
					                Electrovalves.URL_ELECTROVALVES_POST_BASE);
                    intent.putExtra("PARAM", "name=EV6&status=off");
                    Electrovalves.setElectrovalvesStatus(5, false);
                }
                else {
                    intent.putExtra("LINK", 
					                Electrovalves.URL_ELECTROVALVES_POST_BASE);
                    intent.putExtra("PARAM", "name=EV6&status=on");
                    Electrovalves.setElectrovalvesStatus(5, true);
                }
                startService(intent);
            }
        });
    }

	/**
     * The function handle the request to toggle the electrovalve number 5
     */
    private void handleToggleEV5() {
        Toast.makeText(this, "Toggle EV5", Toast.LENGTH_SHORT).show();
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(MenuActivity.this, HTTPService.class);
                intent.putExtra("METHOD","POST");
                if(Electrovalves.getElectrovalveStatus(4)) {
                    intent.putExtra("LINK", 
					                Electrovalves.URL_ELECTROVALVES_POST_BASE);
                    intent.putExtra("PARAM", "name=EV5&status=off");
                    Electrovalves.setElectrovalvesStatus(4, false);
                }
                else {
                    intent.putExtra("LINK", 
					                Electrovalves.URL_ELECTROVALVES_POST_BASE);
                    intent.putExtra("PARAM", "name=EV5&status=on");
                    Electrovalves.setElectrovalvesStatus(4, true);
                }
                startService(intent);
            }
        });
    }

	/**
     * The function handle the request to toggle the electrovalve number 4
     */
    private void handleToggleEV4() {
        Toast.makeText(this, "Toggle EV4", Toast.LENGTH_SHORT).show();
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(MenuActivity.this, HTTPService.class);
                intent.putExtra("METHOD", "POST");
                if(Electrovalves.getElectrovalveStatus(3)) {
                    intent.putExtra("LINK", 
					                Electrovalves.URL_ELECTROVALVES_POST_BASE);
                    intent.putExtra("PARAM", "name=EV4&status=off");
                    Electrovalves.setElectrovalvesStatus(3, false);
                }
                else {
                    intent.putExtra("LINK", 
					                Electrovalves.URL_ELECTROVALVES_POST_BASE);
                    intent.putExtra("PARAM", "name=EV4&status=on");
                    Electrovalves.setElectrovalvesStatus(3, true);
                }
                startService(intent);
            }
        });
    }

	/**
     * The function handle the request to toggle the electrovalve number 3
     */
    private void handleToggleEV3() {
        Toast.makeText(this, "Toggle EV3", Toast.LENGTH_SHORT).show();
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(MenuActivity.this, HTTPService.class);
                intent.putExtra("METHOD", "POST");
                if(Electrovalves.getElectrovalveStatus(2)) {
                    intent.putExtra("LINK", 
					                Electrovalves.URL_ELECTROVALVES_POST_BASE);
                    intent.putExtra("PARAM", "name=EV3&status=off");
                    Electrovalves.setElectrovalvesStatus(2, false);
                }
                else {
                    intent.putExtra("LINK", 
					                Electrovalves.URL_ELECTROVALVES_POST_BASE);
                    intent.putExtra("PARAM", "name=EV3&status=on");
                    Electrovalves.setElectrovalvesStatus(2, true);
                }
                startService(intent);
            }
        });
    }

	/**
     * The function handle the request to toggle the electrovalve number 2
     */
    private void handleToggleEV2() {
        Toast.makeText(this, "Toggle EV2", Toast.LENGTH_SHORT).show();
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(MenuActivity.this, HTTPService.class);
                intent.putExtra("METHOD", "POST");
                if(Electrovalves.getElectrovalveStatus(1)) {
                    intent.putExtra("LINK", 
					                Electrovalves.URL_ELECTROVALVES_POST_BASE);
                    intent.putExtra("PARAM", "name=EV2&status=off");
                    Electrovalves.setElectrovalvesStatus(1, false);
                }
                else {
                    intent.putExtra("LINK", 
					                Electrovalves.URL_ELECTROVALVES_POST_BASE);
                    intent.putExtra("PARAM", "name=EV2&status=on");
                    Electrovalves.setElectrovalvesStatus(1, true);
                }
                startService(intent);
            }
        });
    }

	/**
     * The function handle the request to toggle the electrovalve number 1
     */
    private void handleToggleEV1() {
        Toast.makeText(this, "Toggle EV1", Toast.LENGTH_SHORT).show();
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(MenuActivity.this, HTTPService.class);
                intent.putExtra("METHOD","POST");
                if(Electrovalves.getElectrovalveStatus(0)) {
                    intent.putExtra("LINK",  
					                Electrovalves.URL_ELECTROVALVES_POST_BASE);
                    intent.putExtra("PARAM", "name=EV1&status=off");
                    Electrovalves.setElectrovalvesStatus(0, false);
                }
                else {
                    intent.putExtra("LINK",               
					                Electrovalves.URL_ELECTROVALVES_POST_BASE);
                    intent.putExtra("PARAM", "name=EV1&status=on");
                    Electrovalves.setElectrovalvesStatus(0, true);
                }
                startService(intent);
            }
        });
    }

	/**
     * The function handle the request to show the Drive Electrovalves card
     */
    private void handleViewElectrovalves() {
        Toast.makeText(this, "Electrovalves", Toast.LENGTH_SHORT).show();
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                Log.i(LIVE_CARD_TAG, "State = Electrovalves");
                AppManager.getInstance().setState(ELECTROVALVES);
            }
        });
    }

    private static void setOptionsMenuState(Menu menu, int menuItemId,  
	                                        boolean enabled){
        MenuItem menuItem = menu.findItem(menuItemId);
        menuItem.setVisible(enabled);
        menuItem.setEnabled(enabled);
    }
}
