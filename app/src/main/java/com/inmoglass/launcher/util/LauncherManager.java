package com.inmoglass.launcher.util;

import android.content.Context;

import com.inmoglass.launcher.R;
import com.inmoglass.launcher.base.BaseApplication;
import com.inmoglass.launcher.bean.Channel;
import com.inmoglass.launcher.data.LauncherCardDataDbHelper;

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

    /**
     * 中文版本默认应用顺序
     * 相册、相机、备忘录、QQ音乐、喜马拉雅、高德地图、文档、WPS、设置
     * 2022/02/22 新增需求：添加一个设备连接教程入口
     */
    public static final String[] packageNames = new String[]{
            "com.inmoglass.album",
            "com.yulong.coolcamera",
            "com.inmoglass.launcher.VideoMirrorActivity",
            "com.inmoglass.launcher.PhoneMirrorActivity",
            "com.inmolens.inmomemo",
            "com.tencent.qqmusiccar",
            "com.ximalaya.ting.android.car",
            "com.autonavi.amapauto",
            "com.inmoglass.documents",
            "cn.wps.moffice_eng",
            "com.inmoglass.launcher.NotificationCourseActivity",
            "com.inmo.settings",

//            "com.tencent.mm"
    };

    /**
     * 生产版本
     * 需要测试工具入口
     */
    public static final String[] packageNames_pro = new String[]{
            "com.inmoglass.album",
            "com.yulong.coolcamera",
            "com.inmoglass.launcher.VideoMirrorActivity",
            "com.inmoglass.launcher.PhoneMirrorActivity",
            "com.inmolens.inmomemo",
            "com.tencent.qqmusiccar",
            "com.ximalaya.ting.android.car",
            "com.autonavi.amapauto",
            "com.inmoglass.documents",
            "cn.wps.moffice_eng",
            "com.inmoglass.launcher.NotificationCourseActivity",
            "com.inmo.settings",
            "com.inmoglass.validationTools"
    };

    /**
     * 英文版本默认应用顺序
     * 相册、相机、设置
     */
    public static final String[] packageNames_EN = new String[]{
            "com.inmoglass.album",
            "com.yulong.coolcamera",
            "com.inmo.settings"
    };

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
     * 将数据源保存到db文件
     */
    public void setLauncherCardList2Db() {
        appPackagesList = new ArrayList<>();
        appPackagesList.addAll(Arrays.asList(CommonUtil.isEn() ? packageNames_EN : (CommonUtil.isProductVersion() ? packageNames_pro : packageNames)));
        LauncherCardDataDbHelper.getInstance().insertAllData(appPackagesList);
    }

    /**
     * 读取本地储存的APP卡片列表的顺序
     *
     * @return
     */
    public ArrayList<Channel> getLauncherCardList() {
        List<String> packagesList = LauncherCardDataDbHelper.getInstance().queryAllData();
        ArrayList<Channel> beanList = new ArrayList<>();
        for (String appPackage : packagesList) {
            Channel bean = null;
            if (appPackage.equals(packageNames[0])) { // 相册
                bean = new Channel(R.drawable.img_home_meitiwenjian, BaseApplication.mContext.getString(R.string.string_home_media), R.drawable.icon_home_meiti, packageNames[0], null);
            } else if (appPackage.equals(packageNames[1])) { // 相机
                bean = new Channel(R.drawable.img_home_camera, BaseApplication.mContext.getString(R.string.string_home_camera), R.drawable.icon_home_camera, packageNames[1], null);
            } else if (appPackage.equals(packageNames[2])) { // 视频
                bean = new Channel(R.drawable.img_home_video, BaseApplication.mContext.getString(R.string.string_phone_video), R.drawable.icon_home_video, packageNames[2], null);
            } else if (appPackage.equals(packageNames[3])) { // 手机镜像
                bean = new Channel(R.drawable.img_home_mirror, BaseApplication.mContext.getString(R.string.string_phone_mirror), R.drawable.icon_home_mirror, packageNames[3], null);
            } else if (appPackage.equals(packageNames[4])) { // 备忘录
                bean = new Channel(R.drawable.img_home_beiwanglu, BaseApplication.mContext.getString(R.string.string_home_beiwanglu), R.drawable.icon_home_beiwanglu, packageNames[4], null);
            } else if (appPackage.equals(packageNames[5])) { // QQ音乐
                bean = new Channel(R.drawable.img_home_qqmusic, BaseApplication.mContext.getString(R.string.string_home_qq_music), R.drawable.icon_home_qqmusic, packageNames[5], null);
            } else if (appPackage.equals(packageNames[6])) { //喜马拉雅
                bean = new Channel(R.drawable.img_home_ximalaya, BaseApplication.mContext.getString(R.string.string_home_ximalaya), R.drawable.icon_home_ximalaya, packageNames[6], null);
            } else if (appPackage.equals(packageNames[7])) { // 高德地图
                bean = new Channel(R.drawable.img_home_gaode, BaseApplication.mContext.getString(R.string.string_home_gaode), R.drawable.icon_home_gaode, packageNames[7], null);
            } else if (appPackage.equals(packageNames[8])) { // 文档
                bean = new Channel(R.drawable.img_home_wendang, BaseApplication.mContext.getString(R.string.string_home_wendang), R.drawable.icon_file_word, packageNames[8], null);
            } else if (appPackage.equals(packageNames[9])) { // WPS
                bean = new Channel(R.drawable.img_home_wps, BaseApplication.mContext.getString(R.string.string_home_wps), R.drawable.icon_home_wps, packageNames[9], null);
            } else if (appPackage.equals(packageNames[10])) { // 手机通知
                bean = new Channel(R.drawable.img_home_notice, BaseApplication.mContext.getString(R.string.string_phone_notification), R.drawable.icon_home_notice, packageNames[10], null);
            } else if (appPackage.equals(packageNames[11])) { // 设置
                bean = new Channel(R.drawable.img_home_setting, BaseApplication.mContext.getString(R.string.string_home_setting), R.drawable.icon_home_setting, packageNames[11], null);
            } else if (appPackage.equals(packageNames_pro[12])) {
                bean = new Channel(R.drawable.img_home_default, BaseApplication.mContext.getString(R.string.string_home_mmi), 0, appPackage, BaseApplication.mContext.getDrawable(R.mipmap.ic_launcher));
            } else {
                bean = new Channel(R.drawable.img_home_default, AppUtil.getInstance().getAppName(appPackage), 0, appPackage, AppUtil.getInstance().getAppIcon(appPackage));
            }
            beanList.add(bean);
        }
        return beanList;
    }

    /**
     * 读取本地储存的APP卡片列表的顺序
     *
     * @return
     */
    public ArrayList<Channel> getLauncherCardList_EN() {
        List<String> packagesList = LauncherCardDataDbHelper.getInstance().queryAllData();
        ArrayList<String> packagesEnList = filterPackagesCnList(packagesList);
        ArrayList<Channel> beanList = new ArrayList<>();
        for (String appPackage : packagesEnList) {
            Channel bean = null;
            if (appPackage.equals(packageNames_EN[0])) {
                bean = new Channel(R.drawable.img_home_meitiwenjian, BaseApplication.mContext.getString(R.string.string_home_media), R.drawable.icon_home_meiti, packageNames_EN[0], null);
            } else if (appPackage.equals(packageNames_EN[1])) {
                bean = new Channel(R.drawable.img_home_camera_en, BaseApplication.mContext.getString(R.string.string_home_camera), R.drawable.icon_home_camera, packageNames_EN[1], null);
            } else if (appPackage.equals(packageNames_EN[2])) {
                bean = new Channel(R.drawable.img_home_setting_en, BaseApplication.mContext.getString(R.string.string_home_setting), R.drawable.icon_home_setting, packageNames_EN[2], null);
            } else {
                bean = new Channel(R.drawable.img_home_default, AppUtil.getInstance().getAppName(appPackage), 0, appPackage, AppUtil.getInstance().getAppIcon(appPackage));
            }
            beanList.add(bean);
        }
        return beanList;
    }

    /**
     * 过滤中文版的一些应用
     *
     * @param packagesList
     * @return
     */
    private ArrayList<String> filterPackagesCnList(List<String> packagesList) {
        ArrayList<String> result = new ArrayList<>();
        for (int i = 0; i < packagesList.size(); i++) {
            String name = packagesList.get(i);
            if (name.equals("com.inmolens.inmomemo") || name.equals("com.tencent.qqmusiccar")
                    || name.equals("com.ximalaya.ting.android.car") || name.equals("com.autonavi.amapauto")
                    || name.equals("com.inmoglass.documents") || name.equals("cn.wps.moffice_eng") || name.equals("com.inmoglass.validationTools")) {
            } else {
                result.add(name);
            }
        }
        return result;
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
        LauncherCardDataDbHelper.getInstance().insertAllData(realList);
    }
}
