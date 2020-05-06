package com.zyb.mreader.module.backup;


import android.os.Environment;

import com.thegrizzlylabs.sardineandroid.DavResource;
import com.thegrizzlylabs.sardineandroid.Sardine;
import com.thegrizzlylabs.sardineandroid.impl.OkHttpSardine;
import com.zyb.base.event.BaseEvent;
import com.zyb.base.event.EventConstants;
import com.zyb.base.http.CommonSubscriber;
import com.zyb.base.mvp.AbstractPresenter;
import com.zyb.base.utils.EventBusUtil;
import com.zyb.base.utils.RxUtil;
import com.zyb.base.utils.constant.Constants;
import com.zyb.common.db.DBFactory;
import com.zyb.common.db.bean.Book;
import com.zyb.mreader.core.AppDataManager;
import com.zyb.mreader.core.prefs.PreferenceHelperImpl;
import com.zyb.mreader.utils.FileUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

import javax.inject.Inject;

import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import io.reactivex.FlowableEmitter;
import io.reactivex.FlowableOnSubscribe;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.ObservableSource;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Function;
import io.reactivex.functions.Predicate;

/**
 *
 */

public class BackupPresenter extends AbstractPresenter<BackupContract.View, AppDataManager> implements BackupContract.Presenter {

    PreferenceHelperImpl preferenceHelper;

    @Inject
    BackupPresenter(AppDataManager dataManager) {
        super(dataManager);
        preferenceHelper = new PreferenceHelperImpl();
    }

    @Override
    public String getWebDevHost() {
        return mDataManager.getWebDevHost();
    }

    @Override
    public String getWebDevUserName() {
        return mDataManager.getWebDevUserName();
    }

    @Override
    public String getWebDevPassword() {
        return mDataManager.getWebDevPassword();
    }

    @Override
    public void login(String userName, String password, String host) {
        addSubscribe(
                Flowable.create(new FlowableOnSubscribe<Boolean>() {
                    @Override
                    public void subscribe(FlowableEmitter<Boolean> emitter) throws Exception {
                        try {
                            Sardine sardine = new OkHttpSardine();
                            sardine.setCredentials(userName, password);
                            emitter.onNext(sardine.exists(host));
                            emitter.onComplete();
                        } catch (Exception e) {
                            emitter.onError(e);
                        }
                    }
                }, BackpressureStrategy.BUFFER)
                        .compose(RxUtil.<Boolean>rxSchedulerHelper())
                        .subscribeWith(new CommonSubscriber<Boolean>(mView) {

                            @Override
                            protected void onStartWithViewAlive() {
                                mView.showDialogLoading();
                            }

                            @Override
                            protected void onCompleteWithViewAlive() {
                                mView.hideDialogLoading();
                            }

                            @Override
                            protected void onNextWithViewAlive(Boolean b) {
                                if (b) {
                                    mDataManager.setWebDevUserName(userName);
                                    mDataManager.setWebDevPassword(password);
                                    mDataManager.setWebDevHost(host);
                                    mView.showToast("登录成功");
                                    mView.loginSuccess();
                                } else {
                                    mView.showToast("登录失败");
                                }
                            }

                            @Override
                            protected void onErrorWithViewAlive(Throwable e) {
                                mView.hideDialogLoading();
                                mView.showToast("登录失败");
                            }
                        }));
    }

