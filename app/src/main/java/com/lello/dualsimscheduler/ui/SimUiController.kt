package com.lello.dualsimscheduler.ui

import android.view.accessibility.AccessibilityNodeInfo
import com.lello.dualsimscheduler.utils.Logger
import com.lello.dualsimscheduler.utils.NodeUtils

class SimUiController {

    fun isOnSimSettingsScreen(root: AccessibilityNodeInfo?): Boolean {
        val keys = listOf("SIM", "Schede SIM", "Usa SIM")
        return keys.any { key -> NodeUtils.findByPartialText(root, key) != null }
    }

    fun disableWorkSim(root: AccessibilityNodeInfo?, workLabel: String): Boolean {
        val candidate = findSimNode(root, workLabel)
        val clicked = NodeUtils.safeClick(candidate)
        Logger.i(TAG, "disableWorkSim(label=$workLabel) result=$clicked")
        return clicked
    }

    fun enableWorkSim(root: AccessibilityNodeInfo?, workLabel: String): Boolean {
        val candidate = findSimNode(root, workLabel)
        val clicked = NodeUtils.safeClick(candidate)
        Logger.i(TAG, "enableWorkSim(label=$workLabel) result=$clicked")
        return clicked
    }

    fun isWorkSimDisabled(root: AccessibilityNodeInfo?, workLabel: String): Boolean {
        val node = findSimNode(root, workLabel)
        val desc = node?.contentDescription?.toString().orEmpty().lowercase()
        return desc.contains("off") || desc.contains("disatt")
    }

    fun isWorkSimEnabled(root: AccessibilityNodeInfo?, workLabel: String): Boolean {
        val node = findSimNode(root, workLabel)
        val desc = node?.contentDescription?.toString().orEmpty().lowercase()
        return desc.contains("on") || desc.contains("attiv")
    }

    private fun findSimNode(root: AccessibilityNodeInfo?, workLabel: String): AccessibilityNodeInfo? {
        val labels = listOf(workLabel, "SIM 2", "SIM", "Usa SIM", "Schede SIM")
        return labels.firstNotNullOfOrNull { label ->
            NodeUtils.findByExactText(root, label) ?: NodeUtils.findByPartialText(root, label)
        }
    }

    companion object {
        private const val TAG = "SimUiController"
    }
}
