package com.zyb.mreader.module.webdav;


import android.os.Environment;
import android.view.View;

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

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.inject.Inject;

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

public class WebdavPresenter extends AbstractPresenter<WebdavContract.View, AppDataManager> implements WebdavContract.Presenter {

    PreferenceHelperImpl preferenceHelper;

    @Inject
    WebdavPresenter(AppDataManager dataManager) {
        super(dataManager);
        preferenceHelper = new PreferenceHelperImpl();
    }

    @Override
    public boolean haveWebdavAccount() {
        return !mDataManager.getWebDavHost().isEmpty() &&
                !mDataManager.getWebDavUserName().isEmpty() &&
                !mDataManager.getWebDavPassword().isEmpty();
    }

    @Override
    public void getWebDavBooks() {
        RxUtil.createFlowableData(getSardine())
                .map(new Function<Sardine, List< DavResource>>() {
                    @Override
                    public List< DavResource> apply(Sardine sardine) throws Exception {
                        String serverHostUrl = mDataManager.getWebDavHost() + Constants.WEBDAV_BACKUP_PATH + "/";
                        List<DavResource> list = sardine.list(serverHostUrl);
                        Iterator<DavResource> it = list.iterator();
                        while(it.hasNext()){
                            DavResource davResource = it.next();
                            if(davResource.isDirectory()){
                              it.remove();
                            }
                        }
                        return list;
                    }
                })
                .compose(RxUtil.rxSchedulerHelper())
                .subscribe(new CommonSubscriber<List< DavResource>>(mView) {
                    @Override
                    protected void onStartWithViewAlive() {
                        mView.showDialogLoading();
                    }

                    @Override
                    protected void onCompleteWithViewAlive() {
                        mView.hideDialogLoading();
                    }

                    @Override
                    protected void onNextWithViewAlive(List< DavResource> davResources) {
                        mView.onBooksLoaded(davResources);
                    }

                    @Override
                    protected void onErrorWithViewAlive(Throwable e) {
                        mView.showToast("抱歉，获取书籍失败了");
                        mView.hideDialogLoading();
                    }
                });
    }

    @Override
    public void upload(List<Book> books) {
        if (books.size() <= 0) {
            mView.showToast("无可备份书籍");
            return;
        }

        Observable.fromIterable(books)
                .filter(new Predicate<Book>() {
                    @Override
                    public boolean test(Book book) throws Exception {
                        boolean isIntroductionTxt = book.getTitle().equals("欢迎使用");
                        return !isIntroductionTxt;
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
                                    String serverHostUrl = mDataManager.getWebDavHost();
                                    if (!sardine.exists(serverHostUrl + Constants.WEBDAV_BACKUP_PATH + "/")) {
                                        //若不存在需要创建目录
                                        sardine.createDirectory(serverHostUrl + Constants.WEBDAV_BACKUP_PATH + "/");
                                    }

                                    File bookFile = new File(book.getPath());
                                    //存入数据
                                    sardine.put(serverHostUrl + Constants.WEBDAV_BACKUP_PATH + "/" + bookFile.getName(),
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
                        getWebDavBooks();
                    }
                });
    }

    @Override
    public void download(DavResource davResource, int position) {
        Observable.just(davResource)
                .flatMap(new Function<DavResource, ObservableSource<Boolean>>() {
                    @Override
                    public ObservableSource<Boolean> apply(DavResource davResources) throws Exception {
                        Sardine sardine = getSardine();
                        return Observable.create(new ObservableOnSubscribe<Boolean>() {
                            @Override
                            public void subscribe(ObservableEmitter<Boolean> emitter) throws Exception {
                                try {
                                    if (davResource.isDirectory())
                                        emitter.onError(new Throwable("can not download a directory"));
                                    String serverHostUrl = mDataManager.getWebDavHost() + Constants.WEBDAV_BACKUP_PATH + "/";
                                    InputStream inputStream = sardine.get(serverHostUrl + davResource.getName());
                                    //设置输入缓冲区
                                    write(davResource.getName(), inputStream);
                                    emitter.onNext(true);
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
                        if (mView == null) return;
                        mView.onBookDownloaded(position);
                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                        if (mView == null) return;
                        mView.hideDialogLoading();
                        mView.showToast("抱歉，下载失败了");
                    }

                    @Override
                    public void onComplete() {
                        EventBusUtil.sendStickyEvent(new BaseEvent(EventConstants.EVENT_MAIN_REFRESH_BOOK_SHELF));
                        if (mView == null) return;
                        mView.hideDialogLoading();
                    }
                });
    }

    @Override
    public void delete(List<DavResource> davResources) {
        Observable.just(davResources)
                .flatMap(new Function<List<DavResource>, ObservableSource<Boolean>>() {
                    @Override
                    public ObservableSource<Boolean> apply(List<DavResource> davResources) throws Exception {
                        Sardine sardine = getSardine();
                        return Observable.create(new ObservableOnSubscribe<Boolean>() {
                            @Override
                            public void subscribe(ObservableEmitter<Boolean> emitter) throws Exception {
                                try {
                                    for (DavResource davResource : davResources) {
                                        String serverHostUrl = mDataManager.getWebDavHost() + Constants.WEBDAV_BACKUP_PATH + "/";
                                        sardine.delete(serverHostUrl + davResource.getName());
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
                        if (mView == null) return;
                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                        if (mView == null) return;
                        mView.hideDialogLoading();
                        mView.showToast("抱歉，删除失败了");
                    }

                    @Override
                    public void onComplete() {
                        if (mView == null) return;
                        mView.hideDialogLoading();
                        mView.onBookDeleted();
                    }
                });
    }

    private void write(String filename, InputStream in) {

        File file = new File(Environment.getExternalStorageDirectory() + "/猫豆阅读");
        if (!file.exists()) {
            if (!file.mkdirs()) {//若创建文件夹不成功
                System.out.println("Unable to create external cache directory");
                mView.showToast("无法创建本地文件夹");
                return;
            }
        }
        File targetFile = new File(file, filename);
        if (com.zyb.base.utils.FileUtils.writeFileFromIS(targetFile, in, false)) {
            saveToDb(targetFile);
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
            String userName = mDataManager.getWebDavUserName();
            String password = mDataManager.getWebDavPassword();
            sardine.setCredentials(userName, password);
        }

        return sardine;
    }
}
