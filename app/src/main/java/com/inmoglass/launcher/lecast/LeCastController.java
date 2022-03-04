package com.inmoglass.launcher.lecast;

import android.os.Build;

import com.inmoglass.launcher.base.BaseApplication;
import com.inmoglass.launcher.global.AppGlobals;
import com.inmoglass.launcher.util.MMKVUtils;

/**
 * @author Administrator
 * 乐播服务控制器
 */
public class LeCastController {

    /**
     * 开始投屏接收端服务
     */
    public static void startCastServer() {
        String deviceMacAddress = MMKVUtils.getString(AppGlobals.BLUETOOTH_MAC_ADDRESS);
        LelinkHelper.getInstance(BaseApplication.mContext).startServer("Inmo\t" + Build.DEVICE + "-" + deviceMacAddress);
    }

    /**
     * 停止投屏服务，降低功耗
     */
    public static void stopCastServer() {
        LelinkHelper.getInstance(BaseApplication.mContext).stopServer();
    }
}
