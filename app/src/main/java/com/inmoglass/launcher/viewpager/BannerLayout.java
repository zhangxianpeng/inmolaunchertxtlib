package com.inmoglass.launcher.viewpager;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import androidx.viewpager.widget.ViewPager;

import com.inmoglass.launcher.R;
import com.inmoglass.launcher.adapter.ViewPagerAdapter;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Administrator
 * 自定义View实现banner
 */
public class BannerLayout extends RelativeLayout {

    private ViewPager mViewpager;
    private LinearLayout indicatorLinearLayout;

    private ViewPagerAdapter adapter;
    private List<View> viewList = new ArrayList<>();
    ;

    private int mCurrentItem = 0;

    public BannerLayout(Context context) {
        this(context, null);
    }

    public BannerLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        View view = LayoutInflater.from(context).inflate(R.layout.layout_banner, this, true);
        mViewpager = view.findViewById(R.id.viewpager);
        indicatorLinearLayout = view.findViewById(R.id.ll_indicator);
    }

    /**
     * 区分显示的view
     *
     * @param type 类型区分
     */
    public void setData(Context context, int type) {
        viewList.clear();
        switch (type) {
            case 0: // 从未连接过Inmolens
                View guideView1 = LayoutInflater.from(context).inflate(R.layout.layout_banner_item1, null);
                View guideView2 = LayoutInflater.from(context).inflate(R.layout.layout_banner_item2, null);
                View guideView3 = LayoutInflater.from(context).inflate(R.layout.layout_banner_item3, null);
                View guideView4 = LayoutInflater.from(context).inflate(R.layout.layout_banner_item4, null);
                View guideView5 = LayoutInflater.from(context).inflate(R.layout.layout_banner_item5, null);
                viewList.add(guideView1);
                viewList.add(guideView2);
                viewList.add(guideView3);
                viewList.add(guideView4);
                viewList.add(guideView5);
                break;
            case 1: // 蓝牙未连接手机
                View bleView = LayoutInflater.from(context).inflate(R.layout.layout_banner_item3, null);
                viewList.add(bleView);
                break;
            case 2:
                View connectView = LayoutInflater.from(context).inflate(R.layout.layout_banner_item6, null);
                viewList.add(connectView);
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
