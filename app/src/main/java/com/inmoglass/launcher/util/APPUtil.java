package com.inmoglass.launcher.util;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.text.TextUtils;
import android.util.Log;

import com.blankj.utilcode.util.LogUtils;
import com.inmoglass.launcher.base.BaseApplication;

import java.util.ArrayList;
import java.util.List;

/**
 * App 工具类
 * @author Administrator
 * @date 2021-12-01
 */
public class APPUtil {
    private static final String TAG = APPUtil.class.getSimpleName();
    private static APPUtil mUtil;
    private Context mContext;

    public static synchronized APPUtil getInstance() {
        if (mUtil == null) {
            mUtil = new APPUtil();
        }
        return mUtil;
    }

    public APPUtil() {
        this.mContext = BaseApplication.mContext;
    }

    /**
     * 通过包名检测APP是否安装
     *
     * @param packageName 包名
     * @return true or false
     */
    public static boolean isInstalled(Context context, String packageName) {
        boolean isInstalled = false;

        if (!TextUtils.isEmpty(packageName)) {
            PackageInfo packageInfo;
            try {
                packageInfo = context.getPackageManager().getPackageInfo(packageName, 0);
            } catch (PackageManager.NameNotFoundException e) {
                packageInfo = null;
                e.printStackTrace();
            }

            if (packageInfo == null) {
                isInstalled = false;
            } else {
                isInstalled = true;
            }
        }
        Log.i(TAG, packageName + "is installed ? " + isInstalled);
        return isInstalled;
    }

    /**
     * 通过包名打开应用 返回true:成功 返回false:失败
     */
    public boolean openApplication(String pkgName) {
        if (TextUtils.isEmpty(pkgName)) {
            return false;
        }

        if (!isInstalled(mContext, pkgName)) {
            LogUtils.i(TAG, pkgName + " has not install");
            return false;
        }

        Log.i(TAG, "The package will be open : " + pkgName);
        Intent intent = mContext.getPackageManager().getLaunchIntentForPackage(pkgName);
        if (intent == null) {
            return false;
        }
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        mContext.startActivity(intent);
        return true;
    }

    /**
     * 获取手机里安装的应用信息
     *
     * @param context
     * @param type
     * @return
     */
    public static List<PackageInfo> getPackageInfos(Context context, int type) {
        // 0 表示不接受任何参数。其他参数都带有限制
        // 版本号、APP权限只能通过PackageInfo获取，所以这里不使用getInstalledApplications()方法
        List<PackageInfo> packageInfos = context.getPackageManager().getInstalledPackages(0);
        if (packageInfos == null) {
            return null;
        }
        List<PackageInfo> result = new ArrayList<>();
        switch (type) {
            case 0: // 所有应用程序
                result = packageInfos;
                break;
            case 1: // 系统自带APP
                for (PackageInfo info : packageInfos) {
                    if ((info.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) == 1) {
                        result.add(info);
                    }
                }
                break;
            case 2: // 第三方APP
                for (PackageInfo info : packageInfos) {
                    if ((info.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) == 0) {
                        result.add(info);
                    } else if ((info.applicationInfo.flags & ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) == 1) {
                        result.add(info);
                    }
                }
                break;
            case 3: // 安装在SDCard的应用程序
                for (PackageInfo info : packageInfos) {
                    if ((info.applicationInfo.flags & ApplicationInfo.FLAG_EXTERNAL_STORAGE) == 1) {
                        result.add(info);
                    }
                }
                break;
            default:
                break;
        }
        return result;
    }

    private static List<String> selfStudyApps;
    /**
     * 过滤自研应用
     *
     * @param context
     * @return
     */
    public static List<PackageInfo> filterSelfStudyApp(Context context) {
        List<PackageInfo> result = new ArrayList<>();
        selfStudyApps = new ArrayList<>();
        selfStudyApps.add("com.inmoglass.launcher");
        selfStudyApps.add("com.yulong.coolgallery");
        selfStudyApps.add("com.inmo.settings");
        selfStudyApps.add("com.yulong.coolcamera");
        selfStudyApps.add("com.inmoglass.music");
        selfStudyApps.add("com.koushikdutta.vysor");
        selfStudyApps.add("com.inmo.translation");
        selfStudyApps.add("com.inmo.settings");
        selfStudyApps.add("com.inmolens.inmomemo");
        selfStudyApps.add("com.inmolens.voiceidentify");
        selfStudyApps.add("com.inmoglass.sensorcontrol");
        List<PackageInfo> packageInfos = getPackageInfos(context, 2);
        PackageManager pm = context.getPackageManager();
        for (PackageInfo packageInfo : packageInfos) {
            if ((packageInfo.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) == 0 && !selfStudyApps.contains(packageInfo.packageName)) {
                result.add(packageInfo);
            }
        }
        return result;
    }

}
