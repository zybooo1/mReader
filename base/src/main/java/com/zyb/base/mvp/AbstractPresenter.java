package com.zyb.base.mvp;

import android.content.Context;
import android.support.annotation.Nullable;
import android.widget.ProgressBar;

import com.liulishuo.okdownload.DownloadTask;
import com.liulishuo.okdownload.SpeedCalculator;
import com.liulishuo.okdownload.StatusUtil;
import com.liulishuo.okdownload.core.Util;
import com.liulishuo.okdownload.core.breakpoint.BlockInfo;
import com.liulishuo.okdownload.core.breakpoint.BreakpointInfo;
import com.liulishuo.okdownload.core.cause.EndCause;
import com.liulishuo.okdownload.core.listener.DownloadListener4WithSpeed;
import com.liulishuo.okdownload.core.listener.assist.Listener4SpeedAssistExtend;
import com.zyb.base.utils.LogUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import io.reactivex.Flowable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;
import top.zibin.luban.Luban;

import static com.zyb.base.utils.constant.Constants.FILE_CACHE_PATH;
import static com.zyb.base.utils.constant.Constants.IMG_CACHE_PATH;


/**
 * Base Presenter
 * 管理事件流订阅的生命周期
 */

public class AbstractPresenter<T extends BaseView, D extends BaseDataManager> implements BasePresenter<T> {

    protected T mView;
    private CompositeDisposable compositeDisposable;
    protected D mDataManager;

    @Inject
    protected AbstractPresenter(D dataManager) {
        mDataManager = dataManager;
    }

    protected void addSubscribe(Disposable disposable) {
        if (compositeDisposable == null) {
            compositeDisposable = new CompositeDisposable();
        }
        compositeDisposable.add(disposable);
    }

    @Override
    public void attachView(T view) {
        this.mView = view;
    }

    @Override
    public void detachView() {
        this.mView = null;
        if (compositeDisposable != null) {
            compositeDisposable.clear();
        }
        if (task != null) task.cancel();
    }

//    public UserInfoBean.DataBean getUserBean(){
//        return mDataManager.getUserBean();
//    }

    private String getImageChchePath() {
        File file = new File(IMG_CACHE_PATH);
        if (file.mkdirs()) {
            return IMG_CACHE_PATH;
        }
        return IMG_CACHE_PATH;
    }

