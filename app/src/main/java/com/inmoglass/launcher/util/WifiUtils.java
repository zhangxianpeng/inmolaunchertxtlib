package com.inmoglass.launcher.util;

import static android.content.Context.WIFI_SERVICE;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.DhcpInfo;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;

import androidx.annotation.NonNull;

import com.blankj.utilcode.util.LogUtils;
import com.inmoglass.launcher.global.AppGlobals;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * @author Administrator
 * WiFi 连接网络的SSID
 */
public class WifiUtils {

    private static final String TAG = WifiUtils.class.getSimpleName();

    /**
     * 检测眼镜的wlan和手机的wlan是否是同一个
     *
     * @param context
     * @return
     */
    public static boolean isSameWlanWithPhone(Context context) {
        String glassConnectWifiName = getConnectWifiSsid(context);
        String phoneConnectWifiName = MMKVUtils.getString(AppGlobals.PHONE_WIFI_NAME);
        LogUtils.i(TAG, "glassConnectWifiName = " + glassConnectWifiName + ",phoneConnectWifiName = " + phoneConnectWifiName);
        return glassConnectWifiName.equals(phoneConnectWifiName);
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
        LogUtils.d(TAG, "眼镜端连接的wifi名" + wifiInfo.getSSID());
        // 移除前后的双引号
        return wifiInfo.getSSID().substring(1, wifiInfo.getSSID().length() - 1);
    }

    public static String getWifiIpAddress(@NonNull Context context) {
        WifiManager wifiManager = (WifiManager) context.getSystemService(WIFI_SERVICE);
        DhcpInfo info = wifiManager.getDhcpInfo();
        int ipAddress = info.ipAddress;
        return (ipAddress & 0xff) + "." + (ipAddress >> 8 & 0xff) + "." + (ipAddress >> 16 & 0xff) + "." + (ipAddress >> 24 & 0xff);
    }

    public static boolean isWifiApOpen(Context context) {
        try {
            WifiManager manager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
            //通过放射获取 getWifiApState()方法
            Method method = manager.getClass().getDeclaredMethod("getWifiApState");
            //调用getWifiApState() ，获取返回值
            int state = (int) method.invoke(manager);
            //通过放射获取 WIFI_AP的开启状态属性
            Field field = manager.getClass().getDeclaredField("WIFI_AP_STATE_ENABLED");
            //获取属性值
            int value = (int) field.get(manager);
            //判断是否开启
            return state == value;
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }
        return false;
    }

}
