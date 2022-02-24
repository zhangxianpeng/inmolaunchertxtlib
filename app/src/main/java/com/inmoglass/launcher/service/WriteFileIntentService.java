package com.inmoglass.launcher.service;

import android.app.IntentService;
import android.content.Intent;
import android.os.Environment;

import com.blankj.utilcode.util.LogUtils;
import com.inmoglass.launcher.R;
import com.inmoglass.launcher.base.BaseApplication;

import java.io.ByteArrayOutputStream;
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

    public WriteFileIntentService() {
        super("WriteFileIntentService"); // 调用父类的有参构造函数
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        // 打印当前线程的id
        LogUtils.d(TAG, "Thread id is " + Thread.currentThread().getId());
        saveGuideFileToSDCard();
    }

    /**
     * 把新手教程视频文件写到SD卡
     */
    private void saveGuideFileToSDCard() {
        String picturePath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/CameraV2/";
        File file = new File(picturePath);
        if (!file.exists()) {
            file.mkdirs();
        }
        String filePath = picturePath + "guide.mp4";
        File guideFile = new File(filePath);
        if (guideFile.exists()) {
            return;
        } else {
            InputStream inStream = BaseApplication.mContext.getResources().openRawResource(R.raw.guide);
            File realFile = new File(picturePath, "guide.mp4");
            try {
                FileOutputStream fileOutputStream = new FileOutputStream(realFile);
                byte[] buffer = new byte[10];
                ByteArrayOutputStream outStream = new ByteArrayOutputStream();
                int len = 0;
                while ((len = inStream.read(buffer)) != -1) {
                    outStream.write(buffer, 0, len);
                }
                byte[] bs = outStream.toByteArray();
                fileOutputStream.write(bs);
                outStream.close();
                inStream.close();
                fileOutputStream.flush();
                fileOutputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        LogUtils.d(TAG, "onDestroy");
    }
}
