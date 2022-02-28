package com.inmoglass.launcher.service;

import android.app.IntentService;
import android.content.Intent;
import android.os.Environment;

import com.blankj.utilcode.util.LogUtils;
import com.inmoglass.launcher.R;
import com.inmoglass.launcher.base.BaseApplication;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * @author Administrator
 * 播放视频的时候起独立线程写文件
 */
public class WriteFileIntentService extends IntentService {
    private static final String TAG = WriteFileIntentService.class.getSimpleName();
    /**
     * 路径分隔符
     */
    private static final String SEPARATOR = File.separator;

    public WriteFileIntentService() {
        super("WriteFileIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        LogUtils.d(TAG, "新建线程写教程文件到相册，线程ID=" + Thread.currentThread().getId());
        saveGuideFileToSDCard();
    }

    /**
     * 把新手教程视频文件从raw目录下拷贝到SD卡
     */
    private void saveGuideFileToSDCard() {
        InputStream inputStream = BaseApplication.mContext.getResources().openRawResource(R.raw.guide);
        String picturePath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/CameraV2/";
        String fileName = "guide.mp4";
        File file = new File(picturePath);
        if (!file.exists()) {
            file.mkdirs();
        }
        String guideFilePath = picturePath + fileName;
        File guideFile = new File(guideFilePath);
        if (guideFile.exists()) {
            return;
        } else {
            readInputStream(picturePath + SEPARATOR + fileName, inputStream);
        }
    }

    public static void readInputStream(String storagePath, InputStream inputStream) {
        File file = new File(storagePath);
        try {
            if (!file.exists()) {
                // 1.建立通道对象
                FileOutputStream fos = new FileOutputStream(file);
                // 2.定义存储空间
                byte[] buffer = new byte[inputStream.available()];
                // 3.开始读文件
                int lenght = 0;
                while ((lenght = inputStream.read(buffer)) != -1) {
                    // 循环从输入流读取buffer字节
                    // 将Buffer中的数据写到outputStream对象中
                    fos.write(buffer, 0, lenght);
                }
                fos.flush();
                // 刷新缓冲区
                // 4.关闭流
                fos.close();
                inputStream.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        LogUtils.d(TAG, "onDestroy");
    }
}
