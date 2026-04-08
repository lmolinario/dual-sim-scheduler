package com.lello.dualsimscheduler

import android.accessibilityservice.AccessibilityService
import android.view.accessibility.AccessibilityEvent
import com.lello.dualsimscheduler.automation.AutomationCoordinator
import com.lello.dualsimscheduler.automation.AutomationStateStore
import com.lello.dualsimscheduler.ui.SettingsNavigator
import com.lello.dualsimscheduler.ui.SimUiController
import com.lello.dualsimscheduler.ui.UserSwitcherController
import com.lello.dualsimscheduler.utils.Logger

class ProfileSwitchAccessibilityService : AccessibilityService() {

    private lateinit var store: AutomationStateStore
    private lateinit var coordinator: AutomationCoordinator

    override fun onServiceConnected() {
        super.onServiceConnected()
        store = AutomationStateStore(applicationContext)
        coordinator = AutomationCoordinator(
            store = store,
            settingsNavigator = SettingsNavigator(applicationContext),
            simUiController = SimUiController(),
            userSwitcherController = UserSwitcherController(this),
        )
        Logger.i(TAG, "Accessibility service connected")
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (!::coordinator.isInitialized) return
        Logger.d(TAG, "Event type=${event?.eventType} pkg=${event?.packageName}")
        coordinator.step(rootInActiveWindow)
    }

    override fun onInterrupt() {
        Logger.w(TAG, "Accessibility service interrupted")
    }

    companion object {
        private const val TAG = "ProfileSwitchService"
    }
}
