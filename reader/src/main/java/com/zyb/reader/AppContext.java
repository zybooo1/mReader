package com.zyb.reader;

import android.app.Application;
import android.content.Context;
import android.os.Environment;


import com.zyb.reader.util.PageFactory;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by Administrator on 2016/7/8 0008.
 */
public class AppContext extends Application {
    public static volatile Context applicationContext = null;


    public static final String SAMPLE_DIR_NAME = "baiduTTS";
    public static final String SPEECH_FEMALE_MODEL_NAME = "bd_etts_speech_female.dat";
    public static final String SPEECH_MALE_MODEL_NAME = "bd_etts_speech_male.dat";
    public static final String TEXT_MODEL_NAME = "bd_etts_text.dat";
    public static final String LICENSE_FILE_NAME = "temp_license";
    public static final String ENGLISH_SPEECH_FEMALE_MODEL_NAME = "bd_etts_speech_female_en.dat";
    public static final String ENGLISH_SPEECH_MALE_MODEL_NAME = "bd_etts_speech_male_en.dat";
    public static final String ENGLISH_TEXT_MODEL_NAME = "bd_etts_text_en.dat";

    private static final int PRINT = 0;
    private static final int UI_CHANGE_INPUT_TEXT_SELECTION = 1;
    private static final int UI_CHANGE_SYNTHES_TEXT_SELECTION = 2;

    @Override
    public void onCreate() {
        super.onCreate();
        applicationContext = getApplicationContext();

//        LitePalApplication.initialize(this);

    }


}
