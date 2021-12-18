package com.inmoglass.launcher.util;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.blankj.utilcode.util.LogUtils;
import com.inmoglass.launcher.R;
import com.inmoglass.launcher.base.BaseApplication;

import java.util.ArrayList;
import java.util.List;

/**
 * App 工具类
 *
 * @author Administrator
 * @date 2021-12-01
 */
public class AppUtil {
    private static final String TAG = AppUtil.class.getSimpleName();
    private static AppUtil mUtil;
    private Context mContext;

    public static synchronized AppUtil getInstance() {
        if (mUtil == null) {
            mUtil = new AppUtil();
        }
        return mUtil;
    }

    public AppUtil() {
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
     * 用包名和类名启动
     *
     * @param pkgName
     * @param activityName
     */
    public void openApplication(String pkgName, String activityName) {
        if (TextUtils.isEmpty(pkgName)) {
            return;
        }
        if (!isInstalled(mContext, pkgName)) {
            Toast.makeText(mContext, R.string.string_app_not_installed, Toast.LENGTH_SHORT).show();
            return;
        }
        Intent intent1 = mContext.getPackageManager().getLaunchIntentForPackage(pkgName);
        if (intent1 == null) {
            return;
        }
        Intent intent = new Intent();
        ComponentName componentName = new ComponentName(pkgName, activityName);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setComponent(componentName);
        LogUtils.d("packageName = " + pkgName + " activityName = " + activityName);
        mContext.startActivity(intent);
    }

    /**
     * @param pkgName
     */
    public void openApplicationByPkgName(String pkgName) {
        if (TextUtils.isEmpty(pkgName)) {
            return;
        }

        if (!isInstalled(mContext, pkgName)) {
            Toast.makeText(mContext, R.string.string_app_not_installed, Toast.LENGTH_SHORT).show();
            return;
        }

        Log.i(TAG, "The package will be open : " + pkgName);
        Intent intent = mContext.getPackageManager().getLaunchIntentForPackage(pkgName);
        if (intent == null) {
            return;
        }
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        mContext.startActivity(intent);
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
