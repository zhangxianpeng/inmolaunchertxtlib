package com.inmoglass.launcher.viewpager;

import android.content.Context;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import androidx.viewpager.widget.ViewPager;

import com.blankj.utilcode.util.LogUtils;
import com.inmoglass.launcher.R;
import com.inmoglass.launcher.adapter.ViewPagerAdapter;
import com.inmoglass.launcher.util.AppUtil;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Administrator
 * 自定义View实现banner
 */
public class BannerLayout extends RelativeLayout {
    private static final String TAG = BannerLayout.class.getSimpleName();
    private LinearLayout indicatorLinearLayout;
    private ViewPager mViewpager;
    private ViewPagerAdapter adapter;
    private List<View> viewList = new ArrayList<>();

    private int mCurrentItem = 0;
    private boolean isNeedToGoSetting = false;

    GestureDetector mGestureDetector;

    public enum UI_STATE {
        //====================手机通知====================//
        /**
         * 手机通知--从未连接过手机APP
         */
        PHONE_NOTIFICATION_NOT_CONNECTED_INMOLENS_APP,
        /**
         * 手机通知--眼镜是否跟手机在连接中
         */
        PHONE_NOTIFICATION_CONNECTING_PHONE,
        /**
         * 手机通知--眼镜是否跟InmolensAPP在连接中
         */
        PHONE_NOTIFICATION_IS_CONNECTING_INMOLENS_APP,
        /**
         * 手机通知--手机端开启通知的应用数量是否为0
         */
        PHONE_NOTIFICATION_APP_COUNT,
        //====================手机通知====================//

        //====================手机镜像====================//
        /**
         * 手机镜像--从未接收过手机镜像
         */
        PHONE_MIRROR_NOT_CONNECTED_PHONE,
        /**
         * 手机镜像--眼镜是否跟手机在连接中
         */
        PHONE_MIRROR_CONNECTING_PHONE,
        /**
         * 手机镜像--眼镜是否跟InmolensAPP在连接中
         */
        PHONE_MIRROR_IS_CONNECTING_INMOLENS_APP,
        /**
         * 手机镜像--眼镜是否与手机连接同一个WLAN
         */
        PHONE_MIRROR_IS_CONNECTING_SAME_WLAN_WITH_PHONE,
        /**
         * 手机镜像--正在镜像中
         */
        PHONE_MIRROR_IS_MIRRORING,
        /**
         * 手机镜像--未在镜像中
         */
        PHONE_MIRROR_IS_NOT_MIRRORING,
        //====================手机镜像====================//

        //====================第三方视频====================//
        /**
         * 视频--是否接收过视频镜像推送
         */
        VIDEO_MIRROR_IS_MIRRORED,
        /**
         * 视频--眼镜是否与手机连接同一个WLAN
         */
        VIDEO_MIRROR_IS_CONNECTING_SAME_WLAN_WITH_PHONE,
        /**
         * 视频--投屏服务是否开启
         */
        VIDEO_MIRROR_IS_RECEIVE_SERVICE_RUNNING,
        /**
         * 视频--视频推送中
         */
        VIDEO_MIRROR_IS_MIRRORING,
        //====================第三方视频====================//
    }

    public BannerLayout(Context context) {
        this(context, null);
    }

    public BannerLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        View view = LayoutInflater.from(context).inflate(R.layout.layout_banner, this, true);
        mViewpager = view.findViewById(R.id.viewpager);
        indicatorLinearLayout = view.findViewById(R.id.ll_indicator);

