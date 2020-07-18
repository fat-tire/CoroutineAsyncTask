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

package com.example.background

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.example.background.Constant.Status
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    var task: MyAsyncTask? = null
    var task2: MyRxASyncTask? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        btn2.setText("(Or try the Java activity)")
        header.setText("This is a Kotlin activity")
    }

    fun startCouroutinesService(view: View) {
        if (task?.status == Status.RUNNING) {
            task?.cancel(true)
        }
        task = MyAsyncTask(this)
        task?.execute(10)
    }

    fun startRxService(view: View) {
        if (task2?.status == Status.RUNNING) {
            task2?.cancel(true)
        }
        task2 = MyRxASyncTask(this)
        task2?.execute(10)
    }

    fun cancelIt(v: View) {
        if (task?.status == Status.RUNNING) {
            task?.cancel(true)
        }
        if (task2?.status == Status.RUNNING) {
            task2?.cancel(true)
        }
    }

    fun launchOtherActivity(view: View) {
        val intent = Intent(this, MainActivityInJava::class.java)
        startActivity(intent)
    }

    class MyAsyncTask(private var activity: MainActivity?) : CoroutinesAsyncTask<Int, Int, String>() {

        override fun doInBackground(vararg params: Int?): String {
            for (count in 1..10) {
                if (isCancelled)
                    break
                try {
                    Thread.sleep(1000)
                    publishProgress(count)
                } catch (e: InterruptedException) {
                    e.printStackTrace()
                }
            }
            return "Done!!!"
        }

        override fun onPostExecute(result: String?) {
            activity?.progressBar?.visibility = View.GONE
            activity?.output?.setText(result)
            activity?.btn?.setText("Try RXSyncTask")
            activity?.btn?.setOnClickListener { view -> activity?.startRxService(view) }
        }

        override fun onPreExecute() {
            activity?.btn?.text = "Restart Running CoroutineAsyncTask in Kotlin Activity"
            activity?.output?.text = "Test starting..."
            activity?.progressBar?.visibility = View.VISIBLE
            activity?.progressBar?.max = 10
            activity?.progressBar?.progress = 0
            activity?.cancelBtn?.visibility = View.VISIBLE
        }

        override fun onProgressUpdate(vararg values: Int?) {
            activity?.output?.setText("count is ${values.get(0).toString()}")
            values[0]?.let {
                activity?.progressBar?.setProgress(it)
            }
        }

        override fun onCancelled(result: String?) {
            activity?.output?.text = "Cancelled!"
            activity?.cancelBtn?.visibility = View.GONE
        }
    }

    class MyRxASyncTask(private var activity: MainActivity?) : RxASyncTask<Int, Int, String>() {
        // note third parameter above can't be a Void- make it a Boolean and return false

        override fun doInBackground(vararg params: Int?): String {
            for (count in 1..10) {
                if (isCancelled)
                    break
                try {
                    Thread.sleep(1000)
                    publishProgress(count)
                } catch (e: InterruptedException) {
                    e.printStackTrace()
                }
            }
            return "Done!!!"
        }

        override fun onPostExecute(result: String?) {
            activity?.progressBar?.visibility = View.GONE
            activity?.output?.setText(result)
            activity?.btn?.setText("Try CoroutineASyncTask")
            activity?.btn?.setOnClickListener { view -> activity?.startCouroutinesService(view) }
            activity?.cancelBtn?.visibility = View.GONE
        }

        override fun onPreExecute() {
            activity?.output?.setText("Test starting..")
            activity?.btn?.setText("Restart running RxSyncTask in Kotlin Activity")
            activity?.progressBar?.visibility = View.VISIBLE
            activity?.progressBar?.max = 10
            activity?.progressBar?.progress = 0
            activity?.cancelBtn?.visibility = View.VISIBLE
        }

        override fun onProgressUpdate(vararg values: Int?) {
            activity?.output?.setText("count is ${values.get(0).toString()}")
            values[0]?.let {
                activity?.progressBar?.setProgress(it)
            }
        }

        override fun onCancelled(result: String?) {
            activity?.output?.setText("Cancelled!")
            activity?.cancelBtn?.visibility = View.GONE
        }
    }
}
