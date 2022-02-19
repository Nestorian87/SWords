package com.nestor87.swords.ui;

import android.app.Application;

import com.google.android.gms.ads.MobileAds;

public class App extends Application {
    private static App singleton;

    public App getInstance(){
        return singleton;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        singleton = this;

        MobileAds.initialize(this, initializationStatus -> {

        });
    }
}
