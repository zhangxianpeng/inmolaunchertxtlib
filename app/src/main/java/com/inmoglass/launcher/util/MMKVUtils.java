package com.inmoglass.launcher.util;


import android.content.Context;

import com.google.gson.Gson;
import com.tencent.mmkv.MMKV;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Administrator
 */
public class MMKVUtils {

    public static <T> Boolean setArray(Context mContext, List<T> list, String name) {
        MMKV kv = MMKV.defaultMMKV();
        if (list == null || list.size() == 0) { // 清空
            kv.putInt(name + "size", 0);
            int size = kv.getInt(name + "size", 0);
            for (int i = 0; i < size; i++) {
                if (kv.getString(name + i, null) != null) {
                    kv.remove(name + i);
                }
            }
        } else {
            kv.putInt(name + "size", list.size());
            for (int i = 0; i < list.size(); i++) {
                kv.remove(name + i);
                kv.remove(new Gson().toJson(list.get(i)));//删除重复数据 先删后加
                kv.putString(name + i, new Gson().toJson(list.get(i)));
            }
        }
        return kv.commit();
    }

    public static <T> ArrayList<T> getArray(Context mContext, String name, T bean) {
        MMKV kv = MMKV.defaultMMKV();
        ArrayList<T> list = new ArrayList<T>();
        int size = kv.getInt(name + "size", 0);
        for (int i = 0; i < size; i++) {
            if (kv.getString(name + i, null) != null) {
                try {
                    list.add((T) new Gson().fromJson(kv.getString(name + i, null), bean.getClass()));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

        }
        return list;
    }

    public static void setBoolean(String name, boolean isPlayed) {
        MMKV kv = MMKV.defaultMMKV();
        kv.encode(name, isPlayed);
    }

    public static boolean getBoolean(String name) {
        MMKV kv = MMKV.defaultMMKV();
        return kv.decodeBool(name);
    }
}
