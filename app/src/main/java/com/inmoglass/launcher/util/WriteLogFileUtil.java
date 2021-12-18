package com.inmoglass.launcher.util;

import android.os.Environment;

import com.blankj.utilcode.util.LogUtils;

import java.io.File;
import java.io.RandomAccessFile;

public class WriteLogFileUtil {
    public final static String FILE_PATH = Environment.getExternalStorageDirectory() + "/shutdownLog/";

    public static void writeFile() {
        long time = System.currentTimeMillis();
        String content = time + "============================guanji";
        String fileName = "data.txt";
        String strContent = content + "\n";
        try {
            File fileDir = new File(FILE_PATH);
            if (!fileDir.exists()) {
                fileDir.mkdirs();
                if (!fileDir.exists()) {
                    return;
                }
            }
            File file = new File(FILE_PATH, fileName);
            RandomAccessFile raf = new RandomAccessFile(file, "rw");
            raf.seek(file.length());
            raf.write(strContent.getBytes());
            raf.close();
        } catch (Exception e) {
            e.printStackTrace();
            LogUtils.i("zxp",e.getMessage());
        }
    }

    public static void delLogFile(String path) {
        File file = new File(path);
        if (file.exists()) {
            file.delete();
        }
    }
}
