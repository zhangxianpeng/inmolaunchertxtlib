package com.inmoglass.launcher.util;

import static com.inmoglass.launcher.ui.MainActivity.MEMO_URI;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;

import com.blankj.utilcode.util.LogUtils;
import com.inmo.inmodata.device.CalenderEvents;
import com.inmoglass.launcher.base.BaseApplication;
import com.inmoglass.launcher.bean.InmoMemoData;

/**
 * @author Administrator
 */
public class ContentProviderUtils {
    private static final String TAG = ContentProviderUtils.class.getSimpleName();
    public static final String INFO_CONTENT = "info_content";
    public static final String INFO_CREATE_TIME = "info_create_time";
    public static final String INFO_TIME = "info_time";
    public static final String INFO_COLOR = "info_color";

    public static void insertMemoRecords(CalenderEvents calenderEvent) {
        ContentResolver resolver = BaseApplication.mContext.getContentResolver();
        Uri uri = Uri.parse(MEMO_URI);
        ContentValues conValues = new ContentValues();
        conValues.put(INFO_TIME, calenderEvent.getStartTime());
        conValues.put(INFO_CONTENT, calenderEvent.getEventTitle());
        conValues.put(INFO_CREATE_TIME, calenderEvent.getCreateTime());
        int color = calenderEvent.getColor();
        conValues.put(INFO_COLOR, color);
        resolver.insert(uri, conValues);
        LogUtils.d("数据insert 成功，请查看");
    }

    /**
     * 查询是否有重复数据
     *
     * @param needInsertData
     * @return
     */
    private static boolean checkIsRepeatData(InmoMemoData needInsertData) {
        boolean isRepeatData = false;
        Cursor cursor = BaseApplication.mContext.getContentResolver().query(Uri.parse(MEMO_URI), null, null, null, null);
        while (cursor.moveToNext()) {
            String content = cursor.getString(cursor.getColumnIndex("info_content"));
            long time = cursor.getLong(cursor.getColumnIndex("info_time"));
            long createTime = cursor.getLong(cursor.getColumnIndex("info_create_time"));
            if (needInsertData.getContent().equals(content) &&
                    needInsertData.getTimestamp() == time &&
                    needInsertData.getCreateTime() == createTime) {
                isRepeatData = true;
                LogUtils.d(TAG, "在数据库中查到重复数据，中断游标");
                break;
            }
        }
        cursor.close();
        return isRepeatData;
    }

    /**
     * 没有重复数据的时候再插入表中
     *
     * @param needInsertData
     */
    private static void insertToMemoContentProvider(InmoMemoData needInsertData) {
        ContentResolver resolver = BaseApplication.mContext.getContentResolver();
        Uri uri = Uri.parse(MEMO_URI);
        ContentValues conValues = new ContentValues();
        conValues.put(INFO_TIME, needInsertData.getTimestamp());
        conValues.put(INFO_CONTENT, needInsertData.getContent());
        conValues.put(INFO_CREATE_TIME, needInsertData.getCreateTime());
        int color = 1;
        conValues.put(INFO_COLOR, color);
        resolver.insert(uri, conValues);
        LogUtils.d("数据insert 成功，请查看");
    }

}
