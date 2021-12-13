package com.inmoglass.launcher.adapter;


import android.view.View;
import android.view.ViewGroup;

import androidx.viewpager.widget.PagerAdapter;

import java.util.List;

/**
 * @author Administrator
 * @github : https://192.168.3.113:8443/IMC-ROM/inmogallery2.git
 * @time : 2019/09/03
 * @desc : gallery 使用ViewPager+GridView实现左右滑动的组件
 */
public class ViewPagerAdapter extends PagerAdapter {

    private List<View> mViewList;

    public ViewPagerAdapter(List<View> mViewList) {
        this.mViewList = mViewList;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView(mViewList.get(position));
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        container.addView(mViewList.get(position));
        return (mViewList.get(position));
    }

    @Override
    public int getCount() {
        return mViewList == null ? 0 : mViewList.size();
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }
}
