package com.inmoglass.launcher.bean;

import android.graphics.drawable.Drawable;

/**
 * @author Administrator
 */
public class Channel {
    private int appImg;
    private String appName;
    private int appIcon;
    // 第三方应用图标用这个
    private Drawable appIconDrawable;
    private String packageName;

    public Channel(int appImg, String appName, int appIcon, String packageName,Drawable appIconDrawable) {
        this.appImg = appImg;
        this.appName = appName;
        this.appIcon = appIcon;
        this.packageName = packageName;
        this.appIconDrawable = appIconDrawable;
    }

    public int getAppImg() {
        return appImg;
    }

    public void setAppImg(int appImg) {
        this.appImg = appImg;
    }

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public int getAppIcon() {
        return appIcon;
    }

    public void setAppIcon(int appIcon) {
        this.appIcon = appIcon;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public Drawable getAppIconDrawable() {
        return appIconDrawable;
    }

    public void setAppIconDrawable(Drawable appIconDrawable) {
        this.appIconDrawable = appIconDrawable;
    }
}
