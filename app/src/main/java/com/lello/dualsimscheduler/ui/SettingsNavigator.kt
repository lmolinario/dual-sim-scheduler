package com.lello.dualsimscheduler.ui

import android.content.Context
import android.content.Intent
import android.provider.Settings
import com.lello.dualsimscheduler.utils.Logger

class SettingsNavigator(private val context: Context) {

    fun openSimSettings(): Boolean {
        val intents = listOf(
            Intent("android.settings.NETWORK_OPERATOR_SETTINGS"),
            Intent(Settings.ACTION_NETWORK_OPERATOR_SETTINGS),
            Intent(Settings.ACTION_WIRELESS_SETTINGS),
            Intent(Settings.ACTION_SETTINGS),
        )

        intents.forEach { intent ->
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            if (runCatching { context.startActivity(intent) }.isSuccess) {
                Logger.i(TAG, "Opened settings with action=${intent.action}")
                return true
            }
        }

        Logger.w(TAG, "Unable to open SIM settings with fallback intents")
        return false
    }

    companion object {
        private const val TAG = "SettingsNavigator"
    }
}
