package com.inmoglass.otaupgrade;

import android.app.Application;

import com.tencent.mmkv.MMKV;

public class App extends Application {
    private static Application instance;
    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        MMKV.initialize(this);
    }

    public static Application getContext() {
        return instance;
    }
}
