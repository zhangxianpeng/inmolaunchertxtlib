package com.inmoglass.launcher.data;

import android.content.Context;
import android.database.Cursor;

/**
 * @author Administrator
 * @date 2021/10/26 11:42
 */
public class LauncherDao {

    private final DbHelper mDbHelper;

    public LauncherDao(Context context) {
        mDbHelper = DbHelper.getInstance(context);
    }

    /**
     * 查询所有备忘录数据并按时间的DESC顺序输出结果
     * @return 查询到的备忘录数据Cursor
     */
    public Cursor findAllData() {
        return mDbHelper
                .getReadableDatabase()
                .rawQuery("SELECT * FROM " + LauncherDB.TABLE_NAME, null);
    }

    /**
     * 通过数据id删除对应备忘录数据
     * @param id 要删除的数据数据库id
     * @return the number of rows affected if a whereClause is passed in, 0 otherwise. To remove all rows and get a count pass "1" as the whereClause.
     */
    public int delFromId(int id) {
        String sql = LauncherDB.ID + "=" + id;
        return mDbHelper.getWritableDatabase().delete(LauncherDB.TABLE_NAME, sql, null);
    }

}
