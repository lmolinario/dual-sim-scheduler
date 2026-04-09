package com.lello.dualsimscheduler.utils

import android.util.Log

object Logger {
    private const val DEFAULT_TAG = "DualSimScheduler"

    fun d(tag: String = DEFAULT_TAG, message: String) {
        Log.d(tag, message)
    }

    fun i(tag: String = DEFAULT_TAG, message: String) {
        Log.i(tag, message)
    }

    fun w(tag: String = DEFAULT_TAG, message: String) {
        Log.w(tag, message)
    }

    fun e(tag: String = DEFAULT_TAG, message: String, throwable: Throwable? = null) {
        Log.e(tag, message, throwable)
    }
}
