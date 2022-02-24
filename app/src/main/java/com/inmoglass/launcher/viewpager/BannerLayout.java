package com.inmoglass.launcher.viewpager;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.inmoglass.launcher.R;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Administrator
 * 自定义View实现banner
 */
public class BannerLayout extends RelativeLayout {

    private ViewPager mViewpager;
    private LinearLayout indicatorLinearLayout;
    private List<Integer> imageList;
    private int mCurrentItem = 0;

    public BannerLayout(Context context) {
        this(context, null);
    }

    public BannerLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        View view = LayoutInflater.from(context).inflate(R.layout.layout_banner, this, true);
        mViewpager = view.findViewById(R.id.viewpager);
        indicatorLinearLayout = view.findViewById(R.id.ll_indicator);
        imageList = new ArrayList<>();

        initData();
        initViewPager();
    }

    private void initData() {
        imageList.add(R.drawable.device_connect_01);
        imageList.add(R.drawable.device_connect_02);
        imageList.add(R.drawable.device_connect_03);
        imageList.add(R.drawable.device_connect_04);
    }

    private void initViewPager() {
        MyPagerAdapter adapter = new MyPagerAdapter();
        mViewpager.setAdapter(adapter);
        setIndicator();
    }

    private void setIndicator() {
        View viewIndicator;
        for (int i = 0; i < imageList.size(); i++) {
            // 创建imageview作为小圆点
            viewIndicator = new View(getContext());
            // 设置默认背景
            viewIndicator.setBackgroundResource(R.drawable.indicator_bg);
            viewIndicator.setEnabled(false);
            // 设置指示器宽高
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(20, 20);
            // 除了第一个小圆点，其他小圆点都设置边距
            if (i != 0) {
                layoutParams.leftMargin = 20;
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

    private class MyPagerAdapter extends PagerAdapter {

        @Override
        public int getCount() {
            return imageList.size();
        }

        @Override
        public boolean isViewFromObject(@NonNull View view, @NonNull Object o) {
            return view == o;
        }

        @NonNull
        @Override
        public Object instantiateItem(@NonNull ViewGroup container, int position) {
            ImageView iv = new ImageView(getContext());
            iv.setImageResource(imageList.get(position));
            container.addView(iv);
            return iv;
        }

        @Override
        public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
            container.removeView((View) object);
        }
    }
}
