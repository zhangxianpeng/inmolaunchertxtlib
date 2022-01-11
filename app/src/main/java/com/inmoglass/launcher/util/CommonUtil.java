package com.inmoglass.launcher.util;

import android.app.ActivityManager;
import android.content.ComponentName;

import com.blankj.utilcode.util.LogUtils;
import com.inmoglass.launcher.base.BaseApplication;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class CommonUtil {
    /**
     * 时间戳转时间字符串
     *
     * @param timeStamp
     * @return
     */
    public static String getStrTime(long timeStamp) {
        Date date = new Date(timeStamp);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return sdf.format(date);
    }

    /**
     * 获取系统语言
     *
     * @return
     */
    public static boolean isEn() {
        Locale locale = BaseApplication.mContext.getResources().getConfiguration().locale;
        String language = locale.getLanguage();
        return language.contains("en");
    }
}
