package com.zyb.mreader.module.webdav;


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

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.inject.Inject;

import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import io.reactivex.FlowableEmitter;
import io.reactivex.FlowableOnSubscribe;
import io.reactivex.functions.Function;

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
        addSubscribe(RxUtil.createFlowableData(getSardine())
                .map(new Function<Sardine, List<DavResource>>() {
                    @Override
                    public List<DavResource> apply(Sardine sardine) throws Exception {
                        String serverHostUrl = mDataManager.getWebDavHost() + Constants.WEBDAV_BACKUP_PATH + "/";
                        List<DavResource> list = sardine.list(serverHostUrl);

                        if(list==null||list.isEmpty()) return new ArrayList<>();

                        Iterator<DavResource> it = list.iterator();
                        while (it.hasNext()) {
                            DavResource davResource = it.next();
                            if (davResource.isDirectory()) {
                                it.remove();
                            }
                        }
                        return list;
                    }
                })
                .compose(RxUtil.rxSchedulerHelper())
                .subscribeWith(new CommonSubscriber<List<DavResource>>(mView) {
                    @Override
                    protected void onStartWithViewAlive() {
                        mView.showDialogLoading();
                    }

                    @Override
                    protected void onCompleteWithViewAlive() {
                        mView.hideDialogLoading();
                        mView.onBooksLoadComplete();
                    }

                    @Override
                    protected void onNextWithViewAlive(List<DavResource> davResources) {
                        mView.onBooksLoaded(davResources);
                    }

                    @Override
                    protected void onErrorWithViewAlive(Throwable e) {
                        mView.showToast("抱歉，获取书籍失败了");
                        mView.hideDialogLoading();
                        mView.onBooksLoadComplete();
                    }
                }));
    }

    @Override
    public void upload(List<Book> books) {
        addSubscribe(Flowable.create(new FlowableOnSubscribe<String>() {
            @Override
            public void subscribe(FlowableEmitter<String> emitter) throws Exception {
                for (Book book : books) {
                    if (book.getTitle().equals("欢迎使用")) continue;
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
                    emitter.onNext(book.getPath());
                }
                emitter.onComplete();
            }
        }, BackpressureStrategy.BUFFER)
                .compose(RxUtil.rxSchedulerHelper())
                .subscribeWith(new CommonSubscriber<String>(mView) {
                    @Override
                    protected void onStartWithViewAlive() {
                        mView.showDialogLoading();
                    }

                    @Override
                    protected void onCompleteWithViewAlive() {
                        mView.showToast("已上传至\"" + Constants.WEBDAV_BACKUP_PATH + "\"文件夹");
                        mView.hideDialogLoading();
                        getWebDavBooks();
                    }

                    @Override
                    protected void onNextWithViewAlive(String s) {

                    }

                    @Override
                    protected void onErrorWithViewAlive(Throwable e) {
                        mView.hideDialogLoading();
                        mView.showToast("抱歉，上传失败了");
                    }
                }));
    }

    @Override
    public void download(DavResource davResource, int position) {
        addSubscribe(Flowable.create(new FlowableOnSubscribe<Boolean>() {
            @Override
            public void subscribe(FlowableEmitter<Boolean> emitter) throws Exception {
                if (davResource.isDirectory())
                    emitter.onError(new Throwable("can not download a directory"));
                String serverHostUrl = mDataManager.getWebDavHost() + Constants.WEBDAV_BACKUP_PATH + File.separator;
                InputStream inputStream = sardine.get(serverHostUrl + davResource.getName());

                File file = new File(Environment.getExternalStorageDirectory() + File.separator + Constants.WEBDAV_BACKUP_PATH);
                if (!file.exists()) {
                    if (!file.mkdirs()) {//若创建文件夹不成功
                        System.out.println("Unable to create external cache directory");
                        mView.showToast("无法创建本地文件夹");
                        return;
                    }
                }
                File targetFile = new File(file, davResource.getName());
                boolean isSaved = com.zyb.base.utils.FileUtils.writeFileFromIS(targetFile, inputStream, false);
                if (isSaved) {
                    saveToDb(targetFile);
                }

                emitter.onNext(isSaved);
                emitter.onComplete();
            }
        }, BackpressureStrategy.BUFFER)
                .compose(RxUtil.rxSchedulerHelper())
                .subscribeWith(new CommonSubscriber<Boolean>(mView) {
                    @Override
                    protected void onStartWithViewAlive() {
                        mView.showDialogLoading();
                    }

                    @Override
                    protected void onCompleteWithViewAlive() {
                        EventBusUtil.sendStickyEvent(new BaseEvent<>(EventConstants.EVENT_MAIN_REFRESH_BOOK_SHELF));
                        mView.hideDialogLoading();
                    }

                    @Override
                    protected void onNextWithViewAlive(Boolean isSaved) {
                        if (!isSaved) {
                            mView.showToast("抱歉，下载失败了~");
                            return;
                        }
                        mView.onBookDownloaded(position);
                    }

                    @Override
                    protected void onErrorWithViewAlive(Throwable e) {
                        e.printStackTrace();
                        mView.hideDialogLoading();
                        mView.showToast("抱歉，下载失败了");
                    }
                }));
    }

    @Override
    public void delete(List<DavResource> davResources) {

        addSubscribe(Flowable.create(new FlowableOnSubscribe<Boolean>() {
            @Override
            public void subscribe(FlowableEmitter<Boolean> emitter) throws Exception {
                Sardine sardine = getSardine();

                for (DavResource davResource : davResources) {
                    String serverHostUrl = mDataManager.getWebDavHost() + Constants.WEBDAV_BACKUP_PATH + "/";
                    sardine.delete(serverHostUrl + davResource.getName());
                    emitter.onNext(true);
                }
                emitter.onComplete();
            }
        }, BackpressureStrategy.BUFFER)
                .compose(RxUtil.rxSchedulerHelper())
                .subscribeWith(new CommonSubscriber<Boolean>(mView) {
                    @Override
                    protected void onStartWithViewAlive() {
                        mView.showDialogLoading();
                    }

                    @Override
                    protected void onCompleteWithViewAlive() {
                        if (mView == null) return;
                        mView.hideDialogLoading();
                        mView.onBookDeleted();
                    }

                    @Override
                    protected void onNextWithViewAlive(Boolean isSaved) {
                    }

                    @Override
                    protected void onErrorWithViewAlive(Throwable e) {
                        e.printStackTrace();
                        if (mView == null) return;
                        mView.hideDialogLoading();
                        mView.showToast("抱歉，删除失败了");
                    }
                }));
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
