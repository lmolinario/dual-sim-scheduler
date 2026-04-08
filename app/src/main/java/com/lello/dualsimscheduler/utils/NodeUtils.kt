package com.lello.dualsimscheduler.utils

import android.graphics.Rect
import android.view.accessibility.AccessibilityNodeInfo

object NodeUtils {

    fun findByViewId(root: AccessibilityNodeInfo?, viewId: String): AccessibilityNodeInfo? {
        if (root == null) return null
        return root.findAccessibilityNodeInfosByViewId(viewId).firstOrNull()
    }

    fun findByExactText(root: AccessibilityNodeInfo?, text: String): AccessibilityNodeInfo? {
        if (root == null) return null
        return root.findAccessibilityNodeInfosByText(text)
            .firstOrNull { it.text?.toString()?.trim() == text }
    }

    fun findByPartialText(root: AccessibilityNodeInfo?, query: String): AccessibilityNodeInfo? {
        if (root == null) return null
        val byText = root.findAccessibilityNodeInfosByText(query).firstOrNull()
        if (byText != null) return byText
        return findDepthFirst(root) { node ->
            node.contentDescription?.toString()?.contains(query, ignoreCase = true) == true
        }
    }

    fun safeClick(node: AccessibilityNodeInfo?): Boolean {
        var cursor = node
        while (cursor != null) {
            if (cursor.isClickable && cursor.performAction(AccessibilityNodeInfo.ACTION_CLICK)) {
                return true
            }
            cursor = cursor.parent
        }
        return false
    }

    fun center(node: AccessibilityNodeInfo?): Pair<Int, Int>? {
        if (node == null) return null
        val rect = Rect()
        node.getBoundsInScreen(rect)
        if (rect.isEmpty) return null
        return rect.centerX() to rect.centerY()
    }

    private fun findDepthFirst(
        root: AccessibilityNodeInfo,
        predicate: (AccessibilityNodeInfo) -> Boolean,
    ): AccessibilityNodeInfo? {
        if (predicate(root)) return root
        for (index in 0 until root.childCount) {
            val child = root.getChild(index) ?: continue
            val result = findDepthFirst(child, predicate)
            if (result != null) return result
        }
        return null
    }
}
