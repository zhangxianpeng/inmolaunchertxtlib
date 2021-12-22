package com.inmoglass.launcher.tts;

import android.content.Context;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;

import com.blankj.utilcode.util.LogUtils;

import java.util.Locale;

/**
 * @author Administrator
 */
public class TTSController {
    private static final String TAG = TTSController.class.getSimpleName();
    private static volatile TTSController mTTSController;
    private TextToSpeech mSpeech;
    private Context context;

    private TTSController() {

    }

    public static TTSController getInstance() {
        if (mTTSController == null) {
            synchronized (TTSController.class) {
                if (mTTSController == null) {
                    mTTSController = new TTSController();
                }
            }
        }
        return mTTSController;
    }

    public void init(Context context) {
        if (this.context == null) {
            this.context = context;
        }
        this.context = context;
        if (mSpeech != null) {
            mSpeech.stop();
            mSpeech.shutdown();
            mSpeech = null;
        } else {
            // 创建TTS对象
            mSpeech = new TextToSpeech(context, new TTSListener());
        }
    }

    public void setChineseLanguage() {
        if (mSpeech != null) {
            int result = mSpeech.setLanguage(Locale.CHINESE);
            //如果返回值为-2，说明不支持这种语言
            LogUtils.e(TAG, "是否支持该语言：" + (result != TextToSpeech.LANG_NOT_SUPPORTED));
        }
    }

    /**
     * 将文本用TTS播放
     *
     * @param str 播放的文本内容
     */
    public synchronized void playTTS(String str) {
        if (mSpeech == null) {
            mSpeech = new TextToSpeech(context, new TTSListener());
        }
        if (mSpeech.isSpeaking()) return;
        mSpeech.speak(str, TextToSpeech.QUEUE_FLUSH, null);
        LogUtils.i(TAG, "播放语音为：" + str);
    }

    private final class TTSListener implements TextToSpeech.OnInitListener {
        @Override
        public void onInit(int status) {
            LogUtils.e(TAG, "初始化结果：" + (status == TextToSpeech.SUCCESS));
            if (status == TextToSpeech.SUCCESS) {
                if (mSpeech != null) {
                    int result = mSpeech.setLanguage(Locale.CHINESE);
                    //如果返回值为-2，说明不支持这种语言
                    LogUtils.e(TAG, "是否支持该语言：" + (result != TextToSpeech.LANG_NOT_SUPPORTED));
                    mSpeech.setOnUtteranceProgressListener(new TTSUtteranceListener());
                }
            }

        }
    }

    private ISpeechComplete mISpeechComplete;

    public void setSpeechComplete(ISpeechComplete speechComplete) {
        mISpeechComplete = speechComplete;
    }

    public interface ISpeechComplete {
        void speechComplete();

        void speechError();
    }

    private class TTSUtteranceListener extends UtteranceProgressListener {
        @Override
        public void onStart(String utteranceId) {
        }

        @Override
        public void onDone(String utteranceId) {
            if (mISpeechComplete != null) {
                mISpeechComplete.speechComplete();
            }
        }

        @Override
        public void onError(String utteranceId) {
            if (mISpeechComplete != null) {
                mISpeechComplete.speechError();
            }
        }
    }

    public void stop() {
        if (mSpeech != null) {
            mSpeech.stop();
            mSpeech.shutdown();
            mSpeech = null;
        }
    }

}
