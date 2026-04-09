package com.lello.dualsimscheduler.automation

import android.os.SystemClock
import android.view.accessibility.AccessibilityNodeInfo
import com.lello.dualsimscheduler.ui.SettingsNavigator
import com.lello.dualsimscheduler.ui.SimUiController
import com.lello.dualsimscheduler.ui.UserSwitcherController
import com.lello.dualsimscheduler.utils.Logger

class AutomationCoordinator(
    private val store: AutomationStateStore,
    private val settingsNavigator: SettingsNavigator,
    private val simUiController: SimUiController,
    private val userSwitcherController: UserSwitcherController,
) {

    fun startGoPrivate() {
        store.setPendingAction(PendingAction.GO_PRIVATE)
        transitionTo(AutomationState.START_GO_PRIVATE)
    }

    fun startGoWork() {
        store.setPendingAction(PendingAction.GO_WORK)
        transitionTo(AutomationState.START_GO_WORK)
    }

    fun setError(message: String) {
        Logger.e(TAG, message)
        store.setLastError(message)
        transitionTo(AutomationState.ERROR)
    }

    fun reset() {
        store.setLastError("")
        store.setPendingAction(PendingAction.NONE)
        store.setRetryCount(0)
        transitionTo(AutomationState.IDLE)
    }

    fun step(root: AccessibilityNodeInfo?) {
        val now = SystemClock.elapsedRealtime()
        val state = store.getState()
        if (isTimedOut(now)) {
            handleRetryOrError("Timeout while in state=$state")
            return
        }

        when (state) {
            AutomationState.IDLE,
            AutomationState.COMPLETED,
            AutomationState.ERROR,
            -> Unit

            AutomationState.START_GO_PRIVATE -> {
                if (settingsNavigator.openSimSettings()) {
                    transitionTo(AutomationState.OPENING_SIM_SETTINGS_FOR_DISABLE)
                } else handleRetryOrError("Cannot open SIM settings for disabling")
            }

            AutomationState.OPENING_SIM_SETTINGS_FOR_DISABLE -> {
                if (simUiController.isOnSimSettingsScreen(root)) {
                    transitionTo(AutomationState.DISABLING_WORK_SIM)
                }
            }

            AutomationState.DISABLING_WORK_SIM -> {
                if (simUiController.disableWorkSim(root, store.getWorkSimLabel())) {
                    transitionTo(AutomationState.WAITING_WORK_SIM_DISABLED)
                } else handleRetryOrError("Cannot disable work SIM")
            }

            AutomationState.WAITING_WORK_SIM_DISABLED -> {
                if (simUiController.isWorkSimDisabled(root, store.getWorkSimLabel())) {
                    transitionTo(AutomationState.OPENING_USER_SWITCHER_FOR_PRIVATE)
                }
            }

            AutomationState.OPENING_USER_SWITCHER_FOR_PRIVATE -> {
                if (userSwitcherController.openUserSwitcher()) {
                    transitionTo(AutomationState.SWITCHING_TO_PRIVATE)
                } else handleRetryOrError("Cannot open user switcher for private")
            }

            AutomationState.SWITCHING_TO_PRIVATE -> {
                if (userSwitcherController.isUserSwitcherVisible(root) && userSwitcherController.selectPrivate(root)) {
                    // TODO: Do not mark profile as PRIVATE here.
                    // Real confirmation must happen after detecting that the device actually switched
                    // into the target profile context.
                    transitionTo(AutomationState.ARRIVED_PRIVATE)
                }
            }

            AutomationState.ARRIVED_PRIVATE -> transitionTo(AutomationState.COMPLETED)

            AutomationState.START_GO_WORK -> {
                if (userSwitcherController.openUserSwitcher()) {
                    transitionTo(AutomationState.OPENING_USER_SWITCHER_FOR_WORK)
                } else handleRetryOrError("Cannot open user switcher for work")
            }

            AutomationState.OPENING_USER_SWITCHER_FOR_WORK -> {
                if (userSwitcherController.isUserSwitcherVisible(root)) {
                    transitionTo(AutomationState.SWITCHING_TO_WORK)
                }
            }

            AutomationState.SWITCHING_TO_WORK -> {
                if (userSwitcherController.selectWork(root)) {
                    // TODO: Do not mark profile as WORK here.
                    // Real confirmation must happen after detecting that the device actually switched
                    // into the target profile context.
                    transitionTo(AutomationState.ARRIVED_WORK)
                }
            }

            AutomationState.ARRIVED_WORK -> {
                if (settingsNavigator.openSimSettings()) {
                    transitionTo(AutomationState.OPENING_SIM_SETTINGS_FOR_ENABLE)
                } else handleRetryOrError("Cannot open SIM settings for enabling")
            }

            AutomationState.OPENING_SIM_SETTINGS_FOR_ENABLE -> {
                if (simUiController.isOnSimSettingsScreen(root)) {
                    transitionTo(AutomationState.ENABLING_WORK_SIM)
                }
            }

            AutomationState.ENABLING_WORK_SIM -> {
                if (simUiController.enableWorkSim(root, store.getWorkSimLabel())) {
                    transitionTo(AutomationState.WAITING_WORK_SIM_ENABLED)
                } else handleRetryOrError("Cannot enable work SIM")
            }

            AutomationState.WAITING_WORK_SIM_ENABLED -> {
                if (simUiController.isWorkSimEnabled(root, store.getWorkSimLabel())) {
                    transitionTo(AutomationState.COMPLETED)
                }
            }
        }
    }

    private fun transitionTo(state: AutomationState) {
        Logger.d(TAG, "Transition: ${store.getState()} -> $state")
        store.setState(state)
        store.setLastStateTimestamp(SystemClock.elapsedRealtime())
    }

    private fun isTimedOut(now: Long): Boolean {
        val lastTs = store.getLastStateTimestamp()
        return lastTs > 0L && now - lastTs > STATE_TIMEOUT_MS
    }

    private fun handleRetryOrError(reason: String) {
        val current = store.getRetryCount()
        if (current < MAX_RETRIES) {
            store.setRetryCount(current + 1)
            store.setLastError(reason)
            store.setLastStateTimestamp(SystemClock.elapsedRealtime())
            Logger.w(TAG, "Retry ${current + 1}/$MAX_RETRIES reason=$reason")
        } else {
            setError("$reason (max retries reached)")
        }
    }

    companion object {
        private const val TAG = "AutomationCoordinator"
        private const val STATE_TIMEOUT_MS = 7_000L
        private const val MAX_RETRIES = 4
    }
}
