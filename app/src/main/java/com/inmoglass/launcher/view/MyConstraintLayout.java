package com.inmoglass.launcher.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.blankj.utilcode.util.LogUtils;

/**
 * 自定义约束布局
 * 添加手势监听
 */
public class MyConstraintLayout extends ConstraintLayout {

    private GestureDetector mGestureDetector;

    private static final float FLIP_DISTANCE = 150;
    private float mDownX = 0;

    public MyConstraintLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        mGestureDetector = new GestureDetector(context, gestureListener);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_UP) {
            LogUtils.d("ACTION_UP");
            if (mOnGestureListener != null) {
                mOnGestureListener.onEnsure();
            }
        }
        if (mGestureDetector != null) {
            mGestureDetector.onTouchEvent(event);
        }
        return super.onTouchEvent(event);
    }

    GestureDetector.OnGestureListener gestureListener = new GestureDetector.SimpleOnGestureListener() {
        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            if (e1 == null || e2 == null) {
                return true;
            }
            LogUtils.d("e1 x " + e1.getX() + " e2 x " + e2.getX() + " distanceX " + distanceX);
            float e2X = e2.getX();
            float distance = e2X - mDownX;
            if (distance > 0) {
                LogUtils.d("向前滑动");
                if (Math.abs(distance) > FLIP_DISTANCE) {
                    LogUtils.d("move 向左边移动一格");
                    if (mOnGestureListener != null) {
                        mOnGestureListener.onSwipeLeft();
                    }
                    mDownX = e2X;
                }
            } else {
                LogUtils.d("向后滑动");
                if (Math.abs(distance) > FLIP_DISTANCE) {
                    LogUtils.d("move 向右边移动一格");
                    if (mOnGestureListener != null) {
                        mOnGestureListener.onSwipeRight();
                    }
                    mDownX = e2X;
                }
            }
            return true;
        }

        @Override
        public boolean onDown(MotionEvent e) {
            LogUtils.d("用户按下");
            mDownX = e.getX();
            return true;
        }

        @Override
        public boolean onSingleTapConfirmed(MotionEvent e) {
            if (mOnGestureListener != null) {
                mOnGestureListener.onCancel();
            }
            return true;
        }
    };

    private OnGestureListener mOnGestureListener;

    public void setListener(OnGestureListener onGestureListener) {
        this.mOnGestureListener = onGestureListener;
    }

    public interface OnGestureListener {
        void onSwipeLeft();

        void onSwipeRight();

        void onCancel();

        void onEnsure();
    }
}
