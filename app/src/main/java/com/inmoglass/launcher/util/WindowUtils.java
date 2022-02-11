package com.inmoglass.launcher.util;

import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.os.PowerManager;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.widget.TextView;

import androidx.constraintlayout.utils.widget.ImageFilterView;

import com.blankj.utilcode.util.LogUtils;
import com.inmoglass.launcher.R;
import com.inmoglass.launcher.base.BaseApplication;
import com.inmoglass.launcher.tts.TtsManager;
import com.inmoglass.launcher.ui.MainActivity;
import com.inmoglass.launcher.view.MyConstraintLayout;

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
            // fix bug:426 【系统】低电提醒界面，长按POWER键未调出关机选项
            hidePopupWindow();
        }
        isShown = true;
        LogUtils.i(TAG, "showPopupWindow");
        mContext = context.getApplicationContext();
        mWindowManager = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
        mView = setUpView(context, state, content);
        final WindowManager.LayoutParams params = new WindowManager.LayoutParams();
        params.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        params.flags = WindowManager.LayoutParams.FLAG_DIM_BEHIND | WindowManager.LayoutParams.FLAG_BLUR_BEHIND;
        params.format = PixelFormat.TRANSLUCENT;
        params.width = LayoutParams.MATCH_PARENT;
        params.height = LayoutParams.MATCH_PARENT;
        params.gravity = Gravity.CENTER;
        params.setTitle("InmoShutDown");
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
     * 关闭低电量弹层
     *
     * @param state
     */
    public static void dismissLowBatteryWindow(UI_STATE state) {
        if (isShown && state == UI_STATE.BATTERY_BELOW_6) {
            hidePopupWindow();
        }
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
            isShowingMemory = false;
        }
    }

    private static View setUpView(final Context context, UI_STATE state, String content) {
        LogUtils.i(TAG, "setUp view");
        View view = null;
        if (state == UI_STATE.MEMO_STATE) {
            view = LayoutInflater.from(context).inflate(R.layout.layout_memo_show, null);
            initMemoView(view, content);
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

    /**
     * ==================================备忘录弹层===========================================
     **/
    static boolean isShowingMemory = false;

    private static void initMemoView(View view, String content) {
        TextView memoTextView = view.findViewById(R.id.tvMemoContent);
        memoTextView.setText(content);
        isShowingMemory = true;
        wakeUpScreen();
        playMemoContent(content);
        // fix bug: 325 时间触发备忘录事项提醒，只提醒1次。UX定义30S之后如果没有销毁这个View，重新语音提醒
        myHandler.postDelayed(() -> {
            if (isShowingMemory) {
                playMemoContent(content);
            }
        }, 30000);
    }

    private static void playMemoContent(String content) {
        TtsManager.getInstance().init(mContext, new TtsManager.TtsListener() {
            @Override
            public void onInitSuccess() {
                TtsManager.getInstance().playTTS(content);
            }

            @Override
            public void onInitFail() {

            }
        });
    }

    /**
     * 唤醒屏幕
     */
    private static void wakeUpScreen() {
        // 获取电源管理器对象
        PowerManager pm = (PowerManager) mContext.getSystemService(Context.POWER_SERVICE);
        // 获取PowerManager.WakeLock对象,后面的参数|表示同时传入两个值,最后的是Logcat里用的Tag
        PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.ACQUIRE_CAUSES_WAKEUP | PowerManager.SCREEN_DIM_WAKE_LOCK, "bright");
        // 点亮屏幕
        wl.acquire();
        // 释放
        wl.release();
    }

    /**==================================备忘录弹层===========================================**/

    /**
     * ==================================低电量倒计时关机弹层===========================================
     **/
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

    /**
     * ==================================关机重启弹层===========================================
     **/
    static int mCurrentIndex = 1;
    static ImageFilterView ifvShutDownSelectedBg;
    static ImageFilterView ifvCancelSelectedBg;
    static ImageFilterView ifvRebootSelectedBg;

    private static void initShutDownWindowView(View view) {
        ifvShutDownSelectedBg = view.findViewById(R.id.ifvShutDownSelectedBg);
        ifvCancelSelectedBg = view.findViewById(R.id.ifvCancelSelectedBg);
        ifvRebootSelectedBg = view.findViewById(R.id.ifvRebootSelectedBg);

        MyConstraintLayout layout = view.findViewById(R.id.container);
        // fix bug:343 【launcher】语音弹层唤醒，需要关闭关机弹层, View里面监听按键事件
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            layout.addOnUnhandledKeyEventListener((view1, keyEvent) -> {
                hidePopupWindow();
                return false;
            });
        }
        layout.setListener(new MyConstraintLayout.OnGestureListener() {
            @Override
            public void onSwipeLeft() {
                mCurrentIndex--;
                moveAndShow();
            }

            @Override
            public void onSwipeRight() {
                mCurrentIndex++;
                moveAndShow();
            }

            @Override
            public void onCancel() {
                cancel();
            }

            @Override
            public void onEnsure() {
                useCurrentFunction();
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
                SoundPoolUtil.getInstance(mContext).play(R.raw.reboot);
                break;
            case 1:
                ifvShutDownSelectedBg.setVisibility(View.GONE);
                ifvCancelSelectedBg.setVisibility(View.VISIBLE);
                ifvRebootSelectedBg.setVisibility(View.GONE);
                SoundPoolUtil.getInstance(mContext).play(R.raw.cancel);
                break;
            case 2:
                ifvShutDownSelectedBg.setVisibility(View.GONE);
                ifvCancelSelectedBg.setVisibility(View.GONE);
                ifvRebootSelectedBg.setVisibility(View.VISIBLE);
                SoundPoolUtil.getInstance(mContext).play(R.raw.shut_down);
                break;
            default:
                break;
        }
    }

    private static void useCurrentFunction() {
        switch (mCurrentIndex) {
            case 0:
                rebootSystem();
                break;
            case 2:
                shutdownSystem();
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
