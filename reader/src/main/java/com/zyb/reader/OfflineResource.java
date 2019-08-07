package com.zyb.reader;

import android.content.Context;
import android.content.res.AssetManager;
import android.util.Log;

import com.zyb.reader.util.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

import static android.content.ContentValues.TAG;


/**
 * 离线语音资源管理
 */
public class OfflineResource {

    private static HashMap<String, Boolean> mapInitied = new HashMap<String, Boolean>();
    private AssetManager assets;
    private String destPath;
    private String textFilename;
    private String modelFilename;

    public OfflineResource(Context context, String voiceType) throws IOException {
        context = context.getApplicationContext();
        this.assets = context.getApplicationContext().getAssets();
        this.destPath = FileUtils.createTmpDir(context);
        setOfflineVoiceType(voiceType);
    }

    public String getModelFilename() {
        return modelFilename;
    }

    public String getTextFilename() {
        return textFilename;
    }

    /**
     * m15 离线男声
     * f7 离线女声
     * yyjw 度逍遥
     * as 度丫丫
     */
    public void setOfflineVoiceType(String voiceType) throws IOException {
        String text = "bd_etts_text.dat";
        String model;
        if (Speaker.MALE.getOffline().equals(voiceType)) {
            model = "bd_etts_common_speech_m15_mand_eng_high_am-mix_v3.0.0_20170505.dat";
        } else if (Speaker.FEMALE.getOffline().equals(voiceType)) {
            model = "bd_etts_common_speech_f7_mand_eng_high_am-mix_v3.0.0_20170512.dat";
        } else if (Speaker.DUXY.getOffline().equals(voiceType)) {
            model = "bd_etts_common_speech_yyjw_mand_eng_high_am-mix_v3.0.0_20170512.dat";
        } else if (Speaker.DUYY.getOffline().equals(voiceType)) {
            model = "bd_etts_common_speech_as_mand_eng_high_am_v3.0.0_20170516.dat";
        } else {
            throw new RuntimeException("voice type is not in list");
        }
        textFilename = copyAssetsFile(text);
        modelFilename = copyAssetsFile(model);

    }

    private String copyAssetsFile(String sourceFilename) throws IOException {
        String destFilename = destPath + File.separator + sourceFilename;
        boolean recover = false;
//        Boolean existed = mapInitied.get(sourceFilename); // 启动时完全覆盖一次
//        if (existed == null || !existed) {
//            recover = true;
//        }
        FileUtils.copyFromAssets(assets, sourceFilename, destFilename, recover);
        Log.i(TAG, "文件复制成功：" + destFilename);
        return destFilename;
    }

    /**
     * 发音人
     */
    public enum Speaker {
        MALE("MALE", "0"),
        FEMALE("FEMALE", "1"),
        DUXY("DUXY", "3"),
        DUYY("DUYY", "4");

        /**
         * 在线发音人代码
         */
        private String online;
        /**
         * 离线发音人代码
         */
        private String offline;

        Speaker(String online, String offline) {
            this.online = online;
            this.offline = offline;
        }

        public String getOnline() {
            return online;
        }

        public String getOffline() {
            return offline;
        }
    }
}