        mGestureDetector = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onSingleTapUp(MotionEvent e) {
                if ((mCurrentItem == 1 && isNeedToGoSetting) || (mCurrentItem == 0 && isNeedToGoSetting)) {
                    LogUtils.i(TAG, "onSingleTapUp= " + mCurrentItem);
                    AppUtil.getInstance().openApplicationByPkgName("com.inmo.settings");
                }
                return true;
            }
        });
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        mGestureDetector.onTouchEvent(ev);
        MotionEvent ev2 = MotionEvent.obtain(ev.getDownTime(), ev.getEventTime(), ev.getAction(), -(ev.getX()), ev.getY(), ev.getMetaState());
        mViewpager.dispatchTouchEvent(ev2);
        return true;
    }

    /**
     * 区分显示的view
     *
     * @param context
     * @param state   状态
     * @param type    预留字段
     */
    public void setData(Context context, UI_STATE state, int type) {
        viewList.clear();
        switch (state) {
            case PHONE_NOTIFICATION_NOT_CONNECTED_INMOLENS_APP:
                isNeedToGoSetting = false;
                View notification_guideView1 = LayoutInflater.from(context).inflate(R.layout.layout_banner_notification_guide1, null);
                View notification_guideView2 = LayoutInflater.from(context).inflate(R.layout.layout_banner_notification_guide2, null);
                View notification_guideView3 = LayoutInflater.from(context).inflate(R.layout.layout_banner_notification_guide3, null);
                View notification_guideView4 = LayoutInflater.from(context).inflate(R.layout.layout_banner_notification_guide4, null);
                View notification_guideView5 = LayoutInflater.from(context).inflate(R.layout.layout_banner_notification_guide5, null);
                viewList.add(notification_guideView1);
                viewList.add(notification_guideView2);
                viewList.add(notification_guideView3);
                viewList.add(notification_guideView4);
                viewList.add(notification_guideView5);
                break;
            case PHONE_MIRROR_NOT_CONNECTED_PHONE:
                isNeedToGoSetting = false;
                View mirror_guide1 = LayoutInflater.from(context).inflate(R.layout.layout_banner_notification_guide1, null);
                View mirror_guide2 = LayoutInflater.from(context).inflate(R.layout.layout_banner_notification_guide2, null);
                View mirror_guide3 = LayoutInflater.from(context).inflate(R.layout.layout_banner_notification_guide3, null);
                View mirror_guide4 = LayoutInflater.from(context).inflate(R.layout.layout_banner_mirror_guide4, null);
                View mirror_guide5 = LayoutInflater.from(context).inflate(R.layout.layout_banner_mirror_guide5, null);
                viewList.add(mirror_guide1);
                viewList.add(mirror_guide2);
                viewList.add(mirror_guide3);
                viewList.add(mirror_guide4);
                viewList.add(mirror_guide5);
                break;
            case VIDEO_MIRROR_IS_MIRRORED:
                isNeedToGoSetting = true;
                View video_guide1 = LayoutInflater.from(context).inflate(R.layout.layout_banner_video_guide1, null);
                View video_guide2 = LayoutInflater.from(context).inflate(R.layout.layout_banner_video_guide2, null);
                View video_guide3 = LayoutInflater.from(context).inflate(R.layout.layout_banner_video_guide3, null);
                View video_guide4 = LayoutInflater.from(context).inflate(R.layout.layout_banner_video_guide4, null);
                View video_guide5 = LayoutInflater.from(context).inflate(R.layout.layout_banner_video_guide5, null);
                viewList.add(video_guide1);
                viewList.add(video_guide2);
                viewList.add(video_guide3);
                viewList.add(video_guide4);
                viewList.add(video_guide5);
                break;
            case PHONE_NOTIFICATION_CONNECTING_PHONE:
            case PHONE_MIRROR_CONNECTING_PHONE:
                isNeedToGoSetting = false;
                View bleView = LayoutInflater.from(context).inflate(R.layout.layout_banner_notification_guide3, null);
                viewList.add(bleView);
                break;
            case PHONE_NOTIFICATION_IS_CONNECTING_INMOLENS_APP:
            case PHONE_MIRROR_IS_CONNECTING_INMOLENS_APP:
                isNeedToGoSetting = false;
                View connectView = LayoutInflater.from(context).inflate(R.layout.layout_banner_inmolens, null);
                viewList.add(connectView);
                break;
            case PHONE_MIRROR_IS_CONNECTING_SAME_WLAN_WITH_PHONE:
                isNeedToGoSetting = true;
                View sameWlanView = LayoutInflater.from(context).inflate(R.layout.layout_banner_notice_mirror, null);
                viewList.add(sameWlanView);
                break;
            case PHONE_MIRROR_IS_MIRRORING:
                isNeedToGoSetting = false;
                View isMirroringView = LayoutInflater.from(context).inflate(R.layout.layout_banner_is_mirroring, null);
                viewList.add(isMirroringView);
                break;
            case PHONE_MIRROR_IS_NOT_MIRRORING:
                isNeedToGoSetting = false;
                View isNotMirroringView = LayoutInflater.from(context).inflate(R.layout.layout_banner_document_mirror, null);
                viewList.add(isNotMirroringView);
                break;
            case PHONE_NOTIFICATION_APP_COUNT:
                isNeedToGoSetting = false;
                View notificationView = LayoutInflater.from(context).inflate(R.layout.layout_banner_notification_count, null);
                viewList.add(notificationView);
                break;
            case VIDEO_MIRROR_IS_CONNECTING_SAME_WLAN_WITH_PHONE:
                isNeedToGoSetting = true;
                View wifiView = LayoutInflater.from(context).inflate(R.layout.layout_banner_video_guide2, null);
                viewList.add(wifiView);
                break;
            case VIDEO_MIRROR_IS_RECEIVE_SERVICE_RUNNING:
                isNeedToGoSetting = false;
                View videoMirrorView = LayoutInflater.from(context).inflate(R.layout.layout_banner_video_guide6, null);
                viewList.add(videoMirrorView);
                break;
            case VIDEO_MIRROR_IS_MIRRORING:
                isNeedToGoSetting = false;
                View videoMirroringView = LayoutInflater.from(context).inflate(R.layout.layout_banner_video_mirroring, null);
                viewList.add(videoMirroringView);
                break;
            default:
                break;
        }

        adapter = new ViewPagerAdapter(viewList);
        setViewPagerScrollSpeed(context);
        mViewpager.setAdapter(adapter);
        setIndicator();

        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }
    }

    /**
     * 设置ViewPager的滑动速度
     */
    private void setViewPagerScrollSpeed(Context context) {
        try {
            Field mScroller = null;
            mScroller = ViewPager.class.getDeclaredField("mScroller");
            mScroller.setAccessible(true);
            FixedSpeedScroller scroller = new FixedSpeedScroller(context);
            mScroller.set(mViewpager, scroller);
        } catch (NoSuchFieldException e) {

        } catch (IllegalArgumentException e) {

        } catch (IllegalAccessException e) {

        }
    }

    private void setIndicator() {
        View viewIndicator;
        // 单独一个界面的时候不需要展示指示器
        if (viewList.size() < 2) {
            indicatorLinearLayout.setVisibility(GONE);
        } else {
            indicatorLinearLayout.setVisibility(VISIBLE);
            indicatorLinearLayout.removeAllViews();
        }
        for (int i = 0; i < viewList.size(); i++) {
            // 创建imageview作为小圆点
            viewIndicator = new View(getContext());
            // 设置默认背景
            viewIndicator.setBackgroundResource(R.drawable.indicator_bg);
            viewIndicator.setEnabled(false);
            // 设置指示器宽高
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(8, 8);
            // 除了第一个小圆点，其他小圆点都设置边距
            if (i != 0) {
                layoutParams.leftMargin = 10;
            }
            // 设置布局参数
            viewIndicator.setLayoutParams(layoutParams);
            // 添加指示器到布局
            indicatorLinearLayout.addView(viewIndicator);
        }

        // 默认选中第一个指示器
        indicatorLinearLayout.getChildAt(0).setEnabled(true);

        mViewpager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int i) {
                indicatorLinearLayout.getChildAt(mCurrentItem).setEnabled(false);
                indicatorLinearLayout.getChildAt(i).setEnabled(true);
                mCurrentItem = i;
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }
}
