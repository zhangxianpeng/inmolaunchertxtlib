package com.inmoglass.launcher.bean;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;

public class AppInfo {
    RectF content;
    public String appName;
    public String pkgName;
    public Drawable appIcon;
    public Intent appIntent;
    Bitmap icon;
    int x;
    int y;
    float narrowX;
    float narrowY;
    float scale;
    BitmapShader bitmapShader;

    public RectF getContent() {
        return content;
    }

    public void setContent(RectF content) {
        this.content = content;
    }

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public String getPkgName() {
        return pkgName;
    }

    public void setPkgName(String pkgName) {
        this.pkgName = pkgName;
    }

    public Drawable getAppIcon() {
        return appIcon;
    }

    public void setAppIcon(Drawable appIcon) {
        this.appIcon = appIcon;
    }

    public Intent getAppIntent() {
        return appIntent;
    }

    public void setAppIntent(Intent appIntent) {
        this.appIntent = appIntent;
    }

    public Bitmap getIcon() {
        return icon;
    }

    public void setIcon(Bitmap icon) {
        this.icon = icon;
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public float getNarrowX() {
        return narrowX;
    }

    public void setNarrowX(float narrowX) {
        this.narrowX = narrowX;
    }

    public float getNarrowY() {
        return narrowY;
    }

    public void setNarrowY(float narrowY) {
        this.narrowY = narrowY;
    }

    public float getScale() {
        return scale;
    }

    public void setScale(float scale) {
        this.scale = scale;
    }

    public BitmapShader getBitmapShader() {
        return bitmapShader;
    }

    public void setBitmapShader(BitmapShader bitmapShader) {
        this.bitmapShader = bitmapShader;
    }
}
