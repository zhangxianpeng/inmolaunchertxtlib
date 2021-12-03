package com.inmoglass.coverflowlib;

import android.view.View;
import android.view.ViewGroup;

/**
 * @author Administrator
 */
public interface ICoverFlowAdapter {
    /**
     * 数量
     *
     * @return
     */
    int getCount();

    /**
     * 类型
     *
     * @param position
     * @return
     */
    Object getItem(int position);

    /**
     * id
     *
     * @param position
     * @return
     */
    long getItemId(int position);

    /**
     * 界面
     *
     * @param position
     * @param convertView
     * @param parent
     * @return
     */
    View getView(int position, View convertView, ViewGroup parent);

    /**
     * 数据
     *
     * @param view
     * @param position
     */
    void getData(View view, int position);
}
