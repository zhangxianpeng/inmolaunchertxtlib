package com.inmoglass.launcher.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

import com.blankj.utilcode.util.LogUtils;

/**
 * @author Jack
 * @date 2021/10/25 15:01
 */
public class DbHelper extends SQLiteOpenHelper {
    private static final String TAG = DbHelper.class.getSimpleName();
    private static DbHelper sDbHelper;

    public static DbHelper getInstance(Context context) {
        if (sDbHelper == null) {
            sDbHelper = new DbHelper(context.getApplicationContext());
        }
        return sDbHelper;
    }

    public DbHelper(@Nullable Context context) {
        super(context, LauncherDB.DATABASE_NAME, null, LauncherDB.DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS "
                + LauncherDB.TABLE_NAME + " ( " + LauncherDB.ID + " INTEGER primary key autoincrement, "
                + LauncherDB.INFO_APP_NAME + " TEXT)");
        LogUtils.d(TAG, "创建数据库");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        LogUtils.i(TAG,"更新数据库");
    }

}
