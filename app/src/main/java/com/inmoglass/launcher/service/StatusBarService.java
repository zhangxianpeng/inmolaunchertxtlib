package com.inmoglass.launcher.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.view.WindowManager;

import androidx.annotation.Nullable;

import com.blankj.utilcode.util.LogUtils;
import com.inmoglass.launcher.view.StatusBarView;

/**
 * @author Administrator
 * @github : https://192.168.3.113:8443/IMC-ROM/imc-launcher.git
 * @time : 2019/09/14
 * @desc : 全局状态栏
 */
public class StatusBarService extends Service {

    private StatusBarView statusBarView;

    @Override
    public void onCreate() {
        super.onCreate();
        LogUtils.i("StatusBarService onCreate()");
        createIndicator();
    }

    private void createIndicator() {
        WindowManager windowManager = (WindowManager) getApplication().getSystemService(Context.WINDOW_SERVICE);
        statusBarView = new StatusBarView(this, windowManager);
        statusBarView.getContentView();
    }

    @Override
    public void onDestroy() {
        statusBarView.clearAll();
        statusBarView = null;
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
