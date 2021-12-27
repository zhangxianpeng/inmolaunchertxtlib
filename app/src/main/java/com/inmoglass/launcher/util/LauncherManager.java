package com.inmoglass.launcher.util;

import android.content.Context;

import com.inmoglass.launcher.R;
import com.inmoglass.launcher.base.BaseApplication;
import com.inmoglass.launcher.bean.Channel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author Administrator
 * 桌面卡片排列顺序管理类
 */
public class LauncherManager {
    private static final String TAG = LauncherManager.class.getSimpleName();
    private static LauncherManager mLauncherManager;
    private Context mContext;

    private List<String> appPackagesList;

    //首页应用顺序依次为: 相册、相机、备忘录、QQ音乐、喜马拉雅、高德地图、文档、WPS、设置
    public static final String[] packageNames = new String[]{
            "com.inmoglass.album",
            "com.yulong.coolcamera",
            "com.inmolens.inmomemo",
            "com.tencent.qqmusiccar",
            "com.ximalaya.ting.android.car",
            "com.autonavi.amapauto",
            "com.inmoglass.documents",
            "cn.wps.moffice_eng",
            "com.inmo.settings"
    };

    private static final String APP_PACKAGES_LIST = "AppList";

    public static synchronized LauncherManager getInstance() {
        if (mLauncherManager == null) {
            mLauncherManager = new LauncherManager();
        }
        return mLauncherManager;
    }

    public LauncherManager() {
        this.mContext = BaseApplication.mContext;
    }

    public void setLauncherCardList() {
        appPackagesList = new ArrayList<>();
        appPackagesList.addAll(Arrays.asList(packageNames));
        MMKVUtils.setArray(mContext, appPackagesList, APP_PACKAGES_LIST);
    }

    /**
     * 读取本地储存的APP卡片列表的顺序
     *
     * @return
     */
    public ArrayList<Channel> getLauncherCardList() {
        String packageName = "";
        ArrayList<String> packagesList = MMKVUtils.getArray(mContext, APP_PACKAGES_LIST, packageName);
        ArrayList<Channel> beanList = new ArrayList<>();
        for (String appPackage : packagesList) {
            Channel bean = null;
            if (appPackage.equals(packageNames[0])) {
                bean = new Channel(R.drawable.img_home_meitiwenjian, BaseApplication.mContext.getString(R.string.string_home_media), R.drawable.icon_home_meiti, packageNames[0]);
            } else if (appPackage.equals(packageNames[1])) {
                bean = new Channel(R.drawable.img_home_camera, BaseApplication.mContext.getString(R.string.string_home_camera), R.drawable.icon_home_camera, packageNames[1]);
            } else if (appPackage.equals(packageNames[2])) {
                bean = new Channel(R.drawable.img_home_beiwanglu, BaseApplication.mContext.getString(R.string.string_home_beiwanglu), R.drawable.icon_home_beiwanglu, packageNames[2]);
            } else if (appPackage.equals(packageNames[3])) {
                bean = new Channel(R.drawable.img_home_qqmusic, BaseApplication.mContext.getString(R.string.string_home_qq_music), R.drawable.icon_home_qqmusic, packageNames[3]);
            } else if (appPackage.equals(packageNames[4])) {
                bean = new Channel(R.drawable.img_home_ximalaya, BaseApplication.mContext.getString(R.string.string_home_ximalaya), R.drawable.icon_home_ximalaya, packageNames[4]);
            } else if (appPackage.equals(packageNames[5])) {
                bean = new Channel(R.drawable.img_home_gaode, BaseApplication.mContext.getString(R.string.string_home_gaode), R.drawable.icon_home_gaode, packageNames[5]);
            } else if (appPackage.equals(packageNames[6])) {
                bean = new Channel(R.drawable.img_home_wendang, BaseApplication.mContext.getString(R.string.string_home_wendang), R.drawable.icon_file_word, packageNames[6]);
            } else if (appPackage.equals(packageNames[7])) {
                bean = new Channel(R.drawable.img_home_wps, BaseApplication.mContext.getString(R.string.string_home_wps), R.drawable.icon_home_wps, packageNames[7]);
            } else if (appPackage.equals(packageNames[8])) {
                bean = new Channel(R.drawable.img_home_setting, BaseApplication.mContext.getString(R.string.string_home_setting), R.drawable.icon_home_setting, packageNames[8]);
            } else {
                bean = new Channel(R.drawable.img_home_kfc, AppUtil.getInstance().getAppName(appPackage), R.drawable.icon_home_kfc, appPackage);
            }
            beanList.add(bean);
        }
        return beanList;
    }

    /**
     * 按传入的顺序更新到本地文件
     *
     * @param channelList
     */
    public void updateCardList(ArrayList<Channel> channelList) {
        ArrayList<String> realList = new ArrayList<>();
        for (Channel channel : channelList) {
            realList.add(channel.getPackageName());
        }
        MMKVUtils.setArray(mContext, realList, APP_PACKAGES_LIST);
    }
}
