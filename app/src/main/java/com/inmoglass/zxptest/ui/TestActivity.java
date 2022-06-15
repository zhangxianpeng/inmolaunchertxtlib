package com.inmoglass.zxptest.ui;

import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.blankj.utilcode.util.LogUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.inmoglass.txtreaderlib.bean.TxtMsg;
import com.inmoglass.txtreaderlib.interfaces.ICenterAreaClickListener;
import com.inmoglass.txtreaderlib.interfaces.ILoadListener;
import com.inmoglass.txtreaderlib.main.TxtConfig;
import com.inmoglass.txtreaderlib.main.TxtReaderView;
import com.inmoglass.txtreaderlib.ui.ChapterList;
import com.inmoglass.zxptest.R;

import java.io.File;
import java.util.Locale;

import static android.view.KeyEvent.KEYCODE_HOME;

/**
 * @author Administrator
 * 封装之后的调用
 */
public class TestActivity extends AppCompatActivity {
    protected Handler mHandler;
    protected boolean FileExist = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app_manager);

        String filePath = "/storage/emulated/0/test.txt";
        FileExist = isFileExit(filePath);
        if (FileExist) {
            init();
            loadFile();
            registerListener();
        }
    }

    private boolean isFileExit(String path) {
        if (path == null) {
            ToastUtils.showShort("文件路径为空！");
            finish();
            return false;
        }
        File file = new File(path);
        if (file.exists()) {
            FilePath = path;
            FileName = file.getName();
            return true;
        } else {
            ToastUtils.showShort("文件不存在");
            finish();
            return false;
        }
    }

    protected TxtReaderView mTxtReaderView;

    protected GestureDetector mGestureDetector;//手势检测器
    protected GestureListener gestureListener = new GestureListener();

    protected void init() {
        mHandler = new Handler();
//        mChapterMsgView = findViewById(R.id.activity_hwTxtPlay_chapter_msg);
//        mChapterMsgName = findViewById(R.id.chapter_name);
//        mChapterMsgProgress = findViewById(R.id.chapter_progress);
//        mTopDecoration = findViewById(R.id.activity_hwTxtPlay_top);
//        mBottomDecoration = findViewById(R.id.activity_hwTxtPlay_bottom);
        mTxtReaderView = findViewById(R.id.activity_hwTxtPlay_readerView);
//        mChapterNameText = findViewById(R.id.activity_hwTxtPlay_chapterName);
//        mChapterMenuText = findViewById(R.id.activity_hwTxtPlay_chapter_menuText);
//        mProgressText = findViewById(R.id.activity_hwTxtPlay_progress_text);
//        mSettingText = findViewById(R.id.activity_hwTxtPlay_setting_text);
//        mTopMenu = findViewById(R.id.activity_hwTxtPlay_menu_top);
//        bottomView = findViewById(R.id.activity_bottom);
//        mCoverView = findViewById(R.id.activity_hwTxtPlay_cover);
//        ClipboardView = findViewById(R.id.activity_hwTxtPlay_ClipBoar);
//        mSelectedText = findViewById(R.id.activity_hwTxtPlay_selected_text);
//        bottomView = findViewById(R.id.activity_bottom);
//        tips = findViewById(R.id.tv_privacy_tips);
//        setTips();

//        mMenuHolder.mTitle = findViewById(R.id.txtReader_menu_title);
//        mMenuHolder.mPreChapter = findViewById(R.id.txtReadr_menu_chapter_pre);
//        mMenuHolder.mNextChapter = findViewById(R.id.txtReadr_menu_chapter_next);
////        mMenuHolder.mSeekBar = findViewById(R.id.txtReadr_menu_seekbar);
//        mMenuHolder.mSeekBar = findViewById(R.id.txtReadr_fontsize_seekbar);
////        mMenuHolder.mSeekBar.getViewTreeObserver().addOnDrawListener();
//        mMenuHolder.mTextSize = findViewById(R.id.txtRead_menu_textSize);
//        mMenuHolder.mProgress = findViewById(R.id.txtRead_menu_progress);
////        mMenuHolder.batteryView = findViewById(R.id.sbb_battery);
//        mMenuHolder.batteryLevelTextView = findViewById(R.id.tv_battery_level);
//        mMenuHolder.isChargingImageView = findViewById(R.id.iv_isCharging);

        mGestureDetector = new GestureDetector(this, gestureListener);
    }

    private static final int INT_START_FROM_EN = 71;
    private static final int INT_START_TO_EN = 76;
    private static final int INT_END_FROM_EN = 109;
    private static final int INT_END_TO_EN = 117;

    private static final int INT_START_FROM_CN = 19;
    private static final int INT_START_TO_CN = 22;
    private static final int INT_END_FROM_CN = 29;
    private static final int INT_END_TO_CN = 32;

    private void setTips() {
        String content = getString(R.string.privacy_tips);
        ForegroundColorSpan blackSpan = new ForegroundColorSpan(ContextCompat.getColor(this, R.color.color_battery_green));
        ForegroundColorSpan blackSpan1 = new ForegroundColorSpan(ContextCompat.getColor(this, R.color.color_battery_green));
        SpannableStringBuilder builder = new SpannableStringBuilder(content);
        if (isEn()) {
            // 英文
            LogUtils.d("english length=" + builder.length());
            builder.setSpan(blackSpan, INT_START_FROM_EN, INT_START_TO_EN, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            builder.setSpan(blackSpan1, INT_END_FROM_EN, INT_END_TO_EN, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        } else {
            // 中文
            LogUtils.d("chinese length=" + builder.length());
            builder.setSpan(blackSpan, INT_START_FROM_CN, INT_START_TO_CN, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            builder.setSpan(blackSpan1, INT_END_FROM_CN, INT_END_TO_CN, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
//        tips.setText(builder);
    }

    private boolean isEn() {
        Locale locale = getResources().getConfiguration().locale;
        String language = locale.getLanguage();
        return language.contains("en");
    }

    protected String ContentStr = null;
    protected String FilePath = null;
    protected String FileName = null;

    protected void loadFile() {
        TxtConfig.savePageSwitchDuration(this, 400);
        if (ContentStr == null) {
            if (TextUtils.isEmpty(FilePath) || !(new File(FilePath).exists())) {
                ToastUtils.showShort("文件不存在");
                finish();
                return;
            }

        }
        mHandler.postDelayed(() -> {
            //延迟加载避免闪一下的情况出现
            if (ContentStr == null) {
                loadOurFile();
            } else {
                loadStr();
            }
        }, 300);
    }

    protected void loadOurFile() {
        mTxtReaderView.loadTxtFile(FilePath, new ILoadListener() {
            @Override
            public void onSuccess() {
                if (!hasExisted) {
                    onLoadDataSuccess();
                }
            }

            @Override
            public void onFail(final TxtMsg txtMsg) {
                if (!hasExisted) {
                    runOnUiThread(() -> onLoadDataFail(txtMsg));
                }
            }

            @Override
            public void onMessage(String message) {
            }
        });
    }

    /**
     * @param txtMsg txtMsg
     */
    protected void onLoadDataFail(TxtMsg txtMsg) {
        // 加载失败信息
        ToastUtils.showShort(String.valueOf(txtMsg));
    }

    protected void onLoadDataSuccess() {
        initWhenLoadDone();
    }

    private void loadStr() {
        String testText = ContentStr;
        mTxtReaderView.loadText(testText, new ILoadListener() {
            @Override
            public void onSuccess() {
                initWhenLoadDone();
            }

            @Override
            public void onFail(TxtMsg txtMsg) {
                //加载失败信息
                ToastUtils.showShort(txtMsg + "");
            }

            @Override
            public void onMessage(String message) {
                //加载过程信息
            }
        });
    }

    protected void initWhenLoadDone() {
        mTxtReaderView.setTextSize(TxtConfig.getTextSize(this));
        mTxtReaderView.setPageSwitchByTranslate();
    }

    protected void registerListener() {
        setCenterClickListener();
    }

    protected void setCenterClickListener() {
        mTxtReaderView.setOnCenterAreaClickListener(new ICenterAreaClickListener() {
            @Override
            public boolean onCenterClick(float widthPercentInView) {
                gotoLauncherActivity();
                return true;
            }

            @Override
            public boolean onOutSideCenterClick(float widthPercentInView) {
                gotoLauncherActivity();
                return true;
            }
        });
    }

    public class GestureListener implements GestureDetector.OnGestureListener {

        @Override
        public boolean onDown(MotionEvent e) {
            return false;
        }

        @Override
        public void onShowPress(MotionEvent e) {

        }

        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            LogUtils.d("进入主页面");
            gotoLauncherActivity();
            return false;
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            return false;
        }

        @Override
        public void onLongPress(MotionEvent e) {

        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            return false;
        }
    }

    private class ChapterChangeClickListener implements View.OnClickListener {
        private final Boolean Pre;

        public ChapterChangeClickListener(Boolean pre) {
            Pre = pre;
        }

        @Override
        public void onClick(View view) {
            if (Pre) {
                mTxtReaderView.jumpToPreChapter();
            } else {
                mTxtReaderView.jumpToNextChapter();
            }
        }
    }


    protected static class MenuHolder {
        public TextView mTitle;
        public TextView mPreChapter;
        public TextView mNextChapter;
        public SeekBar mSeekBar;//字体大小滑动条
        public TextView mTextSize;
        public TextView mProgress;
        public TextView batteryLevelTextView;
    }

    @Override
    protected void onResume() {
        super.onResume();
        hideNavigationBarStatusBar();
    }

    private void hideNavigationBarStatusBar() {
        if (Build.VERSION.SDK_INT >= 19) {
            View decorView = getWindow().getDecorView();
            decorView.setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        exist();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    @Override
    public void finish() {
        super.finish();
        exist();
    }

    protected boolean hasExisted = false;

    protected void exist() {
        if (!hasExisted) {
            ContentStr = null;
            hasExisted = true;
            if (mHandler != null) {
                mHandler.removeCallbacksAndMessages(null);
                mHandler = null;
            }
            if (mTxtReaderView != null) {
                mTxtReaderView.saveCurrentProgress();
                mTxtReaderView.onDestroy();
            }
            if (mTxtReaderView != null) {
                mTxtReaderView.getTxtReaderContext().Clear();
                mTxtReaderView = null;
            }
        }
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (event.getKeyCode() == KeyEvent.KEYCODE_BACK || event.getKeyCode() == KEYCODE_HOME || event.getKeyCode() == 299) {
            return true;
        } else {
            return super.dispatchKeyEvent(event);
        }
    }

    private void gotoLauncherActivity() {
        finish();
    }
}