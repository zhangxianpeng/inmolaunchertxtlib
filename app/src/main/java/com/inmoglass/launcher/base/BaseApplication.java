package com.inmoglass.launcher.base;

import android.app.Application;
import android.content.Context;

import com.blankj.utilcode.util.LogUtils;
import com.inmo.network.AndroidNetworking;
import com.qweather.sdk.view.HeConfig;
import com.tencent.mmkv.MMKV;

/**
 * @author Administrator
 */
public class BaseApplication extends Application {
    public static final String WEATHER_PRODUCT_ID = "HE1701090954491910";
    public static final String WEATHER_KEY = "b9754523666e4abeabef84808c64ed2b";
    public static Context mContext;

    @Override
    public void onCreate() {
        super.onCreate();
        MMKV.initialize(this);
        initWaetherSdk();
        mContext = this;
        // Log统一配置
        LogUtils.getConfig().setLogSwitch(true);
        // 是否写入文件的配置
//        LogUtils.getConfig().setLog2FileSwitch(true);
        AndroidNetworking.enableLogging();
    }

    private void initWaetherSdk() {
        HeConfig.init(WEATHER_PRODUCT_ID, WEATHER_KEY);
        // 切换至开发版服务
        HeConfig.switchToDevService();
    }
}
