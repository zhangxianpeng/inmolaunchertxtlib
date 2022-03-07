package com.inmoglass.launcher.lecast;

import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.text.TextUtils;

import com.blankj.utilcode.util.LogUtils;
import com.inmoglass.launcher.R;
import com.inmoglass.launcher.base.BaseApplication;
import com.inmoglass.launcher.global.AppGlobals;
import com.inmoglass.launcher.util.MMKVUtils;
import com.inmoglass.launcher.util.ToastUtil;
import com.inmoglass.launcher.util.WifiUtils;

/**
 * @author Administrator
 * 乐播服务控制器
 */
public class LeCastController {
    /**
     * 开始投屏接收端服务
     */
    public static void startCastServer(Context context) {
        boolean isApopen = MMKVUtils.getBoolean(AppGlobals.IS_PHONE_AP_OPEN);
        String phoneConnectWifiName = MMKVUtils.getString(AppGlobals.PHONE_WIFI_NAME);
        LogUtils.d("LeCastController", "isApopen =" + isApopen);
        LogUtils.d("LeCastController", "phoneConnectWifiName =" + phoneConnectWifiName);
        if (isApopen) {
            // 手机热点已经开启
            if (phoneConnectWifiName.equals("unknown ssid")) {
                String deviceMacAddress = MMKVUtils.getString(AppGlobals.BLUETOOTH_MAC_ADDRESS);
                LelinkHelper.getInstance(BaseApplication.mContext).startServer("Inmo\t" + Build.DEVICE + "-" + deviceMacAddress);
            } else {
                ((Activity) context).runOnUiThread(() -> ToastUtil.showToast(context, context.getString(R.string.string_same_network)));
            }
        } else {
            // 手机热点未开启
            if (!TextUtils.isEmpty(phoneConnectWifiName) && WifiUtils.isSameWlanWithPhone(BaseApplication.mContext)) {
                String deviceMacAddress = MMKVUtils.getString(AppGlobals.BLUETOOTH_MAC_ADDRESS);
                LelinkHelper.getInstance(BaseApplication.mContext).startServer("Inmo\t" + Build.DEVICE + "-" + deviceMacAddress);
            } else {
                ((Activity) context).runOnUiThread(() -> ToastUtil.showToast(context, context.getString(R.string.string_same_network)));
            }
        }
    }

    /**
     * 停止投屏服务，降低功耗
     */
    public static void stopCastServer() {
        LelinkHelper.getInstance(BaseApplication.mContext).stopServer();
    }
}
