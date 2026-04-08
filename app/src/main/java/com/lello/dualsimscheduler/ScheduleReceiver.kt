package com.lello.dualsimscheduler

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.SystemClock
import com.lello.dualsimscheduler.automation.AutomationStateStore
import com.lello.dualsimscheduler.automation.PendingAction
import com.lello.dualsimscheduler.utils.Logger

class ScheduleReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent?) {
        Logger.i(TAG, "ScheduleReceiver fired action=${intent?.action}")
        val store = AutomationStateStore(context)
        if (store.getPendingAction() == PendingAction.NONE) {
            store.setPendingAction(PendingAction.GO_PRIVATE)
        }

        schedulePlaceholderAlarm(context)
    }

    private fun schedulePlaceholderAlarm(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        val nextIntent = Intent(context, ScheduleReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            1001,
            nextIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        alarmManager.setAndAllowWhileIdle(
            AlarmManager.ELAPSED_REALTIME_WAKEUP,
            SystemClock.elapsedRealtime() + ONE_HOUR_MS,
            pendingIntent,
        )
    }

    companion object {
        private const val TAG = "ScheduleReceiver"
        private const val ONE_HOUR_MS = 60 * 60 * 1000L
    }
}