    public void compressImgs(List<String> photos) {
        if (mView == null) return;
        mView.showDialogLoading();
        addSubscribe(Flowable.just(photos)
                .observeOn(Schedulers.io())
                .map(new Function<List<String>, List<File>>() {
                    @Override
                    public List<File> apply(@NonNull List<String> list) throws Exception {
                        return Luban.with((Context) mView)
                                .setTargetDir(getImageChchePath())
                                .load(list)
                                .get();
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .doOnError(new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) {
                        LogUtil.e("compressImgs err:" + throwable.getMessage());
                        if (mView == null) return;
                        if (mView != null) mView.getCompressedImgs(photos);
                    }
                })
                .onErrorResumeNext(Flowable.<List<File>>empty())
                .subscribe(new Consumer<List<File>>() {
                    @Override
                    public void accept(@NonNull List<File> list) {
                        if (mView == null) return;
                        List<String> imgs = new ArrayList<>();
                        for (File file : list) {
                            imgs.add(file.getAbsolutePath());
                        }
                        mView.getCompressedImgs(imgs);
                    }
                }));
    }


    /**
     * 文件下载
     */
    private DownloadTask task;

    public void initSingleDownload(String url) {
        initTask(url);
        initStatus();
        initAction();
    }

    private String getFileChchePath() {
        File file = new File(FILE_CACHE_PATH);
        if (file.mkdirs()) {
            return FILE_CACHE_PATH;
        }
        return FILE_CACHE_PATH;
    }

    private void initTask(String url) {
        final File parentFile = new File(getFileChchePath());
        task = new DownloadTask.Builder(url, parentFile)
                .setFilename(System.currentTimeMillis()+url.substring(url.lastIndexOf(".")))//不指定则自动命名
                .setMinIntervalMillisCallbackProcess(100)
                // ignore the same task has already completed in the past.
                .setPassIfAlreadyCompleted(false)
                .build();
    }

    private void initStatus() {
        final StatusUtil.Status status = StatusUtil.getStatus(task);
        if (status == StatusUtil.Status.COMPLETED) {
//            progressBar.setProgress(progressBar.getMax());
        }

//        statusTv.setText(status.toString());
        final BreakpointInfo info = StatusUtil.getCurrentInfo(task);
        if (info != null) {
            LogUtil.e("init status with: " + info.toString());
//            calcProgressToView(progressBar, info.getTotalOffset(), info.getTotalLength());
        }
    }

    private void initAction() {
        // to start
        startTask();
        // mark
        task.setTag("mark-task-started");
    }

    private void startTask() {
        mView.showDialogLoading();
        task.enqueue(new DownloadListener4WithSpeed() {
            private long totalLength;
            private String readableTotalLength;

            @Override
            public void taskStart(@NonNull DownloadTask task) {
//                statusTv.setText("任务开始");
            }

            @Override
            public void infoReady(@NonNull DownloadTask task, @NonNull BreakpointInfo info,
                                  boolean fromBreakpoint,
                                  @NonNull Listener4SpeedAssistExtend.Listener4SpeedModel model) {
                totalLength = info.getTotalLength();
                readableTotalLength = Util.humanReadableBytes(totalLength, true);
//                calcProgressToView(progressBar, info.getTotalOffset(), totalLength);
            }

            @Override
            public void connectStart(@NonNull DownloadTask task, int blockIndex,
                                     @NonNull Map<String, List<String>> requestHeaders) {
                LogUtil.e("connectStart " + blockIndex);
            }

            @Override
            public void connectEnd(@NonNull DownloadTask task, int blockIndex, int responseCode,
                                   @NonNull Map<String, List<String>> responseHeaders) {
                LogUtil.e("connectEnd " + blockIndex);
            }

            @Override
            public void progressBlock(@NonNull DownloadTask task, int blockIndex,
                                      long currentBlockOffset,
                                      @NonNull SpeedCalculator blockSpeed) {
            }

            @Override
            public void progress(@NonNull DownloadTask task, long currentOffset,
                                 @NonNull SpeedCalculator taskSpeed) {
                final String readableOffset = Util.humanReadableBytes(currentOffset, true);
                final String progressStatus = readableOffset + "/" + readableTotalLength;
                final String speed = taskSpeed.speed();
                final String progressStatusWithSpeed = progressStatus + "(" + speed + ")";

//                statusTv.setText(progressStatusWithSpeed);
//                calcProgressToView(progressBar, currentOffset, totalLength);
            }

            @Override
            public void blockEnd(@NonNull DownloadTask task, int blockIndex, BlockInfo info,
                                 @NonNull SpeedCalculator blockSpeed) {
            }

            @Override
            public void taskEnd(@NonNull DownloadTask task, @NonNull EndCause cause,
                                @Nullable Exception realCause,
                                @NonNull SpeedCalculator taskSpeed) {
                final String statusWithSpeed = "加载完成" + " " + taskSpeed.averageSpeed();
//                statusTv.setText(statusWithSpeed);
                // mark
                task.setTag(null);
                if (mView != null)
                    mView.onFileDownloaded(task.getFile() == null ? "" : task.getFile().getAbsolutePath());
//                获取文件MD5
//                final String realMd5 = FileUtils.getFileMD5ToString(task.getFile().getAbsolutePath());

            }
        });
    }

    private void calcProgressToView(ProgressBar progressBar, long offset, long total) {
        final float percent = (float) offset / total;
        progressBar.setProgress((int) (percent * progressBar.getMax()));
    }

    private boolean isTaskRunning() {
        final StatusUtil.Status status = StatusUtil.getStatus(task);
        return status == StatusUtil.Status.PENDING || status == StatusUtil.Status.RUNNING;
    }
}
