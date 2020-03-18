package com.patent.ftpservice.ftpclient;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.observers.DisposableMaybeObserver;
import io.reactivex.observers.DisposableObserver;

public abstract class BaseModel {
    private CompositeDisposable mDisposables = new CompositeDisposable();
    private List<Disposable> mDisposableList = new ArrayList<>();


    public void addDisposable(Disposable disposable) {
        mDisposableList.add(disposable);
    }

    private void removeDisposable() {
        if (mDisposableList == null) {
            return;
        }
        for (int i = 0; i < mDisposableList.size(); i++) {
            if (!mDisposableList.get(i).isDisposed()) {
                mDisposableList.get(i).dispose();
            }
        }
        mDisposableList.clear();
    }

    private void addObserver(DisposableObserver<?> disposableObserver) {
        mDisposables.add(disposableObserver);
    }

    private void addObserver(DisposableMaybeObserver<?> disposableObserver) {
        mDisposables.add(disposableObserver);
    }


    private void removeObserver(DisposableObserver<?> disposableObserver) {
        if (mDisposables == null || disposableObserver == null) {
            return;
        }
        mDisposables.remove(disposableObserver);
    }

    private void removeObserver(DisposableMaybeObserver<?> disposableObserver) {
        if (mDisposables == null || disposableObserver == null) {
            return;
        }
        mDisposables.remove(disposableObserver);
    }

    private void clearObserver() {
        if (mDisposables != null) {
            mDisposables.clear();
        }
    }


    public abstract class BaseSafeObserver<T> extends DisposableObserver<T> {

        @Override
        protected void onStart() {
            super.onStart();
            addObserver(this);
        }

        @Override
        public void onNext(@NonNull T t) {
        }

        @Override
        public void onError(@NonNull Throwable e) {
            removeObserver(this);
        }

        @Override
        public void onComplete() {
            removeObserver(this);

        }
    }


    //用于过滤observable事件，若所有事件都不符合,onComplete才会执行，否则只会执行onSuccess或者onError
    public abstract class BaseSafeMayBeObserver<T> extends DisposableMaybeObserver<T> {

        @Override
        protected void onStart() {
            super.onStart();
            addObserver(this);
        }

        @Override
        public void onSuccess(@NonNull T t) {
            removeObserver(this);
        }

        @Override
        public void onError(@NonNull Throwable e) {
            removeObserver(this);
        }

        @Override
        public void onComplete() {
            removeObserver(this);

        }
    }

    public void releaseResource() {
        clearObserver();
        removeDisposable();
    }
}
