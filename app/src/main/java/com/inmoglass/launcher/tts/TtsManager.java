package com.inmoglass.launcher.tts;

import android.content.Context;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnInitListener;

import java.util.HashMap;
import java.util.Locale;

/**
 *
 */
public class TtsManager {

    private static volatile TtsManager mTtsManager;
    private TextToSpeech mSpeech;
    private Context context;
    public TtsListener mTtsListener;

    public static TtsManager getInstance() {
        if (mTtsManager == null) {
            synchronized (TtsManager.class) {
                if (mTtsManager == null) {
                    mTtsManager = new TtsManager();
                }
            }
        }
        return mTtsManager;
    }

    public void init(Context context, TtsListener mTtsListener) {
        this.mTtsListener = mTtsListener;
        mSpeech = new TextToSpeech(context, new OnInitListener() {
            @Override
            public void onInit(int i) {
                if (i == 0) {
                    if (mTtsListener != null) {
                        mTtsListener.onInitSuccess();
                    }
                }
            }
        });
    }

    public void playTTS(String content) {
        if(mSpeech!=null) {
            mSpeech.setLanguage(Locale.SIMPLIFIED_CHINESE);
            HashMap<String, String> params = new HashMap<>();
            int result = mSpeech.speak(content, TextToSpeech.QUEUE_FLUSH, params);
        }
    }

    public interface TtsListener {
        void onInitSuccess();

        void onInitFail();
    }
}
