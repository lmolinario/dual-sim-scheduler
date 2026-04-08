package com.lello.dualsimscheduler.ui

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.graphics.Path
import android.view.accessibility.AccessibilityNodeInfo
import com.lello.dualsimscheduler.utils.Logger
import com.lello.dualsimscheduler.utils.NodeUtils

class UserSwitcherController(
    private val accessibilityService: AccessibilityService,
) {

    fun openUserSwitcher(): Boolean {
        // Best-effort fallback: open quick settings shade where many OEMs place user switcher.
        val ok = accessibilityService.performGlobalAction(AccessibilityService.GLOBAL_ACTION_NOTIFICATIONS)
        Logger.i(TAG, "Requested user switcher via notifications shade: $ok")
        return ok
    }

    fun isUserSwitcherVisible(root: AccessibilityNodeInfo?): Boolean {
        val labels = listOf("Utenti multipli", "Lavoro", "Privato")
        return labels.any { label ->
            NodeUtils.findByExactText(root, label) != null || NodeUtils.findByPartialText(root, label) != null
        }
    }

    fun selectWork(root: AccessibilityNodeInfo?): Boolean = selectProfile(root, "Lavoro")

    fun selectPrivate(root: AccessibilityNodeInfo?): Boolean = selectProfile(root, "Privato")

    private fun selectProfile(root: AccessibilityNodeInfo?, label: String): Boolean {
        val node = NodeUtils.findByExactText(root, label)
            ?: NodeUtils.findByPartialText(root, label)

        if (NodeUtils.safeClick(node)) {
            Logger.i(TAG, "Clicked profile label: $label")
            return true
        }

        // Absolute last fallback: gesture tap on node center.
        val center = NodeUtils.center(node)
        if (center != null) {
            val (x, y) = center
            val path = Path().apply { moveTo(x.toFloat(), y.toFloat()) }
            val stroke = GestureDescription.StrokeDescription(path, 0L, 100L)
            val gesture = GestureDescription.Builder().addStroke(stroke).build()
            val dispatched = accessibilityService.dispatchGesture(gesture, null, null)
            Logger.w(TAG, "Gesture fallback for $label at ($x,$y) => $dispatched")
            return dispatched
        }

        Logger.w(TAG, "Unable to select profile: $label")
        return false
    }

    companion object {
        private const val TAG = "UserSwitcherController"
    }
}
