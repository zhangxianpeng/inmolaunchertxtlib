package com.inmoglass.launcher.data;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

import androidx.annotation.Nullable;

import com.blankj.utilcode.util.LogUtils;
import com.inmoglass.launcher.base.BaseApplication;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Administrator
 */
public class LauncherCardDataDbHelper {
    private static final String TAG = LauncherCardDataDbHelper.class.getSimpleName();
    private static LauncherCardDataDbHelper mLauncherCardDataDbHelper;

    private LauncherDao mLauncherDao;
    private ContentResolver mContentResolver;

    public static LauncherCardDataDbHelper getInstance() {
        if (mLauncherCardDataDbHelper == null) {
            mLauncherCardDataDbHelper = new LauncherCardDataDbHelper(BaseApplication.mContext);
        }
        return mLauncherCardDataDbHelper;
    }

    public LauncherCardDataDbHelper(@Nullable Context context) {
        mLauncherDao = new LauncherDao(context);
        mContentResolver = context.getContentResolver();
    }

    /**
     * 查询全部数据
     */
    public List<String> queryAllData() {
        List<String> result = new ArrayList<>();
        Cursor cursor = mLauncherDao.findAllData();
        while (cursor.moveToNext()) {
            String content = cursor.getString(1);
            result.add(content);
        }
        LogUtils.d(TAG, "queryAllData success,data = " + result.toString());
        return result;
    }

    /**
     * 保存全部数据
     *
     * @param sourceData
     */
    public void insertAllData(List<String> sourceData) {
        clearLauncherCardData();
        if (sourceData != null && sourceData.size() > 0) {
            for (int i = 0; i < sourceData.size(); i++) {
                String appPackage = sourceData.get(i);
                insert(i, appPackage);
            }
            LogUtils.d(TAG, "insertAllData success,全部数据插入成功");
        }

    }

    /**
     * 清空表数据
     * 然后重新插入一批新的数据，保证数据的时效性
     */
    public void clearLauncherCardData() {
        Cursor cursor = mLauncherDao.findAllData();
        while (cursor.moveToNext()) {
            String id = cursor.getString(cursor.getColumnIndex("_id"));
            mLauncherDao.delFromId(Integer.parseInt(id));
        }
        LogUtils.d(TAG, "clearLauncherCardData 清空数据表");
    }

    /**
     * 插入数据表
     *
     * @param appPackage
     */
    public void insert(int i, String appPackage) {
        ContentValues values = new ContentValues();
        values.put(LauncherDB.ID, String.valueOf(i));
        values.put(LauncherDB.INFO_APP_NAME, appPackage);
        Uri uri = mContentResolver.insert(LauncherDB.CONTENT_URI, values);
        LogUtils.d(uri == null ? "数据插入失败" : "数据插入成功 uri=" + uri.getPath());
    }

}
