package com.zyb.reader;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;

import com.baidu.tts.auth.AuthInfo;
import com.baidu.tts.chainofresponsibility.logger.LoggerProxy;
import com.baidu.tts.client.SpeechError;
import com.baidu.tts.client.SpeechSynthesizer;
import com.baidu.tts.client.SpeechSynthesizerListener;
import com.baidu.tts.client.TtsMode;
import com.hjq.toast.ToastUtils;
import com.zyb.base.event.BaseEvent;
import com.zyb.base.event.EventConstants;
import com.zyb.base.utils.EventBusUtil;
import com.zyb.base.utils.LogUtil;
import com.zyb.base.utils.constant.ApiConstants;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * 朗读服务
 */
public class SpeechService extends Service {

    private final IBinder mBinder = new SpeechBinder();
    private static final int DEFAULT_SPEED = 5;

    // ================== 初始化参数设置开始 ==========================
    // 离线发音选择，VOICE_FEMALE即为离线女声发音。
    // assets目录下bd_etts_common_speech_m15_mand_eng_high_am-mix_v3.0.0_20170505.dat为离线男声模型；
    // assets目录下bd_etts_common_speech_f7_mand_eng_high_am-mix_v3.0.0_20170512.dat为离线女声模型
    protected String offlineVoice = OfflineResource.VOICE_FEMALE;
    // TODO: 2019/7/25
    // 切换离线资源
    //map.put("离线女声", OfflineResource.VOICE_FEMALE);
    //map.put("离线男声", OfflineResource.VOICE_MALE);
    //map.put("离线度逍遥", OfflineResource.VOICE_DUXY);
    //map.put("离线度丫丫", OfflineResource.VOICE_DUYY);
    protected SpeechSynthesizer mSpeechSynthesizer;
    private SpeechSynthesizerListener speechSynthesizerListener = new SpeechSynthesizerListener() {
        @Override
        public void onSynthesizeStart(String s) {
        }

        /**
         * 合成数据和进度的回调接口，分多次回调
         *
         * @param bytes     合成的音频数据。该音频数据是采样率为16K，2字节精度，单声道的pcm数据。
         * @param i 文本按字符划分的进度，比如:你好啊 进度是0-3
         */
        @Override
        public void onSynthesizeDataArrived(String s, byte[] bytes, int i) {
        }

        /**
         * 合成正常结束，每句合成正常结束都会回调，如果过程中出错，则回调onError，不再回调此接口
         */
        @Override
        public void onSynthesizeFinish(String s) {
        }

        /**
         * 播放开始，每句播放开始都会回调
         */
        @Override
        public void onSpeechStart(String s) {
        }

        /**
         * 播放进度回调接口，分多次回调
         *
         * @param i 文本按字符划分的进度，比如:你好啊 进度是0-3
         */
        @Override
        public void onSpeechProgressChanged(String s, int i) {
        }

        /**
         * 播放正常结束，每句播放正常结束都会回调，如果过程中出错，则回调onError,不再回调此接口
         */
        @Override
        public void onSpeechFinish(String s) {
            EventBusUtil.sendEvent(new BaseEvent(EventConstants.EVENT_SPEECH_FINISH_PAGE));
        }

        /**
         * 当合成或者播放过程中出错时回调此接口
         *
         * @param speechError 包含错误码和错误信息
         */
        @Override
        public void onError(String s, SpeechError speechError) {
            stop();
            ToastUtils.show("语音朗读出现错误");
            LogUtil.e("speech onError", speechError.description);
        }
    };

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        initialTts(); // 初始化TTS引擎
        EventBusUtil.register(this);
    }

    private String currentString = "";

    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true)
    public void onEventReceived(BaseEvent<Object> event) {
        if (event == null) return;
        switch (event.getCode()) {
            case EventConstants.EVENT_SPEECH_STRING_DATA:
                currentString = (String) event.getData();
                mSpeechSynthesizer.speak(currentString);
                break;
        }
    }

    /**
     * 初始化引擎，需要的参数均在InitConfig类里
     * <p>
     * DEMO中提供了3个SpeechSynthesizerListener的实现
     * MessageListener 仅仅用log.i记录日志，在logcat中可以看见
     * UiMessageListener 在MessageListener的基础上，对handler发送消息，实现UI的文字更新
     * FileSaveListener 在UiMessageListener的基础上，使用 onSynthesizeDataArrived回调，获取音频流
     */
    protected void initialTts() {
        LoggerProxy.printable(true); // 日志打印在logcat中
        // 设置初始化参数

        mSpeechSynthesizer = SpeechSynthesizer.getInstance();
        mSpeechSynthesizer.setContext(this);
        mSpeechSynthesizer.setSpeechSynthesizerListener(speechSynthesizerListener);


        // 请替换为语音开发者平台上注册应用得到的App ID ,AppKey ，Secret Key ，填写在SynthActivity的开始位置
        mSpeechSynthesizer.setAppId(ApiConstants.BAIDU_TTS_APP_ID);
        mSpeechSynthesizer.setApiKey(ApiConstants.BAIDU_TTS_APP_KEY, ApiConstants.BAIDU_TTS_SECRET_KEY);

        if (BuildConfig.DEBUG) {
            // 授权检测接口(只是通过AuthInfo进行检验授权是否成功。选择纯在线可以不必调用auth方法。
            AuthInfo authInfo = mSpeechSynthesizer.auth(TtsMode.MIX);
            if (!authInfo.isSuccess()) {
                // 离线授权需要网站上的应用填写包名。本demo的包名是com.baidu.tts.sample，定义在build.gradle中
                String errorMsg = authInfo.getTtsError().getDetailMessage();
                LogUtil.e("鉴权失败 =" + errorMsg);
            } else {
                LogUtil.e("验证通过，离线正式授权文件存在。");
            }
        }
        setParams();
        //
        // TtsMode.MIX; 离在线融合，在线优先； TtsMode.ONLINE 纯在线； 没有纯离线
        int result = mSpeechSynthesizer.initTts(TtsMode.MIX);
        if (result != 0) {
            ToastUtils.show("语音引擎初始化失败" + result);
            stopSelf();
        }
    }

    /**
     * 合成的参数，可以初始化时填写，也可以在合成前设置。
     *
     * @return
     */
    protected void setParams() {
        Map<String, String> params = new HashMap<String, String>();
        // 以下参数均为选填
        // 设置在线发声音人： 0 普通女声（默认） 1 普通男声 2 特别男声 3 情感男声<度逍遥> 4 情感儿童声<度丫丫>
        params.put(SpeechSynthesizer.PARAM_SPEAKER, "0");
        // 设置合成的音量，0-9 ，默认 5
        params.put(SpeechSynthesizer.PARAM_VOLUME, "9");
        // 设置合成的语速，0-9 ，默认 5
        params.put(SpeechSynthesizer.PARAM_SPEED, "5");
        // 设置合成的语调，0-9 ，默认 5
        params.put(SpeechSynthesizer.PARAM_PITCH, "5");

        params.put(SpeechSynthesizer.PARAM_MIX_MODE, SpeechSynthesizer.MIX_MODE_HIGH_SPEED_SYNTHESIZE_WIFI);
        // 该参数设置为TtsMode.MIX生效。即纯在线模式不生效。
        // MIX_MODE_DEFAULT 默认 ，wifi状态下使用在线，非wifi离线。在线状态下，请求超时6s自动转离线
        // MIX_MODE_HIGH_SPEED_SYNTHESIZE_WIFI wifi状态下使用在线，非wifi离线。在线状态下， 请求超时1.2s自动转离线
        // MIX_MODE_HIGH_SPEED_NETWORK ， 3G 4G wifi状态下使用在线，其它状态离线。在线状态下，请求超时1.2s自动转离线
        // MIX_MODE_HIGH_SPEED_SYNTHESIZE, 2G 3G 4G wifi状态下使用在线，其它状态离线。在线状态下，请求超时1.2s自动转离线
        // 离线资源文件， 从assets目录中复制到临时目录，需要在initTTs方法前完成
        OfflineResource offlineResource = createOfflineResource(offlineVoice);
        // 声学模型文件路径 (离线引擎使用), 请确认下面两个文件存在
        params.put(SpeechSynthesizer.PARAM_TTS_TEXT_MODEL_FILE, offlineResource.getTextFilename());
        params.put(SpeechSynthesizer.PARAM_TTS_SPEECH_MODEL_FILE,
                offlineResource.getModelFilename());

        for (Map.Entry<String, String> e : params.entrySet()) {
            mSpeechSynthesizer.setParam(e.getKey(), e.getValue());
        }
    }

    protected OfflineResource createOfflineResource(String voiceType) {
        OfflineResource offlineResource = null;
        try {
            offlineResource = new OfflineResource(this, voiceType);
        } catch (IOException e) {
            // IO 错误自行处理
            e.printStackTrace();
            LogUtil.e("复制离线语音资源文件失败" + e.getMessage());
        }
        return offlineResource;
    }

    /**
     * 切换离线发音。注意需要添加额外的判断：引擎在合成时该方法不能调用
     */
    public void switchVoice(String mode) {
        stop();
        offlineVoice = mode;
        OfflineResource offlineResource = createOfflineResource(offlineVoice);
        int result = mSpeechSynthesizer.loadModel(offlineResource.getModelFilename(), offlineResource.getTextFilename());
        mSpeechSynthesizer.setParam(SpeechSynthesizer.PARAM_SPEAKER, "1");
        checkResult(result, "loadModel");
        if (!currentString.isEmpty()) mSpeechSynthesizer.speak(currentString);
    }

    /**
     * 更新参数（语速）
     */
    public void changeSpeed(int speed) {
        if (speed > 9) speed = 9;
        if (speed < 0) speed = 0;
        stop();
        // 设置合成的语速，0-9 ，默认 5
        mSpeechSynthesizer.setParam(SpeechSynthesizer.PARAM_SPEED, speed + "");
        if (!currentString.isEmpty()) mSpeechSynthesizer.speak(currentString);
    }

    private void checkResult(int result, String method) {
        if (result != 0) {
            LogUtil.e("baidu TTS error code :" + result + " method:" + method + ", 错误码文档:http://yuyin.baidu.com/docs/tts/122 ");
        }
    }

    /**
     * 暂停播放。仅调用speak后生效
     */
    private void pause() {
        int result = mSpeechSynthesizer.pause();
        checkResult(result, "pause");
    }

    /**
     * 继续播放。仅调用speak后生效，调用pause生效
     */
    private void resume() {
        int result = mSpeechSynthesizer.resume();
        checkResult(result, "resume");
    }

    /**
     * 停止合成引擎。即停止播放，合成，清空内部合成队列。
     */
    private void stop() {
        int result = mSpeechSynthesizer.stop();
        EventBusUtil.sendEvent(new BaseEvent(EventConstants.EVENT_SPEECH_STOP));
        checkResult(result, "stop");
    }

    @Override
    public void onDestroy() {
        stop();
        mSpeechSynthesizer.release();
        LogUtil.e("speechservice 释放资源成功");
        super.onDestroy();
    }

    public class SpeechBinder extends Binder {
        SpeechService getService() {
            return SpeechService.this;
        }
    }
}
