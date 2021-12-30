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

import androidx.constraintlayout.utils.widget.ImageFilterView;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.blankj.utilcode.util.LogUtils;
import com.blankj.utilcode.util.TouchUtils;
import com.inmoglass.launcher.R;
import com.inmoglass.launcher.base.BaseApplication;
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
         * 关机
         */
        SHUT_DOWN_ACTION,
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
        if (state != UI_STATE.SHUT_DOWN_ACTION) {
            // 关机的操作交给它本身
            mView.setOnTouchListener((view, motionEvent) -> {
                if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                    hidePopupWindow();
                }
                return false;
            });
        }
        mWindowManager.addView(mView, params);
        // 充电界面2S自动关闭
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
            view = LayoutInflater.from(context).inflate(R.layout.layout_memo_show, null);
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
            initTimerShutDownView(view);
        } else if (state == UI_STATE.CHARGING) {
            view = LayoutInflater.from(context).inflate(R.layout.layout_charging, null);
            TextView batteryLevelTextView = view.findViewById(R.id.tvBatteryLevel);
            batteryLevelTextView.setText(content + "%");
        } else if (state == UI_STATE.SHUT_DOWN_ACTION) {
            view = LayoutInflater.from(context).inflate(R.layout.layout_shutdown, null);
            initShutDownWindowView(view);
        }
        return view;
    }

    /**==================================低电量倒计时关机弹层===========================================**/
    private static void initTimerShutDownView(View view) {
        TextView countDownTextView = view.findViewById(R.id.tvCountdownTime);
        CountDownTimer timer = new CountDownTimer(16000, 1000) {
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
    }
    /** ==================================低电量倒计时关机弹层===========================================**/

    /**==================================关机重启弹层===========================================**/
    static boolean isSwipeLeft = false;
    static boolean isSwipeRight = false;
    static int mCurrentIndex = 1;
    static ImageFilterView ifvShutDownSelectedBg;
    static ImageFilterView ifvCancelSelectedBg;
    static ImageFilterView ifvRebootSelectedBg;

    private static void initShutDownWindowView(View view) {
        ifvShutDownSelectedBg = view.findViewById(R.id.ifvShutDownSelectedBg);
        ifvCancelSelectedBg = view.findViewById(R.id.ifvCancelSelectedBg);
        ifvRebootSelectedBg = view.findViewById(R.id.ifvRebootSelectedBg);

        ConstraintLayout layout = view.findViewById(R.id.container);
        layout.setOnTouchListener(new TouchUtils.OnTouchUtilsListener() {
            @Override
            public boolean onDown(View view, int x, int y, MotionEvent event) {
                LogUtils.i(TAG, "onDown");
                return false;
            }

            @Override
            public boolean onMove(View view, int direction, int x, int y, int dx, int dy, int totalX, int totalY, MotionEvent event) {
                LogUtils.i(TAG, "onMove = " + direction);
                if (direction == 1) {
                    // 左滑
                    isSwipeLeft = true;
                    isSwipeRight = false;
                } else if (direction == 4) {
                    // 右滑
                    isSwipeRight = true;
                    isSwipeLeft = false;
                }
                return false;
            }

            @Override
            public boolean onStop(View view, int direction, int x, int y, int totalX, int totalY, int vx, int vy, MotionEvent event) {
                LogUtils.i(TAG, "onStop");
                if (isSwipeLeft && !isSwipeRight) {
                    mCurrentIndex--;
                    moveAndShow();
                }
                if (isSwipeRight && !isSwipeLeft) {
                    mCurrentIndex++;
                    moveAndShow();
                }
                if (!isSwipeLeft && !isSwipeRight && mCurrentIndex == 1) {
                    cancel();
                }
                return false;
            }
        });
    }

    private static void moveAndShow() {
        // 防止越界
        if (mCurrentIndex > 2) {
            mCurrentIndex = 2;
        } else if (mCurrentIndex < 1) {
            mCurrentIndex = 0;
        }
        LogUtils.i("mCurrentIndex=" + mCurrentIndex);
        switch (mCurrentIndex) {
            case 0:
                ifvShutDownSelectedBg.setVisibility(View.VISIBLE);
                ifvCancelSelectedBg.setVisibility(View.GONE);
                ifvRebootSelectedBg.setVisibility(View.GONE);
                break;
            case 1:
                ifvShutDownSelectedBg.setVisibility(View.GONE);
                ifvCancelSelectedBg.setVisibility(View.VISIBLE);
                ifvRebootSelectedBg.setVisibility(View.GONE);
                break;
            case 2:
                ifvShutDownSelectedBg.setVisibility(View.GONE);
                ifvCancelSelectedBg.setVisibility(View.GONE);
                ifvRebootSelectedBg.setVisibility(View.VISIBLE);
                break;
            default:
                break;
        }
        useCurrentFunction();
    }

    private static void useCurrentFunction() {
        switch (mCurrentIndex) {
            case 0:
                shutdownSystem();
                break;
            case 2:
                rebootSystem();
                break;
            case 1:
            default:
                cancel();
                break;
        }
    }

    private static void shutdownSystem() {
        // 写入文件测试是否收到这个广播,/sdcard/shutdownLog/data.txt
        WriteLogFileUtil.writeFile();
        Intent shutdownIntent = new Intent("com.android.systemui.keyguard.shutdown");
        BaseApplication.mContext.sendBroadcast(shutdownIntent);
    }

    private static void rebootSystem() {
        Intent rebootIntent = new Intent("com.android.systemui.keyguard.shutdown");
        rebootIntent.putExtra("reboot", true);
        BaseApplication.mContext.sendBroadcast(rebootIntent);
    }

    private static void cancel() {
        mCurrentIndex = 1;
        hidePopupWindow();
    }
    /**==================================关机重启弹层===========================================*/

}
