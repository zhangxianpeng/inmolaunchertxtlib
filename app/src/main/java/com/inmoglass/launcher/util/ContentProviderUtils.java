package com.inmoglass.launcher.util;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.net.Uri;

import com.blankj.utilcode.util.LogUtils;
import com.inmo.inmodata.device.DateTimeInfo;
import com.inmoglass.launcher.base.BaseApplication;
import com.inmoglass.launcher.ui.MainActivity;

public class ContentProviderUtils {
    public static void insertMemoRecords(DateTimeInfo info) {
        ContentResolver resolver = BaseApplication.mContext.getContentResolver();
        Uri uri = Uri.parse(MainActivity.MEMO_URI);
        ContentValues conValues = new ContentValues();
        conValues.put("address", "123456789");
        conValues.put("type", 1);
        conValues.put("date", System.currentTimeMillis());
        conValues.put("body", "no zuo no die why you try!");
        resolver.insert(uri, conValues);
        LogUtils.d("数据insert 成功，请查看");
    }
}
