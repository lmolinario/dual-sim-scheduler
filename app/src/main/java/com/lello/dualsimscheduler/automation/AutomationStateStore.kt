package com.lello.dualsimscheduler.automation

import android.content.Context
import android.content.SharedPreferences

class AutomationStateStore(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun getState(): AutomationState =
        prefs.getString(KEY_STATE, null)?.let { safeEnumValueOf<AutomationState>(it) }
            ?: AutomationState.IDLE

    fun setState(state: AutomationState) {
        prefs.edit().putString(KEY_STATE, state.name).apply()
    }

    fun getPendingAction(): PendingAction =
        prefs.getString(KEY_PENDING_ACTION, null)?.let { safeEnumValueOf<PendingAction>(it) }
            ?: PendingAction.NONE

    fun setPendingAction(action: PendingAction) {
        prefs.edit().putString(KEY_PENDING_ACTION, action.name).apply()
    }

    fun getCurrentProfile(): ProfileType =
        prefs.getString(KEY_CURRENT_PROFILE, null)?.let { safeEnumValueOf<ProfileType>(it) }
            ?: ProfileType.PRIVATE

    fun setCurrentProfile(profileType: ProfileType) {
        prefs.edit().putString(KEY_CURRENT_PROFILE, profileType.name).apply()
    }

    fun getWorkSimLabel(): String =
        prefs.getString(KEY_WORK_SIM_LABEL, DEFAULT_WORK_SIM_LABEL) ?: DEFAULT_WORK_SIM_LABEL

    fun setWorkSimLabel(label: String) {
        prefs.edit().putString(KEY_WORK_SIM_LABEL, label.trim()).apply()
    }

    fun getLastError(): String = prefs.getString(KEY_LAST_ERROR, "") ?: ""

    fun setLastError(error: String) {
        prefs.edit().putString(KEY_LAST_ERROR, error).apply()
    }

    fun getRetryCount(): Int = prefs.getInt(KEY_RETRY_COUNT, 0)

    fun setRetryCount(count: Int) {
        prefs.edit().putInt(KEY_RETRY_COUNT, count).apply()
    }

    fun getLastStateTimestamp(): Long = prefs.getLong(KEY_LAST_STATE_TS, 0L)

    fun setLastStateTimestamp(ts: Long) {
        prefs.edit().putLong(KEY_LAST_STATE_TS, ts).apply()
    }

    private inline fun <reified T : Enum<T>> safeEnumValueOf(name: String): T? =
        runCatching { enumValueOf<T>(name) }.getOrNull()

    companion object {
        private const val PREFS_NAME = "automation_state_store"
        private const val KEY_STATE = "state"
        private const val KEY_PENDING_ACTION = "pending_action"
        private const val KEY_CURRENT_PROFILE = "current_profile"
        private const val KEY_WORK_SIM_LABEL = "work_sim_label"
        private const val KEY_LAST_ERROR = "last_error"
        private const val KEY_RETRY_COUNT = "retry_count"
        private const val KEY_LAST_STATE_TS = "last_state_ts"

        private const val DEFAULT_WORK_SIM_LABEL = "SIM 2"
    }
}
