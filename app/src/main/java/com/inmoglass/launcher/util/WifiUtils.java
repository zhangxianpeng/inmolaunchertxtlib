package com.inmoglass.launcher.util;

import static android.content.Context.WIFI_SERVICE;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;

import androidx.annotation.NonNull;

import com.blankj.utilcode.util.LogUtils;
import com.inmoglass.launcher.global.AppGlobals;

/**
 * @author Administrator
 * WiFi 连接网络的SSID
 */
public class WifiUtils {

    /**
     * 检测眼镜的wlan和手机的wlan是否是同一个
     *
     * @param context
     * @return
     */
    public static boolean isSameWlanWithPhone(Context context) {
        String glassConnectWifiName = getConnectWifiSsid(context);
        return glassConnectWifiName.equals(AppGlobals.phoneConnectWifiName);
    }

    /**
     * WiFi 是否连接
     *
     * @param context
     * @return
     */
    public static boolean isWifiConnect(Context context) {
        ConnectivityManager connManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo mWifiInfo = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        return mWifiInfo.isConnected();
    }

    /**
     * 获取连接的WiFi名称
     *
     * @param context
     * @return
     */
    public static String getConnectWifiSsid(@NonNull Context context) {
        WifiManager wifiManager = (WifiManager) context.getSystemService(WIFI_SERVICE);
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        LogUtils.d(wifiInfo.toString());
        LogUtils.d("得到的wifi名" + wifiInfo.getSSID());
        // 移除前后的双引号
        return wifiInfo.getSSID().substring(1, wifiInfo.getSSID().length() - 1);
    }
}
