package com.zyb.mreader.widget;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.google.gson.Gson;
import com.thegrizzlylabs.sardineandroid.Sardine;
import com.thegrizzlylabs.sardineandroid.impl.OkHttpSardine;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * 同步管理器
 * SyncManager(Context context)
 * getCloudFiles(final Handler handler) 获取文件
 * public void upDate(final Handler handler) 更新文件
 * */
public class SyncManager {
    private String serverHostUrl;//WebDav服务器地址，坚果云为：https://dav.jianguoyun.com/dav/
    private String userName;//用户名
    private String password;//密码
    private Sardine sardine;
    private Context context;
//    private SettingManager settingManager;

    public SyncManager(Context context){
        this.context=context;
        //初始化设置,主要是获取用户设定的服务器地址、用户名和密码
//        settingManager=new SettingManager(context);
//        serverHostUrl=settingManager.getUserServer();
//        userName=settingManager.getUsername();
//        password=settingManager.getPassword();
        //初始化sardine，使用OkHttpSardine进行实例化
        sardine=new OkHttpSardine();
    }
    /**
     * 更新数据（即上传数据）
     * @param counterEvents 数据源
     * @param handler 线程回调
     * */
//    private void updateFiles(List<CounterEvent> counterEvents, Handler handler){
//        sardine.setCredentials(userName,password);//设置登录账号，登录
//        //自己规定一种数据格式，这里面我采用的是json
//        Gson gson=new Gson();
//        String jsons=gson.toJson(counterEvents);
//        //把要上传的数据转成byte数组
//        byte[] data=jsons.getBytes();
//        try {
//            //首先判断目标存储路径文件夹存不存在
//            if(!sardine.exists(serverHostUrl+"pickTime/")){
//                //若不存在需要创建目录
//                sardine.createDirectory(serverHostUrl+"pickTime/");
//            }
//            //存入数据
//            sardine.put(serverHostUrl+"pickTime/backup.txt",data);
//            //通知主线程进行下一步操作
//            Message message=new Message();
//            message.what=1;
//            handler.sendMessage(message);
//        } catch (IOException e) {
//            Message message=new Message();
//            message.what=-1;
//            message.obj=e;
//            handler.sendMessage(message);
//            Log.d("啥情况",e.toString());
//            e.printStackTrace();
//        }
//    }
    /**
     * 封装的更新方法
     * @param handler 回调
     * */
    public void upDate(final Handler handler){
        if(userName.equals("")||password.equals("")){
//            Toasty.info(context,"请设置账号",Toasty.LENGTH_SHORT).show();
//            Intent intent=new Intent(context, SettingActivity.class);
//            context.startActivity(intent);
        }else {
            //开启线程进行执行操作
            new Thread(new Runnable() {
                @Override
                public void run() {
//                    updateFiles(Manager.getAllCounterEvents(context),handler);
                }
            }).start();
        }
    }
    /**
     * 封装的获取文件方法
     * @param handler 回调
     * */
    public void getCloudFiles(final Handler handler){
        if(userName.equals("")||password.equals("")){
//            Toasty.info(context,"请设置账号",Toasty.LENGTH_SHORT).show();
//            Intent intent=new Intent(context, SettingActivity.class);
//            context.startActivity(intent);
        }else {
//            Toasty.info(context,"恢复中",Toasty.LENGTH_SHORT).show();
            new Thread(new Runnable() {
                @Override
                public void run() {
                    getFiles(handler);
                }
            }).start();
        }
    }
    /**
     * 获取文件
     * @param handler 回调
     * */
    private void getFiles(Handler handler){
        sardine.setCredentials(userName,password);//登录。设置账号
        try {
            //拿到输入流
            InputStream inputStream=sardine.get(serverHostUrl+"pickTime/backup.txt");
            //设置输入缓冲区
//            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8)); // 实例化输入流，并获取网页代
//            String s; // 依次循环，至到读的值为空
//            StringBuilder sb = new StringBuilder();
//            while ((s = reader.readLine()) != null) {
//                sb.append(s);
//            }
//            reader.close();

//            String str = sb.toString();
//            //TODO 这里放你的处理逻辑，或者由handler传送给相应处理
//            Message message=new Message();
//            message.what=2;
//            handler.sendMessage(message);
        } catch (IOException e) {
            Message message=new Message();
            message.what=-2;
            message.obj=e;
            handler.sendMessage(message);
            e.printStackTrace();
        }

    }

}