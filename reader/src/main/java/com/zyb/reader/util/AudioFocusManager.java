package com.zyb.reader.util;

import android.content.Context;
import android.media.AudioFocusRequest;
import android.media.AudioManager;
import android.os.Build;

import com.zyb.base.utils.LogUtil;


/**
 * 音频管理类
 * 主要用来管理音频焦点
 */

public class AudioFocusManager {

    private AudioManager mAudioManager;
    AudioManager.OnAudioFocusChangeListener audioFocusChangeListener;


    public AudioFocusManager(Context mContext, AudioManager.OnAudioFocusChangeListener audioFocusChangeListener) {
        initAudioManager(mContext);
        this.audioFocusChangeListener =audioFocusChangeListener;
    }

    /**
     * 初始化AudioManager&Receiver
     */
    private void initAudioManager(Context mContext) {
        mAudioManager = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
    }

    /**
     * 请求音频焦点
     */
    public void requestAudioFocus() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            AudioFocusRequest mAudioFocusRequest = new AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
                    .setOnAudioFocusChangeListener(audioFocusChangeListener)
                    .build();
            int res = mAudioManager.requestAudioFocus(mAudioFocusRequest);
            if (res == 1) {
                LogUtil.e("requestAudioFocus=" + true);
            }
        } else {
            if (audioFocusChangeListener != null) {
                boolean result = AudioManager.AUDIOFOCUS_REQUEST_GRANTED ==
                        mAudioManager.requestAudioFocus(audioFocusChangeListener,
                                AudioManager.STREAM_MUSIC,
                                AudioManager.AUDIOFOCUS_GAIN);
                LogUtil.e("requestAudioFocus=" + result);
            }
        }
    }

    /**
     * 关闭音频焦点
     */
    public void abandonAudioFocus() {
        if (audioFocusChangeListener != null) {
            boolean result = AudioManager.AUDIOFOCUS_REQUEST_GRANTED ==
                    mAudioManager.abandonAudioFocus(audioFocusChangeListener);
            LogUtil.e("requestAudioFocus=" + result);
        }
    }

}
