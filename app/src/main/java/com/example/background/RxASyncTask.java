/*
 * Copyright (C) 2008 The Android Open Source Project
 * Copyright (C) 2020 additions from Fattire
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.background;

import android.util.Log;

import androidx.annotation.MainThread;
import androidx.annotation.WorkerThread;

import com.example.background.Constant.Status;

import java.util.concurrent.atomic.AtomicBoolean;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.ObservableOnSubscribe;
import io.reactivex.rxjava3.core.Observer;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public abstract class RxASyncTask<Params, Progress, Result> {

    private volatile Constant.Status mStatus = Status.PENDING;

    private final AtomicBoolean mCancelled = new AtomicBoolean();

    @SuppressWarnings({"unchecked", "varargs"})
    @MainThread
    public final RxASyncTask<Params, Progress, Result> execute(Params... params) {

        if (mStatus != Status.PENDING) {
            switch (mStatus) {
                case RUNNING:
                    throw new IllegalStateException("Cannot execute task:"
                            + " the task is already running.");
                case FINISHED:
                    throw new IllegalStateException("Cannot execute task:"
                            + " the task has already been executed "
                            + "(a task can be executed only once)");
            }
        }
        mStatus = Status.RUNNING;

        Observable.create((ObservableOnSubscribe<Result>) emitter -> {
            if (!emitter.isDisposed() && !isCancelled()) {
                emitter.onNext(doInBackground(params));
            }
        })
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<Result>() {
                    @Override
                    public void onSubscribe(@NonNull Disposable d) {
                        onPreExecute();
                    }

                    @Override
                    public void onNext(@NonNull Result result) {
                        if (isCancelled()) {
                            onCancelled(result);
                        } else {
                            onPostExecute(result);
                        }
                        mStatus = Status.FINISHED;
                    }

                    @Override
                    public void onError(@NonNull Throwable e) {
                        Log.e("Error!", e.getLocalizedMessage(), e);
                        onCancelled();
                    }

                    @Override
                    public void onComplete() {
                    }
                });
        return this;
    }

    public final Status getStatus() {
        return mStatus;
    }

    @SuppressWarnings({"unchecked", "varargs"})
    @WorkerThread
    protected abstract Result doInBackground(Params... params);

    @MainThread
    protected void onPreExecute() {
    }

    @MainThread
    protected void onPostExecute(Result result) {
    }

    @SuppressWarnings({"UnusedParameters"})
    @MainThread
    protected void onCancelled(Result result) {
        onCancelled();
    }

    @MainThread
    protected void onCancelled() {
    }

    @SuppressWarnings({"UnusedDeclaration", "unchecked", "varargs"})
    @MainThread
    protected void onProgressUpdate(Progress... values) {
    }

    @SuppressWarnings({"unchecked", "varargs"})
    @WorkerThread
    protected final void publishProgress(Progress... values) {
        if (!isCancelled()) {
            onProgressUpdate(values);
        }
    }

    @SuppressWarnings("UnusedParameters")
    public final boolean cancel(boolean mayInterruptIfRunning) {
        mCancelled.set(true);
        mStatus = Status.FINISHED;
        onCancelled();
        return true;
    }

    public final boolean isCancelled() {
        return mCancelled.get();
    }
}