/*
 * Copyright (C) 2017 The Android Open Source Project
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

package com.sg2022.we_got_the_moves;

import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * Global executor PoolThreads for the whole application.
 * <p>
 * Grouping tasks like this avoids the effects of task starvation (e.g. disk reads don't wait behind
 * webservice requests).
 */
public class AppExecutors {

    private static final String TAG = "AppExecutors";

    private static final int NUMBER_OF_THREADS = 3;
    private static volatile AppExecutors INSTANCE;

    private final Executor singleThread;
    private final Executor PoolThread;
    private final Executor mainThread;

    public AppExecutors() {
        this(Executors.newSingleThreadExecutor(), Executors.newFixedThreadPool(NUMBER_OF_THREADS),
                new mainThreadExecutor());
    }

    private AppExecutors(Executor singleThread, Executor PoolThread, Executor mainThread) {
        this.singleThread = singleThread;
        this.PoolThread = PoolThread;
        this.mainThread = mainThread;
    }

    public static AppExecutors getInstance() {
        if (INSTANCE == null) {
            synchronized (AppExecutors.class) {
                if (INSTANCE == null) {
                    INSTANCE = new AppExecutors();
                }
            }
        }
        return INSTANCE;
    }

    public Executor getSingleThread() {
        return singleThread;
    }

    public Executor getPoolThread() {
        return PoolThread;
    }

    public Executor getMainThread() {
        return mainThread;
    }

    private static class mainThreadExecutor implements Executor {
        private Handler mainThreadHandler = new Handler(Looper.getMainLooper());

        @Override
        public void execute(@NonNull Runnable command) {
            mainThreadHandler.post(command);
        }
    }
}
