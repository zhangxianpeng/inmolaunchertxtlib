package com.inmoglass.launcher.util;

import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.widget.TextView;

import com.blankj.utilcode.util.LogUtils;
import com.inmoglass.launcher.R;
import com.inmoglass.launcher.tts.TtsManager;
import com.inmoglass.launcher.ui.MainActivity;

/**
 * @author Administrator
 */
public class WindowUtils {
    private static final String TAG = WindowUtils.class.getSimpleName();
    private static View mView = null;
    private static WindowManager mWindowManager = null;
    private static Context mContext = null;
    public static boolean isShown = false;
    private static Handler myHandler = new Handler(Looper.getMainLooper());

    public enum UI_STATE {
        /**
         * 备忘录
         */
        MEMO_STATE,
        IMAGE_STATE,
        /**
         * 电量过低
         */
        BATTERY_BELOW_6,
        /**
         * 关机倒计时
         */
        BATTERY_BELOW_2,
        /**
         * 充电中
         */
        CHARGING,
    }

    public static void showPopupWindow(Context context, UI_STATE state, final String content) {
        if (isShown) {
            LogUtils.i(TAG, "return cause already shown");
            return;
        }
        isShown = true;
        LogUtils.i(TAG, "showPopupWindow");
        // 获取应用的Context
        mContext = context.getApplicationContext();
        // 获取WindowManager
        mWindowManager = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
        mView = setUpView(context, state, content);
        final WindowManager.LayoutParams params = new WindowManager.LayoutParams();
        // 类型
        params.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        params.flags = WindowManager.LayoutParams.FLAG_DIM_BEHIND | WindowManager.LayoutParams.FLAG_BLUR_BEHIND;
        params.format = PixelFormat.TRANSLUCENT;
        params.width = LayoutParams.MATCH_PARENT;
        params.height = LayoutParams.MATCH_PARENT;
        params.gravity = Gravity.CENTER;
        mView.setOnTouchListener((view, motionEvent) -> {
            if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                hidePopupWindow();
            }
            return false;
        });
        mWindowManager.addView(mView, params);
        if (state == UI_STATE.CHARGING) {
            myHandler.postDelayed(() -> {
                mWindowManager.removeView(mView);
                isShown = false;
            }, 2000);
        }
        LogUtils.i(TAG, "add view");
    }

    /**
     * 隐藏弹出框
     */
    public static void hidePopupWindow() {
        LogUtils.i(TAG, "hide " + isShown + ", " + mView);
        if (isShown && null != mView) {
            LogUtils.i(TAG, "hidePopupWindow");
            mWindowManager.removeView(mView);
            isShown = false;
        }
    }

    private static View setUpView(final Context context, UI_STATE state, String content) {
        LogUtils.i(TAG, "setUp view");
        View view = null;
        if (state == UI_STATE.MEMO_STATE) {
            view = LayoutInflater.from(context).inflate(R.layout.activity_memo_show, null);
            TextView memoTextView = view.findViewById(R.id.tvMemoContent);
            memoTextView.setText(content);
            TtsManager.getInstance().init(context, new TtsManager.TtsListener() {
                @Override
                public void onInitSuccess() {
                    TtsManager.getInstance().playTTS(content);
                }

                @Override
                public void onInitFail() {

                }
            });

        } else if (state == UI_STATE.BATTERY_BELOW_6) {
            view = LayoutInflater.from(context).inflate(R.layout.layout_low_battery, null);
        } else if (state == UI_STATE.BATTERY_BELOW_2) {
            view = LayoutInflater.from(context).inflate(R.layout.layout_shutdown_countdown, null);
            TextView countDownTextView = view.findViewById(R.id.tvCountdownTime);
            CountDownTimer timer = new CountDownTimer(15000, 1000) {
                @Override
                public void onTick(long millisUntilFinished) {
                    countDownTextView.setText(millisUntilFinished / 1000 + "S");
                    LogUtils.i(TAG, "seconds remaining: " + millisUntilFinished / 1000);
                    if (MainActivity.isChargingNow) {
                        // 倒计时过程中插入充电器，倒计时弹层消失
                        hidePopupWindow();
                    }
                }

                @Override
                public void onFinish() {
                    // 倒计时结束后直接关机
                    if (!MainActivity.isChargingNow) {
                        Intent shutdownIntent = new Intent("com.android.systemui.keyguard.shutdown");
                        mContext.sendBroadcast(shutdownIntent);
                    }
                }
            };
            timer.start();
        } else if (state == UI_STATE.CHARGING) {
            view = LayoutInflater.from(context).inflate(R.layout.layout_charging, null);
            TextView batteryLevelTextView = view.findViewById(R.id.tvBatteryLevel);
            batteryLevelTextView.setText(content + "%");
        }
        return view;
    }
}
