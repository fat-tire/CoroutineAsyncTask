/*
 * Copyright (C) 2020 Rahul Lad (original in Kotlin)
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

package com.example.background;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.background.Constant.Status;

public class MainActivityInJava extends AppCompatActivity {

    MyAsyncTask task = null;
    MyRxASyncTask task2 = null;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ((Button) findViewById(R.id.btn2)).setText("(Try the Kotlin Activity)");
        ((TextView) findViewById(R.id.header)).setText("This is a Java Activity");

    }

    public void startCouroutinesService(View view) {
        if (task != null && task.getStatus() == Status.RUNNING) {
            task.cancel(true);
        }
        task = new MyAsyncTask(this);
        task.execute(10);
    }

    public void startRxService(View view) {
        if (task2 != null && task2.getStatus() == Status.RUNNING) {
            task2.cancel(true);
        }
        task2 = new MyRxASyncTask(this);
        task2.execute(10);
    }

    public void launchOtherActivity(View view) {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

    public void cancelIt(View v) {
        if (task != null && task.getStatus() == Status.RUNNING) {
            task.cancel(true);
        }
        if (task2 != null && task2.getStatus() == Status.RUNNING) {
            task2.cancel(true);
        }
    }

    class MyAsyncTask extends CoroutinesAsyncTask<Integer, Integer, String> {

        MainActivityInJava activity;

        public MyAsyncTask(MainActivityInJava activity) {
            this.activity = activity;
        }

        @Override
        protected String doInBackground(Integer... integers) {
            for (int count = 1; count <= 10; count++) {
                if (this.isCancelled())
                    break;
                try {
                    Thread.sleep(1000);
                    publishProgress(count);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            return "Done!!!";
        }

        @Override
        protected void onPostExecute(String result) {
            activity.findViewById(R.id.progressBar).setVisibility(View.GONE);
            ((TextView) activity.findViewById(R.id.output)).setText(result);
            ((Button) activity.findViewById(R.id.btn)).setText("Try RXSyncTask");
            activity.findViewById(R.id.btn).setOnClickListener(v -> activity.startRxService(v));
            findViewById(R.id.cancelBtn).setVisibility(View.GONE);
        }

        @Override
        protected void onPreExecute() {
            ((Button) activity.findViewById(R.id.btn)).setText("Restart running CoroutineAsync in Java Activity");
            ((TextView) activity.findViewById(R.id.output)).setText("Test starting...");
            activity.findViewById(R.id.progressBar).setVisibility(View.VISIBLE);
            ((ProgressBar) activity.findViewById(R.id.progressBar)).setMax(10);
            ((ProgressBar) activity.findViewById(R.id.progressBar)).setProgress(0);
            findViewById(R.id.cancelBtn).setVisibility(View.VISIBLE);
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            ((TextView) activity.findViewById(R.id.output)).setText("count is " + values[0]);
            ((ProgressBar) activity.findViewById(R.id.progressBar)).setProgress(values[0]);
        }

        @Override
        protected void onCancelled() {
            ((TextView) activity.findViewById(R.id.output)).setText("Canceled!");
            findViewById(R.id.cancelBtn).setVisibility(View.GONE);
        }
    }

    class MyRxASyncTask extends RxASyncTask<Integer, Integer, String> {
        // note third parameter above can't be a Void- make it a Boolean and return false
        MainActivityInJava activity;

        public MyRxASyncTask(MainActivityInJava activity) {
            this.activity = activity;
        }

        @Override
        protected String doInBackground(Integer... integers) {
            for (int count = 1; count <= 10; count++) {
                if (this.isCancelled())
                    break;
                try {
                    Thread.sleep(1000);
                    publishProgress(count);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            return "Done!!!";
        }

        @Override
        protected void onPostExecute(String result) {
            activity.findViewById(R.id.progressBar).setVisibility(View.GONE);
            ((TextView) activity.findViewById(R.id.output)).setText(result);
            ((Button) activity.findViewById(R.id.btn)).setText("Try CoroutineASyncTask");
            activity.findViewById(R.id.btn).setOnClickListener(v -> activity.startCouroutinesService(v));
            findViewById(R.id.cancelBtn).setVisibility(View.GONE);
        }

        @Override
        protected void onPreExecute() {
            ((Button) activity.findViewById(R.id.btn)).setText("Restart running RxSyncTask in Java Activity");
            ((TextView) activity.findViewById(R.id.output)).setText("Test starting...");
            activity.findViewById(R.id.progressBar).setVisibility(View.VISIBLE);
            ((ProgressBar) activity.findViewById(R.id.progressBar)).setMax(10);
            ((ProgressBar) activity.findViewById(R.id.progressBar)).setProgress(0);
            findViewById(R.id.cancelBtn).setVisibility(View.VISIBLE);
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            ((TextView) activity.findViewById(R.id.output)).setText("count is " + values[0]);
            ((ProgressBar) activity.findViewById(R.id.progressBar)).setProgress(values[0]);
        }

        @Override
        protected void onCancelled() {
            ((TextView) activity.findViewById(R.id.output)).setText("Canceled!");
            findViewById(R.id.cancelBtn).setVisibility(View.GONE);
        }
    }
}

