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

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.example.background.Constant.Status
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    var task: MyAsyncTask? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    fun startService(view: View) {
        if (task?.status == Status.RUNNING){
            task?.cancel(true)
        }
        task = MyAsyncTask(this)
        task?.execute(10)
    }

    fun cancelIt(v: View) {
        if (task?.status == Status.RUNNING) {
            task?.cancel(true)
        }
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
            activity?.btn?.setText("Restart")
        }

        override fun onPreExecute() {
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
            activity?.output?.setText("Cancelled!")
            activity?.cancelBtn?.visibility = View.GONE
        }
    }
}
