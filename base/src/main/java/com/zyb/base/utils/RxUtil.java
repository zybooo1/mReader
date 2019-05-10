package com.zyb.base.utils;

import com.zyb.base.http.CommonSubscriber;

import java.util.concurrent.TimeUnit;

import io.reactivex.BackpressureStrategy;
import io.reactivex.Completable;
import io.reactivex.CompletableTransformer;
import io.reactivex.Flowable;
import io.reactivex.FlowableEmitter;
import io.reactivex.FlowableOnSubscribe;
import io.reactivex.FlowableTransformer;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.ObservableSource;
import io.reactivex.ObservableTransformer;
import io.reactivex.Single;
import io.reactivex.SingleSource;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;


public class RxUtil {

    /**
     * Flowable线程切换简化
     */
    public static <T> FlowableTransformer<T, T> rxSchedulerHelper() {    //compose简化线程
        return new FlowableTransformer<T, T>() {
            @Override
            public Flowable<T> apply(Flowable<T> observable) {
                return observable.subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread());
            }
        };
    }
    public static <T> SingleSource<T> toSimpleSingle(Single<T> upstream){
        return upstream.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    public static <T> ObservableSource<T> toSimpleSingle(Observable<T> upstream){
        return upstream.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }
    /**
     * Completable线程切换简化
     */
    public static CompletableTransformer completableSchedulerHelper() {
        return new CompletableTransformer() {
            @Override
            public Completable apply(Completable observable) {
                return observable.subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread());
            }
        };
    }

    /**
     * 统一线程处理
     *
     * @param <T> 指定的泛型类型
     * @return ObservableTransformer
     */
    public static <T> ObservableTransformer<T, T> rxObservableSchedulerHelper() {
        return observable -> observable.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    /**
     * 统一返回结果处理
     */
//  public static <T> FlowableTransformer<BaseResponse<T>, T> handleResult() {
//    return new FlowableTransformer<BaseResponse<T>, T>() {
//      @Override
//      public Flowable<T> apply(Flowable<BaseResponse<T>> httpResponseFlowable) {
//        return httpResponseFlowable.flatMap(new Function<BaseResponse<T>, Flowable<T>>() {
//          @Override
//          public Flowable<T> apply(BaseResponse<T> tBaseResponse) throws Exception {
//            /*if (tBaseResponse.code == 200) {
//              return createFlowableData(tBaseResponse.data);
//            } else {
//              return Flowable.error(new ApiException(tBaseResponse.message));
//            }*/
//            return createFlowableData(tBaseResponse.data);
//          }
//        });
//
//      }
//    };
//  }

    /**
     * 生成Flowable
     */
    public static <T> Flowable<T> createFlowableData(final T t) {
        return Flowable.create(new FlowableOnSubscribe<T>() {
            @Override
            public void subscribe(FlowableEmitter<T> emitter) throws Exception {
                try {
                    emitter.onNext(t);
                    emitter.onComplete();
                } catch (Exception e) {
                    emitter.onError(e);
                }
            }
        }, BackpressureStrategy.BUFFER);
    }

    /**
     * 得到 Observable
     *
     * @param <T> 指定的泛型类型
     * @return Observable
     */
    public static  <T> Observable<T> createObservableData(final T t) {
        return Observable.create(new ObservableOnSubscribe<T>() {
            @Override
            public void subscribe(ObservableEmitter<T> emitter) throws Exception {
                emitter.onNext(t);
                emitter.onComplete();
            }
        });
    }

    /**
     * createDelayFlowable
     */
    public static Disposable createDelayObservable(int ms, Consumer<? super Long> consumer) {
        return Observable.timer(ms, TimeUnit.MILLISECONDS)
                .compose(rxObservableSchedulerHelper())
                .subscribe(consumer, throwable -> {});
    }

    /**
     * createDelayFlowable
     */
    public static Disposable createDelayFlowable(int ms, CommonSubscriber<? super Long> consumer) {
        return Flowable.timer(ms, TimeUnit.MILLISECONDS)
                .compose(rxSchedulerHelper())
                .subscribeWith(consumer);
    }
}
