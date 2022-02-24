package com.inmoglass.launcher.util;

/**
 * @author lijianwen
 * 蓝牙通话状态
 */
public class PhoneUtils {
    public static final String AG_CALL_CHANGED = "android.bluetooth.headsetclient.profile.action.AG_CALL_CHANGED";
    public static final  String ACTION_LAST_VTAG = "android.bluetooth.headsetclient.profile.action.LAST_VTAG";
    public static final String EXTRA_CALL = "android.bluetooth.headsetclient.extra.CALL";
    public static final String EXTRA_NUMBER = "android.bluetooth.headsetclient.extra.NUMBER";

    public static final String KEY_CALL_STATE = "mState";
    public static final String KEY_CALL_NUMBER = "mNumber";

    public static final String STATE_DIALING = "DIALING";//拨号
    public static final String STATE_ALERTING = "ALERTING";//响铃
    public static final String STATE_ACTIVE = "ACTIVE";//通话中
    public static final String STATE_TERMINATED = "TERMINATED";//通话结束
    public static final String STATE_INCOMING = "INCOMING";//通话结束
}
