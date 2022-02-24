package com.inmoglass.launcher.ui;

import static android.view.View.VISIBLE;
import static com.inmoglass.launcher.global.AppGlobals.GLASS_CONNECTED_INMOLENS_APP;
import static com.inmoglass.launcher.global.AppGlobals.GLASS_CONNECTING_INMOLENS_APP;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;

import com.blankj.utilcode.util.LogUtils;
import com.inmo.ui.loading.LoadingView;
import com.inmoglass.launcher.R;
import com.inmoglass.launcher.base.BaseActivity;
import com.inmoglass.launcher.util.BtUtil;
import com.inmoglass.launcher.util.MMKVUtils;
import com.inmoglass.launcher.viewpager.BannerLayout;

/**
 * @author Administrator
 * 手机消息通知弹到眼镜设置教程
 */
public class NotificationCourseActivity extends BaseActivity {

    private static final String TAG = NotificationCourseActivity.class.getSimpleName();
    private LoadingView loading;
    private BannerLayout mBannerLayout;
    private LinearLayout mConnectingLinearLayout;
    private LinearLayout mConnectedLinearLayout;

    boolean isConnectingPhone = false;
    // 开启通知的应用数，这个问了剑文应该是静态图
    int notificationAppCount = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification_course);
        initViews();
        checkConnectStatus();
    }

    private void initViews() {
        loading = findViewById(R.id.loadingView);
        mBannerLayout = findViewById(R.id.bannerLayout);
        mConnectingLinearLayout = findViewById(R.id.ll_connecting_view);
        mConnectedLinearLayout = findViewById(R.id.ll_connected_view);

        loading.showLoadingView();
    }

    // 5.开启通知的应用数，需要展示到眼镜端
    private void checkConnectStatus() {
        // 1.判断蓝牙开关状态
        boolean isBleAvailable = BluetoothAdapter.getDefaultAdapter().isEnabled();
        if (!isBleAvailable) {
            LogUtils.d("蓝牙未打开，调用enable()");
            BluetoothAdapter.getDefaultAdapter().enable();
        } else {
            LogUtils.d("蓝牙已经打开");
        }

        // 2.判断是否连接过Inmolens
        boolean isConnectedInmolensApp = MMKVUtils.getBoolean(GLASS_CONNECTED_INMOLENS_APP);
        if (!isConnectedInmolensApp) {
            // 展示ViewPager
            LogUtils.d("重来没有连接过inmolens");
            loading.setVisibility(View.GONE);
            mBannerLayout.setVisibility(VISIBLE);
        }

        // 3.判断是否连接手机
        BluetoothDevice connectDevice = BtUtil.getConectedBluetoothDevice();
        if (connectDevice != null) {
            isConnectingPhone = BtUtil.getDeviceType(connectDevice.getBluetoothClass()) == BtUtil.PROFILE_PHONE;
        }

        if(!isConnectingPhone){
            // 未连接手机，展示教程连接到手机
        } else {

        }

        // 4.判断是否连接Inmolens
        boolean isConnectingInmolensApp = MMKVUtils.getBoolean(GLASS_CONNECTING_INMOLENS_APP);

    }
}