package com.inmoglass.launcher.view;

import static android.content.Context.BATTERY_SERVICE;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;

import com.blankj.utilcode.util.LogUtils;
import com.inmoglass.launcher.R;
import com.inmoglass.launcher.impl.BatteryChangedListener;

import java.lang.ref.WeakReference;

/**
 * 自定义状态栏的电池
 *
 * @author Administrator
 * @date 2021-12-02
 */
public class StatusBarBatteryView extends androidx.appcompat.widget.AppCompatImageView {
    private static final String TAG = StatusBarBatteryView.class.getSimpleName();
    private BatteryManager batteryManager;
    private BatteryHandler handler;
    private BatteryChangedListener batteryChangedListener;

    private static final int MSG_BATTERY = 0x01;

    public StatusBarBatteryView(Context context) {
        this(context, null);
    }
    public StatusBarBatteryView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }
    public StatusBarBatteryView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        batteryManager = (BatteryManager) getContext().getSystemService(BATTERY_SERVICE);
        handler = new BatteryHandler(this);
        init();
    }

    private void init() {
        this.setImageResource(R.drawable.icon_battery01);
        int battery = batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY);
        LogUtils.i(TAG, "current battery: " + battery);
        Message.obtain(handler, MSG_BATTERY, battery, 0).sendToTarget();
    }

    private int getImageByBattery(int battery) {
        if (battery >= 0 && battery <= 25) {
            return R.drawable.icon_battery01;
        } else if (battery > 25 && battery <= 50) {
            return R.drawable.icon_battery02;
        } else if (battery > 50 && battery <= 75) {
            return R.drawable.icon_battery03;
        } else if (battery > 75) {
            return R.drawable.icon_battery04;
        } else {
            return R.drawable.icon_battery01;
        }
    }

    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            int level = (intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) * 100) / intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
            Message.obtain(handler, MSG_BATTERY, level, 0).sendToTarget();
            Log.i(TAG, "EXTRA_LEVEL: " + level);
            if (batteryChangedListener != null) {
                batteryChangedListener.onBatteryChanged(level);
            }
        }
    };

    private static class BatteryHandler extends Handler {
        WeakReference<StatusBarBatteryView> mView;

        public BatteryHandler(StatusBarBatteryView view) {
            this.mView = new WeakReference<StatusBarBatteryView>(view);
        }

        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            if (mView.get() == null) {
                return;
            }

            StatusBarBatteryView view = mView.get();
            if (view.getVisibility() == View.GONE) {
                view.setVisibility(View.VISIBLE);
            }
            switch (msg.what) {
                case MSG_BATTERY:
                    view.setImageResource(view.getImageByBattery(msg.arg1));
                    break;
            }
        }
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        IntentFilter intentFilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        getContext().registerReceiver(receiver, intentFilter);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        handler.removeCallbacksAndMessages(null);
        getContext().unregisterReceiver(receiver);
    }
}
