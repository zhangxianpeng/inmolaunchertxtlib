package com.inmoglass.launcher.bean;

/**
 * @author Administrator
 */
public class Channel {
    private int appImg;
    private String appName;
    private int appIcon;

    public Channel(int appImg, String appName, int appIcon) {
        this.appImg = appImg;
        this.appName = appName;
        this.appIcon = appIcon;
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
