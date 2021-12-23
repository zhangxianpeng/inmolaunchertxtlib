package com.inmoglass.launcher.bean;

/**
 * @author Administrator
 */
public class Channel {
    private int appImg;
    private String appName;
    private int appIcon;
    private String packageName;

    public Channel(int appImg, String appName, int appIcon,String packageName) {
        this.appImg = appImg;
        this.appName = appName;
        this.appIcon = appIcon;
        this.packageName = packageName;
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
}
