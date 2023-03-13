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

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class AppExecutors {

  private static final String TAG = "AppExecutors";
  private static final int NUMBER_OF_THREADS = 4;
  private static volatile AppExecutors INSTANCE;

  private final Executor PoolThread;
  private final Executor SingleThread;

  public AppExecutors() {
    this(Executors.newFixedThreadPool(NUMBER_OF_THREADS), Executors.newSingleThreadExecutor());
  }

  private AppExecutors(Executor PoolThread, Executor SingleThread) {
    this.PoolThread = PoolThread;
    this.SingleThread = SingleThread;
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

  public Executor getPoolThread() {
    return this.PoolThread;
  }

  public Executor getSingleThread() {
    return this.SingleThread;
  }
}
