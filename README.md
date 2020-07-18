Replace Android Asynctask with Kotlin Coroutines (or RxJava)
===================================

If you're like me, you use the [**AsyncTask**](https://developer.android.com/reference/android/os/AsyncTask)  class a lot, because, you know, it was
recommended by Google for a decade.  And if you're like me, you panicked in horror when you learned it was [deprecated](https://android-review.googlesource.com/c/platform/frameworks/base/+/1156409) by Google in Android 11 (SDK 30).

See [this video](https://www.youtube.com/watch?v=6manrgTPzyA).

So there are some choices if you've used it a lot.

1. Quickly learn Kotlin and rewrite everything you've ever done to use [coroutines](https://developer.android.com/topic/libraries/architecture/coroutines) or refactor everything to use `java.util.concurrent`.
1. Ignore the deprecation and assume Android will always support **AsyncTask** even though they say not to use it.
1. Really cheat and add [the source code of **AsyncTask**](https://android.googlesource.com/platform/frameworks/base/+/master/core/java/android/os/AsyncTask.java) directly in my project, call it **MySyncTask**, and pretend none of this ever happened.

But maybe a drop-in replacement might work?  I looked on github and found a Kotlin class **CoroutinesAsyncTask.kt** [written originally by ladrahul25](https://github.com/ladrahul25/CoroutineAsyncTask) (which was further modified by me, fat-tire) with a Kotlin-based activity that could help migrate an **AsyncTask** to Kotlin coroutine.

Not every method from **AsyncTask** is supported here, but with some tweaking of the original code and stuff,  `onPreExecute`, `doInBackground()`, `onProgressUpdate()`, `publishProgress()`, and `onPostExecute()`, and maybe even `OnCanceled()` kinda work.  That last one is kind of a work-in-progress.

Since I'd never used [RxJava](https://github.com/ReactiveX/RxJava) before either and barely understand it, I decided to create a similar RxJava-type **AsyncTask** replacement class to compare.  The result is a class named **RxASyncTask.java**.

The demo app here includes BOTH replacement classes used in (1) A [Java Activity](app/src/main/java/com/example/background/MainActivityInJava.java) as well as (2) A [Kotlin Activity](app/src/main/java/com/example/background/MainActivity.kt).

You can try them both and see if either of them work.  And please send any pull requests because I truly have no idea what I'm doing, not having used RxJava **or** Kotlin before this.

__I seriously can't promise anything here.  But it may be a start for someone to play with who knows more about this stuff or like me wants to learn.__

## CoroutinesAsyncTask Notes

This is probaby the one you want to try first.  If you've got a Java project going and want to try it, you'll need to enable Kotlin first because, well, this was written in Kotlin -- coroutines aren't available in Java. But you **can** use it from Java. Look at the demo activities (.kt and .java) to get an idea of how it might work.

#### To Use the CoroutinesAsyncTask class

* First enable Kotlin in your project.  This is a couple lines to `build.gradle` and instructions are available elsewhere for this.

* Now add `CoroutineAsyncTask.kt` to your project's source code and replace the `package ` line with your package.

* Change any line in your original code that references `AsyncTask` to `extend CoroutineAsyncTask` instead.

Be sure to check out the demo activities to see this in action.

## RxASyncTask Notes

Thanks to [this link](https://android.jlelse.eu/how-to-convert-an-asynctask-to-rxjava-80e5d777a40) by Pierce Zaifman suggesting what to put where, I thought I could maybe also try to make an **AsyncTask**-compatible class with RxJava.

This experiment seems to work-- with some minor caveats...

#### To Use the RxASyncTask class

* First, add `RxASyncTask.java` to your project's source code and replace the `package ` line with your package.

* If you're doing more i/o work than computation work, change `Schedulers.io()` to `Schedulers.computation()`.  Not really sure what the difference is, but I think it's whether or not it creates more threads than CPU cores or not (io = yes, computation = no).

* Add these lines to `build.gradle` to bring in the rxJava/rxAndroid dependencies.  (The version number may be different.)

        implementation 'io.reactivex.rxjava3:rxandroid:3.0.0'
        implementation 'io.reactivex.rxjava3:rxjava:3.0.4'

* You may need to [add some lines to proguard-rules.pro](app/proguard-rules.pro#L15-L16) if you use R8 and run into issues.

* Change any line in your original code that extends `AsyncTask` to `extend RxASyncTask` instead.

* **RxASyncTask**'s `doInBackground()` can't return type `Void` (which must always be `null`) because in the RxJava world, the result passed to `onNext()` is always set to `@NonNull`.  If you don't know what that means, don't worry about it.  I barely do either.  But there's an easy fix:

SOLUTION:  Just change any `Void` to `Boolean` and return `false` instead of `null`.    For example:

    private static class MyAddTask extends RxSyncTask<Void, Void, Void> {

would need to become:

    private static class MyAddTask extends RxSyncTask<Void, Void, Boolean> {

and

    protected Void doInBackground(Void... Void)  {

becomes

    protected Boolean doInBackground(Void... Void) {

and then, in `doInBackground()` any time you:

     return null;

Now you

     return false;

And finally

    protected void onPostExecute(Void result) {

   becomes

    protected void onPostExecute(Boolean result) {

As with **CoroutineAsyncTask**, look at the demo activities (kt and java) to see it sort of in action.

I probably should have named the class **RxAsyncTask** with a lower-case "**s**" to match **AsyncTask**... but it's **RxASyncTask**.  Oh well.


--[fat-tire](https://www.twitter.com/fat__tire)
