package com.inmoglass.launcher.ui;

import android.os.Bundle;

import com.blankj.utilcode.util.LogUtils;
import com.inmoglass.launcher.R;
import com.inmoglass.launcher.base.BaseActivity;
import com.inmoglass.launcher.tts.TtsManager;

/**
 * @author Administrator
 * 备忘录展示弹层
 */
public class MemoShowActivity extends BaseActivity {
    private static final String TAG = MemoShowActivity.class.getSimpleName();
    private static long DOUBLE_TIME = 30000;
    String content;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_memo_show);
//        String content = getIntent().getStringExtra("content");
        String content = "asdada";
        TtsManager.getInstance().init(getApplicationContext(), new TtsManager.TtsListener() {
            @Override
            public void onInitSuccess() {
                TtsManager.getInstance().playTTS(content);
            }

            @Override
            public void onInitFail() {

            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        LogUtils.i(TAG, "onBackPressed");
    }
}