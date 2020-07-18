/*
 * Copyright (C) 2020 Rahul Lad
 * Additional changes (C) 2020 by fat-tire same license below
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
package  com.example.background;

import androidx.annotation.MainThread
import androidx.annotation.WorkerThread
import com.example.background.Constant.Status
import kotlinx.coroutines.*


@MainThread
abstract class CoroutinesAsyncTask<Params, Progress, Result> {

    var status: Status = Status.PENDING
    var isCancelled: Boolean = false
    private val job: Job = Job()
    val result = null

    @WorkerThread
    protected abstract fun doInBackground(vararg params: Params?): Result

    @MainThread
    protected open fun onProgressUpdate(vararg values: Progress?) {
    }

    @MainThread
    protected open fun onPostExecute(result: Result?) {
    }

    @MainThread
    protected open fun onPreExecute() {
    }

    @MainThread
    protected open fun onCancelled(result: Result?) {
        onCancelled()
    }

    @MainThread
    protected open fun onCancelled() {
    }

    @MainThread
    fun execute(vararg params: Params): CoroutinesAsyncTask<Params, Progress, Result> {

        if (status != Status.PENDING) {
            if (status == Status.RUNNING) {
                throw IllegalStateException("Cannot execute task:"
                        + " the task is already running.")
            } else if (status == Status.FINISHED) {
                throw IllegalStateException("Cannot execute task:"
                        + " the task has already been executed "
                        + "(a task can be executed only once)")
            }
        }

        status = Status.RUNNING

        // it can be used to setup UI - it should have access to Main Thread
        GlobalScope.launch(Dispatchers.Main) {
            onPreExecute()
        }

        job.invokeOnCompletion {
            if (it is CancellationException) {
                isCancelled = true
                GlobalScope.launch(Dispatchers.Main) {
                    // note unless this is refactored, onCancelled will return null always
                    onCancelled(null)
                }
            }
        }
        // doInBackground works on background thread(default)
        CoroutineScope(Dispatchers.Default + job).launch {
            val result = doInBackground(*params)
            status = Status.FINISHED
            withContext(Dispatchers.Main) {
                if (!job.isCancelled && !isCancelled) {
                    onPostExecute(result)
                }
            }
        }
        return this
    }

    fun cancel(@Suppress("UNUSED_PARAMETER") mayInterruptIfRunning: Boolean): Boolean {
        job.cancel()
        isCancelled = true
        return job.isCancelled
    }

    @WorkerThread
    fun publishProgress(vararg values: Progress) {
        //need to update main thread
        GlobalScope.launch(Dispatchers.Main) {
            if (!job.isCancelled) {
                onProgressUpdate(*values)
            }
        }
    }
}