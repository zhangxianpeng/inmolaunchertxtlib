package com.inmoglass.launcher.util;

import android.content.Context;

import com.inmoglass.launcher.R;
import com.inmoglass.launcher.base.BaseApplication;
import com.inmoglass.launcher.bean.Channel2;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Administrator
 * 桌面卡片排列顺序管理类
 */
public class LauncherManager {

    private static final String TAG = LauncherManager.class.getSimpleName();
    private static LauncherManager mLauncherManager;
    private Context mContext;

    private List<Channel2> appList;

    private final String[] packageNames = new String[]{
            "com.inmolens.inmomemo",
            "com.inmoglass.documents",
            "com.inmoglass.album",
            "com.yulong.coolcamera",
            "com.inmo.settings",
            "com.inmoglass.appstore",
            "com.ichano.athome.camera",
            "com.tencent.qqmusicpad",
            "com.autonavi.amapauto"
    };

    private final String[] packageActivities = new String[]{
            "com.inmolens.inmomemo.MainActivity",
            "com.inmoglass.documents.ui.MainActivity",
            "com.inmoglass.album.ui.MainActivity",
            "com.yulong.arcamera.MainActivity",
            "com.inmo.settings.MainActivity",
            "com.inmoglass.appstore.MainActivity",
            "com.ichano.athome.camera.LoadingActivity",
            "com.tencent.qqmusicpad.activity.AppStarterActivity",
            "com.autonavi.amapauto.MainMapActivity"
    };

    /**
     * 出厂预装
     */
    private static final String FACTORY_INSTALL_APP = "factoryInstallAppList";
    /**
     * yingyongshangdian
     */
    private static final String APP_STORE_INSTALL_APP = "appStoreInstallAppList";
    /**
     * disanfangzijianzhuang
     */
    private static final String THIRD_INSTALL_APP = "thirdInstallAppList";

    public static synchronized LauncherManager getInstance() {
        if (mLauncherManager == null) {
            mLauncherManager = new LauncherManager();
        }
        return mLauncherManager;
    }

    public LauncherManager() {
        this.mContext = BaseApplication.mContext;
    }

    /**
     * 写入出厂预装的应用
     */
    public void writeFactoryPresetApp() {
        appList = new ArrayList<>();
        appList.add(new Channel2(R.drawable.img_home_beiwanglu, R.drawable.icon_home_beiwanglu,
                mContext.getString(R.string.string_home_beiwanglu), packageNames[0], packageActivities[0], "", ""));
        appList.add(new Channel2(R.drawable.img_home_wendang, R.drawable.icon_file_word,
                mContext.getString(R.string.string_home_wendang), packageNames[1], packageActivities[1], "", ""));
        appList.add(new Channel2(R.drawable.img_home_meitiwenjian, R.drawable.icon_home_meiti,
                mContext.getString(R.string.string_home_media), packageNames[2], packageActivities[2], "", ""));
        appList.add(new Channel2(R.drawable.img_home_camera, R.drawable.icon_home_camera,
                mContext.getString(R.string.string_home_camera), packageNames[3], packageActivities[3], "", ""));
        appList.add(new Channel2(R.drawable.img_home_setting, R.drawable.icon_home_setting,
                mContext.getString(R.string.string_home_setting), packageNames[4], packageActivities[4], "", ""));
        appList.add(new Channel2(R.drawable.img_home_store, R.drawable.icon_home_store,
                mContext.getString(R.string.string_home_store), packageNames[5], packageActivities[5], "", ""));
        appList.add(new Channel2(R.drawable.img_home_kanjia, R.drawable.icon_home_kanjia,
                mContext.getString(R.string.string_home_kanjia), packageNames[6], packageActivities[6], "", ""));
        appList.add(new Channel2(R.drawable.img_home_qqmusic, R.drawable.icon_home_qqmusic,
                mContext.getString(R.string.string_home_qq_music), packageNames[7], packageActivities[7], "", ""));
        appList.add(new Channel2(R.drawable.img_home_gaode, R.drawable.icon_home_gaode,
                mContext.getString(R.string.string_home_gaode), packageNames[8], packageActivities[8], "", ""));

        MMKVUtils.setArray(mContext, appList, "appList");
    }

    public void writeAppStoreInstalledApp() {
        Channel2 channel2 = new Channel2();
        ArrayList<Channel2> result = MMKVUtils.getArray(mContext, "appList", channel2);
        // TODO: 2021/12/17 获取应用商店下载安装的应用列表，并写入本地
    }

    public void writeThirdInstalledApp() {

    }
}
