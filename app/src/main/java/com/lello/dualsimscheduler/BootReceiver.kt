package com.lello.dualsimscheduler

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.lello.dualsimscheduler.utils.Logger

class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent?) {
        if (intent?.action == Intent.ACTION_BOOT_COMPLETED) {
            Logger.i(TAG, "Boot completed; scheduling placeholder receiver")
            context.sendBroadcast(Intent(context, ScheduleReceiver::class.java))
        }
    }

    companion object {
        private const val TAG = "BootReceiver"
    }
}
