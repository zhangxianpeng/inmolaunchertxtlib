package com.inmoglass.launcher.ui;

import static android.view.View.VISIBLE;
import static com.inmoglass.launcher.global.AppGlobals.GLASS_CONNECTED_INMOLENS_APP;
import static com.inmoglass.launcher.global.AppGlobals.GLASS_CONNECTED_LE_CAST;
import static com.inmoglass.launcher.global.AppGlobals.GLASS_CONNECTED_THIRD_APP_VIDEO;
import static com.inmoglass.launcher.global.AppGlobals.GLASS_CONNECTING_INMOLENS_APP;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.os.Bundle;
import android.view.View;

import com.blankj.utilcode.util.LogUtils;
import com.blankj.utilcode.util.ThreadUtils;
import com.inmo.ui.loading.LoadingView;
import com.inmoglass.launcher.R;
import com.inmoglass.launcher.base.BaseActivity;
import com.inmoglass.launcher.lecast.LeCastController;
import com.inmoglass.launcher.lecast.LelinkHelper;
import com.inmoglass.launcher.util.BtUtil;
import com.inmoglass.launcher.util.MMKVUtils;
import com.inmoglass.launcher.util.WifiUtils;
import com.inmoglass.launcher.viewpager.BannerLayout;

/**
 * @author Administrator
 * 教程入口
 */
public class NotificationCourseActivity extends BaseActivity {

    private static final String TAG = NotificationCourseActivity.class.getSimpleName();
    private LoadingView loading;
    private BannerLayout mBannerLayout;
    boolean isConnectingPhone = false;

    int notificationAppCount = 0;

