package com.inmoglass.launcher.data;

import android.net.Uri;

/**
 * @author Administrator
 * launche卡片排序
 */
public class LauncherDB {

    public static final String AUTHORITY = "com.inmoglass.launcher.data.launcherdb";
    public static final String DATABASE_NAME = "inmo_launcher_db";
    public static final int DATABASE_VERSION = 1;
    public  static final String TABLE_NAME = "launcher_info";

    public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/" + TABLE_NAME);
    public static final int MEMOS = 1;
    public static final int MEMOS_ID = 2;

    public static final String CONTENT_TYPE = "vnd.android.cursor.dir/launcherdb.all";
    public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.dir/launcherdb.item";

    public static final String ID = "_id";
    public static final String INFO_APP_NAME = "app_name";
}
