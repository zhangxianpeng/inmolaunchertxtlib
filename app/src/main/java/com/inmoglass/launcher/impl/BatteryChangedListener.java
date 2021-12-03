package com.inmoglass.launcher.impl;

/**
 * 电量变化回调接口
 * @author Administrator
 * @date 2021-12-02
 */
public interface BatteryChangedListener {
    void onBatteryChanged(int level);
}