    /**
     * 区分手机通知 0 、手机镜像 1、视频 2
     */
    int type = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification_course);
        type = getIntent().getIntExtra("type", -1);
        logType(type);
        initViews();

        checkConnectStatus();
    }

    private void logType(int type) {
        switch (type) {
            case 0:
                LogUtils.d(TAG, "手机通知教程");
                break;
            case 1:
                LogUtils.d(TAG, "手机镜像教程");
                break;
            case 2:
                LogUtils.d(TAG, "手机视频教程");
                break;
            default:
                break;
        }
    }

    private void initViews() {
        loading = findViewById(R.id.loadingView);
        mBannerLayout = findViewById(R.id.bannerLayout);
        loading.showLoadingView();
    }

    private void checkConnectStatus() {
        // 1.判断蓝牙开关状态
        boolean isBleAvailable = BluetoothAdapter.getDefaultAdapter().isEnabled();
        if (!isBleAvailable) {
            LogUtils.d(TAG, "蓝牙未打开，调用enable()");
            BluetoothAdapter.getDefaultAdapter().enable();
        } else {
            LogUtils.d(TAG, "蓝牙已经打开");
        }

        loading.setVisibility(View.GONE);
        mBannerLayout.setVisibility(VISIBLE);

        // 2.判断是否连接过Inmolens
        boolean isConnectedInmolensApp = MMKVUtils.getBoolean(GLASS_CONNECTED_INMOLENS_APP);
        LogUtils.d(TAG, "是否连接过Inmolens = " + isConnectedInmolensApp);

        // 3.判断是否连接手机
        BluetoothDevice connectDevice = BtUtil.getConectedBluetoothDevice();
        if (connectDevice != null) {
            isConnectingPhone = BtUtil.getDeviceType(connectDevice.getBluetoothClass()) == BtUtil.PROFILE_PHONE;
        }
        LogUtils.d(TAG, "判断是否连接手机 = " + isConnectingPhone);

        // 4.判断是否连接Inmolens
        boolean isConnectingInmolensApp = MMKVUtils.getBoolean(GLASS_CONNECTING_INMOLENS_APP);
        LogUtils.d(TAG, "判断Inmolens是否连接中 = " + isConnectingInmolensApp);

        // 5.是否投屏过手机镜像
        boolean isMirrored = MMKVUtils.getBoolean(GLASS_CONNECTED_LE_CAST);
        LogUtils.d(TAG, "判断是否投屏过手机镜像 = " + isMirrored);

        // 6.眼镜是否连接wlan
        boolean isConnectedWlan = WifiUtils.isWifiConnect(this);
        LogUtils.d(TAG, "眼镜是否连接wlan = " + isConnectedWlan);

        // 6.是否跟手机连接到同一个Wlan
        boolean isSameWlanWithPhone = WifiUtils.isSameWlanWithPhone(this);
        LogUtils.d(TAG, "是否跟手机连接到同一个Wlan = " + isSameWlanWithPhone);

        // 7.是否正在镜像中
        boolean isMirroring = LelinkHelper.isIsMirroring();
        LogUtils.d(TAG, "是否正在镜像中 = " + isMirroring);

        // 8.是否接收过视频镜像
        boolean isMirroredVideos = MMKVUtils.getBoolean(GLASS_CONNECTED_THIRD_APP_VIDEO);
        LogUtils.d(TAG, "是否接收过第三方APP视频镜像 = " + isMirroredVideos);

        if (type == 0) { // 手机通知
            if (!isConnectedInmolensApp) {
                LogUtils.d(TAG, "重来没有连接过inmolens");
                mBannerLayout.setData(this, BannerLayout.UI_STATE.PHONE_NOTIFICATION_NOT_CONNECTED_INMOLENS_APP, 0);
            } else {
                if (!isConnectingPhone) {
                    // 未连接手机，展示教程连接到手机
                    LogUtils.d(TAG, "未连接手机，展示教程连接到手机");
                    mBannerLayout.setData(this, BannerLayout.UI_STATE.PHONE_NOTIFICATION_CONNECTING_PHONE, 0);
                } else {
                    if (!isConnectingInmolensApp) {
                        mBannerLayout.setData(this, BannerLayout.UI_STATE.PHONE_NOTIFICATION_IS_CONNECTING_INMOLENS_APP, 0);
                    } else {
                        mBannerLayout.setData(this, BannerLayout.UI_STATE.PHONE_NOTIFICATION_APP_COUNT, 0);
                    }
                }
            }
        } else if (type == 1) { // 手机镜像
            if (!isMirrored) {
                LogUtils.d(TAG, "重来没有接收过手机端的投屏请求");
                mBannerLayout.setData(this, BannerLayout.UI_STATE.PHONE_MIRROR_NOT_CONNECTED_PHONE, 1);
            } else {
                LogUtils.d(TAG, "已经接收过投屏");
                if (!isConnectingPhone) {
                    LogUtils.d(TAG, "未连接手机，展示教程连接到手机");
                    mBannerLayout.setData(this, BannerLayout.UI_STATE.PHONE_MIRROR_CONNECTING_PHONE, 1);
                } else {
                    LogUtils.d(TAG, "手机正在连接中");
                    if (!isConnectingInmolensApp) {
                        mBannerLayout.setData(this, BannerLayout.UI_STATE.PHONE_MIRROR_IS_CONNECTING_INMOLENS_APP, 1);
                    } else {
                        LogUtils.d(TAG, "眼镜未连接InmolensApp");
                        if (!isConnectedWlan) {
                            LogUtils.d(TAG, "眼镜未连接wlan");
                            mBannerLayout.setData(this, BannerLayout.UI_STATE.PHONE_MIRROR_IS_CONNECTING_SAME_WLAN_WITH_PHONE, 1);
                        } else {
                            LogUtils.d(TAG, "眼镜已经连接wlan");
                            if (isMirroring) {
                                LogUtils.d(TAG, "正在镜像中");
//                                mBannerLayout.setData(this, BannerLayout.UI_STATE.PHONE_MIRROR_IS_MIRRORING, 1);
                            } else {
                                LogUtils.d(TAG, "未在镜像中");
                                mBannerLayout.setData(this, BannerLayout.UI_STATE.PHONE_MIRROR_IS_NOT_MIRRORING, 1);
                            }
                        }
                    }
                }
            }
        } else if (type == 2) { // 视频镜像
            if (!isMirroredVideos) {
                LogUtils.d(TAG, "重来没有接收过手机端第三方APP的推视频请求");
                mBannerLayout.setData(this, BannerLayout.UI_STATE.VIDEO_MIRROR_IS_MIRRORED, 2);
                startCastServerAsync();
            } else {
                if (!isConnectedWlan) {
                    LogUtils.d(TAG, "未连接WLAN");
                    mBannerLayout.setData(this, BannerLayout.UI_STATE.VIDEO_MIRROR_IS_CONNECTING_SAME_WLAN_WITH_PHONE, 2);
                } else {
                    LogUtils.d(TAG, "已经连接WLAN");
                    mBannerLayout.setData(this, BannerLayout.UI_STATE.VIDEO_MIRROR_IS_RECEIVE_SERVICE_RUNNING, 2);
                    if (!LelinkHelper.isLelinkServiceRunning) {
                        startCastServerAsync();
                    }
                }
            }
        }
    }

    private void startCastServerAsync() {
        ThreadUtils.getCachedPool().execute(() -> LeCastController.startCastServer(this));
    }
}