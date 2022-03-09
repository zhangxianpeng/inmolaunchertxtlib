package com.inmoglass.launcher.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.blankj.utilcode.util.LogUtils;

/**
 * @author Administrator
 * 用于保存桌面卡片数据，替代原有mmkv保存卡数据的方式，实现跨进程数据传输
 */
public class LauncherContentProvider extends ContentProvider {

    private final String TAG = LauncherContentProvider.class.getSimpleName();

    private DbHelper mDbHelper;
    private static final UriMatcher mUriMatcher;

    static {
        mUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        mUriMatcher.addURI(LauncherDB.AUTHORITY, LauncherDB.TABLE_NAME, LauncherDB.MEMOS);
        mUriMatcher.addURI(LauncherDB.AUTHORITY, LauncherDB.TABLE_NAME + "/#", LauncherDB.MEMOS_ID);
    }

    @Override
    public boolean onCreate() {
        mDbHelper = DbHelper.getInstance(getContext());
        // 创建数据库
        mDbHelper.getWritableDatabase();
        LogUtils.i(TAG, "Launcher内容提供者初始化成功");
        return true;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        switch (mUriMatcher.match(uri)) {
            case LauncherDB.MEMOS:
                break;
            case LauncherDB.MEMOS_ID:
                String id = uri.getPathSegments().get(1);
                LogUtils.d(TAG, "select id = " + id);
                if (selection == null) {
                    selection = LauncherDB.ID + "=" + id;
                } else {
                    selection = LauncherDB.ID + "=" + id + "and" + selection;
                }
                break;
            default:
                throw new IllegalArgumentException("Unknown URI type: " + uri);
        }

        if (sortOrder == null) {
            sortOrder = LauncherDB.ID + " ASC";
        }
        SQLiteDatabase db = mDbHelper.getReadableDatabase();
        Cursor c = db.query(LauncherDB.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);
        LogUtils.d(TAG, "cursor count = " + c.getCount());
        return c;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        switch (mUriMatcher.match(uri)) {
            case LauncherDB.MEMOS:
                return LauncherDB.CONTENT_TYPE;
            case LauncherDB.MEMOS_ID:
                return LauncherDB.CONTENT_ITEM_TYPE;
            default:
                throw new IllegalArgumentException("Unknown URI get type: " + uri);
        }
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {
        if (mUriMatcher.match(uri) != LauncherDB.MEMOS) {
            throw new IllegalArgumentException("Wrong Insert Type: " + uri);
        }
        if (values == null) {
            throw new IllegalArgumentException("Wrong Data.");
        }
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        long rowId = db.insert(LauncherDB.TABLE_NAME, null, values);
        if (rowId > 0) {
            return ContentUris.withAppendedId(LauncherDB.CONTENT_URI, rowId);
        }
        return null;
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        if (mUriMatcher.match(uri) != LauncherDB.MEMOS_ID) {
            throw new IllegalArgumentException("Wrong Insert Type: " + uri);
        }
        String id = uri.getPathSegments().get(1);
        if (selection == null) {
            selection = LauncherDB.ID + "=" + id;
        } else {
            selection = LauncherDB.ID + "=" + id + " and " + selection;
        }
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        int i = db.delete(LauncherDB.TABLE_NAME, selection, selectionArgs);
        LogUtils.i(TAG, i > 0 ? "数据更新成功" : "数据未更新");
        return i;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection, @Nullable String[] selectionArgs) {
        if (mUriMatcher.match(uri) != LauncherDB.MEMOS_ID) {
            throw new IllegalArgumentException("Wrong Insert Type: " + uri);
        }
        if (values == null) {
            throw new IllegalArgumentException("Wrong Data");
        }
        String id = uri.getPathSegments().get(1);
        if (selection == null) {
            selection = LauncherDB.ID + "=" + id;
        } else {
            selection = LauncherDB.ID + "=" + id + " and " + selection;
        }
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        int i = db.update(LauncherDB.TABLE_NAME, values, selection, selectionArgs);
        LogUtils.i(TAG, i > 0 ? "数据更新成功" : "数据未更新");
        return i;
    }
}
