package com.inmoglass.launcher.ui;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.constraintlayout.utils.widget.ImageFilterView;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.blankj.utilcode.util.LogUtils;
import com.inmoglass.launcher.R;
import com.inmoglass.launcher.base.BaseActivity;
import com.inmoglass.launcher.util.WriteLogFileUtil;

import java.util.ArrayList;

/**
 * @author Administrator
 * 关机、重启弹层
 */
public class PopupWindowActivity extends BaseActivity {
    private static final String TAG = PopupWindowActivity.class.getSimpleName();
    private int mCurrentIndex = 1;
    final private ArrayList<ImageFilterView> mBgList = new ArrayList<>();
    private ConstraintLayout containerLayout;

    /**
     * 用于区分单击还是左右滑动
     */
    private float x1 = 0;
    private float y1 = 0;
    private float x2 = 0;
    private float y2 = 0;
    private int MOVE_DISTANCE = 100;
    private boolean isSwipeLeft = false;
    private boolean isSwipeRight = false;

    /**
     * 判断是否激活
     */
    public static boolean isActive = false;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        isActive = true;
        setContentView(R.layout.activity_popup_window);
        containerLayout = findViewById(R.id.container);
        containerLayout.setOnTouchListener((view, motionEvent) -> {
            switch (motionEvent.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    LogUtils.i(TAG, "ACTION_DOWN");
                    x1 = motionEvent.getX();
                    y1 = motionEvent.getY();
                    if (!isSwipeLeft && !isSwipeRight) {
                        cancel();
                    }
                    break;
                case MotionEvent.ACTION_UP:
                    if (isSwipeRight && !isSwipeLeft) {
                        LogUtils.i(TAG, "右滑");
                        mCurrentIndex++;
                        moveAndShow();
                    }
                    if (isSwipeLeft && !isSwipeRight) {
                        LogUtils.i(TAG, "左滑");
                        mCurrentIndex--;
                        moveAndShow();
                    }
                    break;
                case MotionEvent.ACTION_MOVE:
                    LogUtils.i(TAG, "ACTION_MOVE");
                    x2 = motionEvent.getX();
                    y2 = motionEvent.getY();

                    if (x1 - x2 > MOVE_DISTANCE) {
                        isSwipeRight = false;
                        isSwipeLeft = true;

                    } else if (x2 - x1 > MOVE_DISTANCE) {
                        isSwipeLeft = false;
                        isSwipeRight = true;
                    }
                    break;
                default:
                    break;
            }
            return true;
        });

        ImageFilterView ifvCancelSelectedBg = findViewById(R.id.ifvCancelSelectedBg);
        ImageFilterView ifvShutDownSelectedBg = findViewById(R.id.ifvShutDownSelectedBg);
        ImageFilterView ifvRebootSelectedBg = findViewById(R.id.ifvRebootSelectedBg);

        mBgList.add(ifvShutDownSelectedBg);
        mBgList.add(ifvCancelSelectedBg);
        mBgList.add(ifvRebootSelectedBg);
    }

    private void moveAndShow() {
        LogUtils.i("mCurrentIndex=" + mCurrentIndex);
        // 防止越界
        if (mCurrentIndex > 2) {
            mCurrentIndex = 2;
        } else if (mCurrentIndex < 0) {
            mCurrentIndex = 0;
        }
        for (int i = 0; i < 3; i++) {
            showOrHide(mBgList.get(i), mCurrentIndex == i);
        }
        useCurrentFunction();
    }

    private void showOrHide(@NonNull View v, boolean isShow) {
        v.setVisibility(isShow ? View.VISIBLE : View.INVISIBLE);
    }

    private void useCurrentFunction() {
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

    private void shutdownSystem() {
        // 写入文件测试是否收到这个广播,/sdcard/shutdownLog/data.txt
        WriteLogFileUtil.writeFile();
        Intent shutdownIntent = new Intent("com.android.systemui.keyguard.shutdown");
        sendBroadcast(shutdownIntent);
    }

    private void rebootSystem() {
        Intent rebootIntent = new Intent("com.android.systemui.keyguard.shutdown");
        rebootIntent.putExtra("reboot", true);
        sendBroadcast(rebootIntent);
    }

    private void cancel() {
        isActive = false;
        finish();
    }
}