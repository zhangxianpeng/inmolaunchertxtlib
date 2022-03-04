package com.inmoglass.launcher.global;

public class AppGlobals {
    /**
     * 是否已经播放过新手视频教程
     */
    public static final String NOVICE_TEACHING_VIDEO_PLAY_FLAG = "isPlayedVideo";

    /**
     * 是否已经连接过inmolensApp
     */
    public static final String GLASS_CONNECTED_INMOLENS_APP = "isConnectedInmolensApp";

    /**
     * 是否正在连接InmolensApp
     */
    public static final String GLASS_CONNECTING_INMOLENS_APP = "isConnectingInmolensApp";

    /**
     * 乐播接收端
     */
    public static final String APP_ID_RECEIVER = "20291";
    public static final String APP_SECRET_RECEIVER = "fb40422026e56467dc6e1d0c704e725a";

    /**
     * 此设备的蓝牙mac地址 40:45:DA:5E:E9:61
     * 保存到本地mmkv中
     */
    public static String BLUETOOTH_MAC_ADDRESS = "bluetooth_mac_address";

    /**
     * 眼镜是否接收到过手机镜像
     * true 不展示镜像教程 false 展示镜像教程
     */
    public static final String GLASS_CONNECTED_LE_CAST = "isConnectedLecast";

    /**
     * 眼镜端与手机端是否连接同一个wlan
     */
    public static final String CONNECTED_SAME_WLAN = "isConnectedSameWlan";

    /**
     * 接收端开启服务成功时的时间戳
     */
    public static long startLecastServiceTimestamp = 0L;

    /**
     * 断开服务的时间戳
     */
    public static long disconnectLecastServiceTimestamp = 0L;

    /**
     * 手机连接的wlan
     */
    public static String phoneConnectWifiName = "";

    /**
     * 是否接收过第三方APP推送的视频镜像
     */
    public static final String GLASS_CONNECTED_THIRD_APP_VIDEO = "isConnectedPhoneVideo";
}
