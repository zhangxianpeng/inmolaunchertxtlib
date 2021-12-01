package com.inmoglass.appModule;

import android.app.Application;


public class App extends Application {
    private static Application instance;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
    }

    public static Application getContext() {
        return instance;
    }
}
