package com.inmoglass.launcher.view;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageView;

import com.inmoglass.launcher.R;

import java.lang.ref.WeakReference;

/**
 * 监听wifi变化图标
 */
public class StatusBarWifiStateView extends AppCompatImageView {

    private WifiManager wifiManager;

    private WifiHandler wifiHandler;

    // 没有开启Wifi或开启了Wifi但没有连接
    private static final int LEVEL_NONE = 0;

    // Wifi信号等级（最弱）
    private static final int LEVEL_1 = 1;

    // Wifi信号等级
    private static final int LEVEL_2 = 2;

    // Wifi信号等级
    private static final int LEVEL_3 = 3;

    // Wifi信号等级（最强）
    private static final int LEVEL_4 = 4;

    private static final String TAG = "WifiStateView";

    private static class WifiHandler extends Handler {

        // 虚引用
        private WeakReference<StatusBarWifiStateView> stateViewWeakReference;

        WifiHandler(StatusBarWifiStateView wifiStateView) {
            stateViewWeakReference = new WeakReference<>(wifiStateView);
        }

        @Override
        public void handleMessage(Message msg) {
            StatusBarWifiStateView wifiStateView = stateViewWeakReference.get();
            if (wifiStateView == null) {
                return;
            }
            switch (msg.what) {
                case LEVEL_2:
                    wifiStateView.setImageResource(R.drawable.ic_icon_statusbar_wifi02);
                    break;
                case LEVEL_3:
                    wifiStateView.setImageResource(R.drawable.ic_icon_statusbar_wifi03);
                    break;
                case LEVEL_4:
                    wifiStateView.setImageResource(R.drawable.ic_icon_statusbar_wifi04);
                    break;
                case LEVEL_NONE:
                default:
                    wifiStateView.setImageResource(R.drawable.ic_icon_statusbar_wifi01);
                    break;
            }
        }
    }

    private BroadcastReceiver wifiStateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction() != null) {

                switch (intent.getAction()) {
                    case WifiManager.WIFI_STATE_CHANGED_ACTION:
                        if (wifiManager.getWifiState() == WifiManager.WIFI_STATE_DISABLING) {
                            wifiHandler.sendEmptyMessage(LEVEL_NONE);
                        }
                        break;
                    case WifiManager.RSSI_CHANGED_ACTION:
                        if (wifiManager.getWifiState() == WifiManager.WIFI_STATE_DISABLED) {
                            wifiHandler.sendEmptyMessage(LEVEL_NONE);
                            return;
                        }
                        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
                        int level = WifiManager.calculateSignalLevel(wifiInfo.getRssi(), 5);

                        wifiHandler.sendEmptyMessage(level);
                        break;
                    default:
                        break;
                }
            }
        }
    };


    public StatusBarWifiStateView(Context context) {
        this(context, null);
    }

    public StatusBarWifiStateView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public StatusBarWifiStateView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        wifiHandler = new WifiHandler(this);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        IntentFilter intentFilter = new IntentFilter();
        // Wifi连接状态变化
        intentFilter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
        // Wifi信号强度变化
        intentFilter.addAction(WifiManager.RSSI_CHANGED_ACTION);
        getContext().registerReceiver(wifiStateReceiver, intentFilter);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        wifiHandler.removeCallbacksAndMessages(null);
        getContext().unregisterReceiver(wifiStateReceiver);
    }

}
