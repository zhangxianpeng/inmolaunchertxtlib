package com.inmoglass.launcher.broadcastreceiver;

import static com.inmoglass.launcher.global.AppGlobals.isFirstLaunchSystem;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.blankj.utilcode.util.LogUtils;

/**
 * @author Administrator
 * 监听开机广播
 */
public class BootBroadcastReceiver extends BroadcastReceiver {

    private static final String TAG = BootBroadcastReceiver.class.getSimpleName();
    private static final String ACTION_BOOT = "android.intent.action.BOOT_COMPLETED";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(ACTION_BOOT)) {
            // 开机启动完成
            LogUtils.d(TAG, "开机启动完成");
            isFirstLaunchSystem = true;
        }
    }
}