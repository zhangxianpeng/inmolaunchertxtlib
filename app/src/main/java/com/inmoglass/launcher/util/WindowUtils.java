package com.inmoglass.launcher.util;

import static com.inmoglass.launcher.global.AppGlobals.NOVICE_TEACHING_VIDEO_PLAY_FLAG;

import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.net.Uri;
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
import android.widget.VideoView;

import com.blankj.utilcode.util.LogUtils;
import com.inmoglass.launcher.R;
import com.inmoglass.launcher.base.BaseApplication;
import com.inmoglass.launcher.bean.ScreenFlagMsgBean;
import com.inmoglass.launcher.tts.TtsManager;
import com.inmoglass.launcher.ui.MainActivity;

import org.greenrobot.eventbus.EventBus;

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

    public static boolean isPlayingVideo = false;

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
        /**
         * 新手教程视频
         */
        BEGINNER_VIDEO,
    }

    public static void showPopupWindow(Context context, UI_STATE state, final String content) {
        if (isShown) {
            // fix bug:426 【系统】低电提醒界面，长按POWER键未调出关机选项
            hidePopupWindow();
        }
        isShown = true;
        LogUtils.d(TAG, "showPopupWindow");
        mContext = context.getApplicationContext();
        mWindowManager = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
        mView = setUpView(context, state, content);
        if (mView == null) {
            return;
        }
        final WindowManager.LayoutParams params = new WindowManager.LayoutParams();
        params.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        params.flags = WindowManager.LayoutParams.FLAG_DIM_BEHIND | WindowManager.LayoutParams.FLAG_BLUR_BEHIND;
        params.format = PixelFormat.TRANSLUCENT;
        params.width = LayoutParams.MATCH_PARENT;
        params.height = LayoutParams.MATCH_PARENT;
        params.gravity = Gravity.CENTER;
        params.setTitle("InmoShutDown");

        mView.setOnTouchListener((view, motionEvent) -> {
            if (motionEvent.getAction() == MotionEvent.ACTION_DOWN && (state != UI_STATE.BEGINNER_VIDEO)) {
                hidePopupWindow();
                if (mTimer != null) {
                    mTimer.cancel();
                    mTimer = null;
                }
            }
            return false;
        });

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
        LogUtils.d(TAG, "setUp view");
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
            view = LayoutInflater.from(context).inflate(R.layout.layout_second_confirm, null);
            initShutDownWindowView(view);
        } else if (state == UI_STATE.BEGINNER_VIDEO) {
            view = LayoutInflater.from(context).inflate(R.layout.layout_beginner_video, null);
            String uri = "android.resource://" + mContext.getPackageName() + "/" + R.raw.guide;
            initVideoView(view, uri);
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
        // fix bug:560 【低电量】1%低电量弹层，单击【我知道了】隐藏弹层后，15s后无法自动关机
        CountDownTimer lowBatteryTimer = new CountDownTimer(16000, 1000) {
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
        lowBatteryTimer.start();
    }
    /** ==================================低电量倒计时关机弹层===========================================**/

    /**
     * ==================================关机倒计时弹层===========================================
     **/
    static CountDownTimer mTimer;

    private static void initShutDownWindowView(View view) {
        // 2022.03.09 关机弹层优化 --- 直接展示二次确认弹窗，重启逻辑交由系统处理（监测power键按下时间）
        TextView countDownTextView = view.findViewById(R.id.tvCountdownTime);
        TextView tvMemoContent = view.findViewById(R.id.tvMemoContent);
        tvMemoContent.setText(BaseApplication.mContext.getString(R.string.string_shutdown_rightnow));
        TextView cancelText = view.findViewById(R.id.cancelText);
        cancelText.setText(BaseApplication.mContext.getString(R.string.string_shutdown_cancel));
        mTimer = new CountDownTimer(4000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                countDownTextView.setText(millisUntilFinished / 1000 + "S");
            }

            @Override
            public void onFinish() {
                // 倒计时结束后直接关机
                if (isShown) {
                    shutdownSystem();
                }
            }
        };
        mTimer.start();
    }

    private static void shutdownSystem() {
        // 写入文件测试是否收到这个广播,/sdcard/shutdownLog/data.txt
        WriteLogFileUtil.writeFile();
        Intent shutdownIntent = new Intent("com.android.systemui.keyguard.shutdown");
        BaseApplication.mContext.sendBroadcast(shutdownIntent);
    }

    /**
     * ==================================新手教程弹层===========================================
     **/
    private static void initVideoView(View view, String uri) {
        VideoView mVideoView = view.findViewById(R.id.videoView);
        forbiddenOperation(view);
        mVideoView.setVideoURI(Uri.parse(uri));
        mVideoView.setOnCompletionListener(mediaPlayer -> {
            LogUtils.d(TAG, "新手教学播放完成，保存标志位");
            isPlayingVideo = false;
            MMKVUtils.setBoolean(NOVICE_TEACHING_VIDEO_PLAY_FLAG, true);
            ScreenFlagMsgBean msg = new ScreenFlagMsgBean();
            msg.setNeedClear(true);
            EventBus.getDefault().post(msg);
            hidePopupWindow();
        });
        mVideoView.start();
        isPlayingVideo = true;
    }

    private static void forbiddenOperation(View view) {
        // 播放新手教程视频的时候禁用操作
        // 1.禁用下滑返回 keyBack事件
        // 2.禁用万能键事件
        // 3.禁用关机弹层
        // 4.禁用触摸板
        // 播放视频过程中不熄屏
        view.setOnTouchListener((view1, motionEvent) -> false);
    }
    /**==================================新手教程弹层===========================================*/
}
