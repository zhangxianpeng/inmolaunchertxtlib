package com.inmoglass.launcher.bean;

/**
 * 卡片对象
 *
 * @author Administrator
 */
public class Channel2 {
    private int appImg;
    private int appIcon;
    private String appImgString;

    public String getAppImgString() {
        return appImgString;
    }

    public void setAppImgString(String appImgString) {
        this.appImgString = appImgString;
    }

    public String getAppIconString() {
        return appIconString;
    }

    public void setAppIconString(String appIconString) {
        this.appIconString = appIconString;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public String getLaunchActivityName() {
        return launchActivityName;
    }

    public void setLaunchActivityName(String launchActivityName) {
        this.launchActivityName = launchActivityName;
    }

    private String appIconString;
    private String appName;
    private String packageName;
    private String launchActivityName;

    public Channel2() {

    }

    public Channel2(int appImg, int appIcon, String appName, String packageName, String launchActivityName, String appImgString, String appIconString) {
        this.appImg = appImg;
        this.appName = appName;
        this.appIcon = appIcon;
        this.packageName = packageName;
        this.launchActivityName = launchActivityName;
        this.appImgString = appImgString;
        this.appIconString = appIconString;
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
}
