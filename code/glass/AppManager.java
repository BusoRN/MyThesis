package com.google.android.glass.sample.klabinterface;

/**
 * Created by alik on 12/2/2014.
 */
public class AppManager {

    private int state;

    private static  AppManager instance = new AppManager();

    public static AppManager getInstance(){
        return instance;
    }

    public void setState(int value){
        state = value;
    }

    public int getState(){
        return state;
    }

}
