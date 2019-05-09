package com.zyb.mreader.utils;

import android.os.Environment;

import java.io.File;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import io.reactivex.FlowableEmitter;
import io.reactivex.FlowableOnSubscribe;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Single;
import io.reactivex.SingleEmitter;
import io.reactivex.SingleOnSubscribe;

/**
 * Created by newbiechen on 17-5-11.
 */

public class FileUtils {
    //采用自己的格式去设置文件，防止文件被系统文件查询到
    public static final String SUFFIX_WY = ".wy";
    public static final String SUFFIX_TXT = ".txt";
    public static final String SUFFIX_EPUB = ".epub";
    public static final String SUFFIX_PDF = ".pdf";


    //文件名
    public static String getSimpleName(File file) {
        if (file == null) return "";
        String name = file.getName();
        if (file.isDirectory()) return name;
        if (!name.contains(".")) return name;
        if (name.trim().isEmpty()) return "";
        name = name.substring(0, name.lastIndexOf("."));
        return name;
    }


    public static String getFileSize(long size) {
        if (size <= 0) return "0";
        final String[] units = new String[]{"b", "kb", "M", "G", "T"};
        //计算单位的，原理是利用lg,公式是 lg(1024^n) = nlg(1024)，最后 nlg(1024)/lg(1024) = n。
        int digitGroups = (int) (Math.log10(size) / Math.log10(1024));
        //计算原理是，size/单位值。单位值指的是:比如说b = 1024,KB = 1024^2
        return new DecimalFormat("#,##0.##").format(size / Math.pow(1024, digitGroups)) + " " + units[digitGroups];
    }

    //获取txt文件
    public static List<File> getTxtFiles(String filePath) {
        List txtFiles = new ArrayList();
        File file = new File(filePath);
        //获取文件夹
        File[] dirs = file.listFiles(
                pathname -> {
                    if (pathname.isDirectory() && !pathname.getName().startsWith(".")) {
                        return true;
                    } else if (pathname.getName().endsWith(".txt")) { //获取txt文件
                        txtFiles.add(pathname);
                        return false;
                    } else {
                        return false;
                    }
                }
        );
        //遍历文件夹
        for (File dir : dirs) {
            //递归遍历txt文件
            txtFiles.addAll(getTxtFiles(dir.getPath()));
        }
        return txtFiles;
    }

    //由于遍历比较耗时
    public static Single<List<File>> getSDTxtFile() {
        //外部存储卡路径
        String rootPath = Environment.getExternalStorageDirectory().getPath();
        return Single.create(new SingleOnSubscribe<List<File>>() {
            @Override
            public void subscribe(SingleEmitter<List<File>> e) throws Exception {
                List<File> files = getTxtFiles(rootPath);
                e.onSuccess(files);
            }
        });
    }

    //由于遍历比较耗时
    public static Observable<List<File>> scanTxtFiles() {
        //外部存储卡路径
        String rootPath = Environment.getExternalStorageDirectory().getPath();
        return Observable.create(new ObservableOnSubscribe<List<File>>() {
            @Override
            public void subscribe(ObservableEmitter<List<File>> emitter) throws Exception {
                List<File> files = getTxtFiles(rootPath);
                emitter.onNext(files);
            }
        });
    }

    //由于遍历比较耗时
    public static Flowable<List<File>> scanTxtFile() {
        //外部存储卡路径
        String rootPath = Environment.getExternalStorageDirectory().getPath();
        return Flowable.create(new FlowableOnSubscribe<List<File>>() {
            @Override
            public void subscribe(FlowableEmitter<List<File>> emitter) throws Exception {
                List<File> files = getTxtFiles(rootPath);
                emitter.onNext(files);
            }
        }, BackpressureStrategy.BUFFER);
    }

    public static String getChildNum(File file) {
        return file.listFiles().length + "项";
    }
}
