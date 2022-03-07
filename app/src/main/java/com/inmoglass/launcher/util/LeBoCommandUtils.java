package com.inmoglass.launcher.util;

import com.blankj.utilcode.util.LogUtils;
import com.inmo.inmodata.message.Dispatcher;
import com.inmoglass.launcher.base.BaseApplication;

/**
 * @author Administrator
 * 根据不同的指令做不同的操作
 */
public class LeBoCommandUtils {
    private static final String TAG = LeBoCommandUtils.class.getSimpleName();

    public static final String[] commands = new String[]{"cmd", "ping", "ip"};
    /**
     * 开始镜像
     */
    public static final String START_MIRROR = "startMirror";
    /**
     * 结束镜像
     */
    public static final String STOP_MIRROR = "stopMirror";

    /**
     * 发送IP地址
     */
    public static void sendIpAddress() {
        String ipAddress = WifiUtils.getWifiIpAddress(BaseApplication.mContext);
        LogUtils.d(TAG, "眼镜端连接手机热点的ip地址：" + ipAddress);
        String ipCommand = commands[2] + ":" + ipAddress;
        BluetoothController.getInstance().sendMessage2Phone(ipCommand, Dispatcher.LEBO_CAST_COMMAND);
    }
}