    @Override
    public void backup() {
        List<Book> books = mDataManager.getAllBooks();

        if (books.size() <= 0) {
            mView.showToast("无可备份书籍");
            return;
        }

        Observable.fromIterable(books)
                .filter(new Predicate<Book>() {
                    @Override
                    public boolean test(Book book) throws Exception {
                        // TODO: 2020/5/1  应该过滤掉 欢迎使用.txt
                        return true;
                    }
                })
                .flatMap(new Function<Book, ObservableSource<Boolean>>() {
                    @Override
                    public ObservableSource<Boolean> apply(Book book) throws Exception {

                        return Observable.create(new ObservableOnSubscribe<Boolean>() {
                            @Override
                            public void subscribe(ObservableEmitter<Boolean> emitter) throws Exception {
                                try {
                                    Sardine sardine = getSardine();
                                    //把要上传的数据转成byte数组
                                    //首先判断目标存储路径文件夹存不存在
                                    String serverHostUrl = mDataManager.getWebDevHost();
                                    if (!sardine.exists(serverHostUrl + Constants.WEBDEV_BACKUP_PATH + "/")) {
                                        //若不存在需要创建目录
                                        sardine.createDirectory(serverHostUrl + Constants.WEBDEV_BACKUP_PATH + "/");
                                    }

                                    File bookFile = new File(book.getPath());
                                    //存入数据
                                    sardine.put(serverHostUrl + Constants.WEBDEV_BACKUP_PATH + "/" + bookFile.getName(),
                                            bookFile, "txt");
                                    emitter.onNext(true);
                                    emitter.onComplete();
                                } catch (Exception e) {
                                    emitter.onError(e);
                                }
                            }
                        });
                    }
                }, true)
                .compose(RxUtil.<Boolean>rxObservableSchedulerHelper())
                .subscribe(new Observer<Boolean>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        if (mView == null) return;
                        mView.showDialogLoading();
                    }

                    @Override
                    public void onNext(Boolean b) {
                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                        if (mView == null) return;
                        mView.hideDialogLoading();
                        mView.showToast("抱歉，备份失败了");
                    }

                    @Override
                    public void onComplete() {
                        if (mView == null) return;
                        mView.showToast("已备份至\"猫豆阅读\"文件夹");
                        mView.hideDialogLoading();
                    }
                });
    }

    @Override
    public void recover() {
        Observable.just(true)
                .map(new Function<Boolean, List<DavResource>>() {
                    @Override
                    public List<DavResource> apply(Boolean aBoolean) throws Exception {
                        Sardine sardine = getSardine();
                        String serverHostUrl = mDataManager.getWebDevHost()+ Constants.WEBDEV_BACKUP_PATH+"/";
                        List<DavResource> resources = sardine.list(serverHostUrl);
                        return resources;
                    }
                })
                .flatMap(new Function<List<DavResource>, ObservableSource<Boolean>>() {
                    @Override
                    public ObservableSource<Boolean> apply(List<DavResource> davResources) throws Exception {
                        Sardine sardine = getSardine();
                        return Observable.create(new ObservableOnSubscribe<Boolean>() {
                            @Override
                            public void subscribe(ObservableEmitter<Boolean> emitter) throws Exception {
                                try {
                                    for (DavResource davResource : davResources) {
                                        if(davResource.isDirectory())continue;
                                        String serverHostUrl = mDataManager.getWebDevHost()+ Constants.WEBDEV_BACKUP_PATH+"/";
                                        InputStream inputStream = sardine.get(serverHostUrl+davResource.getName());
                                        //设置输入缓冲区
                                        write(davResource.getName(), inputStream);
                                        emitter.onNext(true);
                                    }
                                    emitter.onComplete();
                                } catch (Exception e) {
                                    emitter.onError(e);
                                }
                            }
                        });
                    }
                })
                .compose(RxUtil.<Boolean>rxObservableSchedulerHelper())
                .subscribe(new Observer<Boolean>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        if (mView == null) return;
                        mView.showDialogLoading();
                    }

                    @Override
                    public void onNext(Boolean b) {
                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                        if (mView == null) return;
                        mView.hideDialogLoading();
                        mView.showToast("抱歉，恢复失败了");
                    }

                    @Override
                    public void onComplete() {
                        EventBusUtil.sendStickyEvent(new BaseEvent(EventConstants.EVENT_MAIN_REFRESH_BOOK_SHELF));
                        if (mView == null) return;
                        mView.showToast("已成功恢复");
                        mView.hideDialogLoading();
                    }
                });
    }

    private void write(String filename, InputStream in) {

        File file = new File(Environment.getExternalStorageDirectory() + "/猫豆阅读");
        if (!file.exists()) {
            if (!file.mkdirs()) {//若创建文件夹不成功
                System.out.println("Unable to create external cache directory");
            }
        }

        File targetFile = new File(file, filename);
        OutputStream os = null;
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8)); // 实例化输入流，并获取网页代
            os = new FileOutputStream(targetFile);
            int ch = 0;
            while ((ch = in.read())!= -1) {
                os.write(ch);
            }
            reader.close();
            os.flush();
            saveToDb(targetFile);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                os.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void saveToDb(File bookFile) {
        for (Book allBook : mDataManager.getAllBooks()) {
            if (allBook.getTitle().equals(FileUtils.getSimpleName(bookFile))) {
                return;
            }
        }

        Book book = new Book();
        book.setId(bookFile.getAbsolutePath());
        book.setTitle(FileUtils.getSimpleName(bookFile));
        book.setPath(bookFile.getAbsolutePath());
        book.setSize(FileUtils.getFileSize(bookFile.length()));
        long time = System.currentTimeMillis();
        book.setAddTime(time);
        book.setLastReadTime(time);
        book.setSort((int) DBFactory.getInstance().getBooksManage().count());
        mDataManager.addBook(book);
    }

    Sardine sardine;

    private Sardine getSardine() {
        if (sardine == null) {
            sardine = new OkHttpSardine();
            String userName = mDataManager.getWebDevUserName();
            String password = mDataManager.getWebDevPassword();
            sardine.setCredentials(userName, password);
        }

        return sardine;
    }
}
