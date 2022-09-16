package com.codicts.onetaphelp;

import android.app.Application;
import android.content.Context;

import androidx.multidex.MultiDex;
import androidx.multidex.MultiDexApplication;

public class OneTapHelp extends MultiDexApplication {
    private static OneTapHelp singleton;

    private static OneTapHelp getInstance(){
        return singleton;
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }

    public void onCreate() {
        super.onCreate();
        singleton = this;
    }

}