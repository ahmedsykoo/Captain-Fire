package com.fairkm.driver.service

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import com.fairkm.driver.data.FareBus
import com.fairkm.driver.data.FareOffer
import java.util.regex.Pattern

/**
 * Watches inDrive's window content and extracts the offered price (EGP)
 * and distance (km) whenever a ride-request screen is shown, then
 * publishes it on FareBus for the OverlayService to render.
 *
 * inDrive doesn't expose stable view IDs across builds, so we walk the
 * node tree and pattern-match on text content instead — the same
 * approach used successfully in the earlier NetFare build.
 */
class FareAccessibilityService : AccessibilityService() {

    companion object {
        private const val TARGET_PACKAGE = "sinet.startup.inDriver"

        // Matches "402 EGP" / "402 ج" style price strings
        private val PRICE_PATTERN: Pattern = Pattern.compile("([0-9]+(?:[.,][0-9]+)?)\\s*(EGP|ج|جنيه)")

        // Matches "2,2 كم" / "67.0 كم" style distance strings
        private val DISTANCE_PATTERN: Pattern = Pattern.compile("([0-9]+(?:[.,][0-9]+)?)\\s*كم")
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        startForeground()
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent) {
        if (event.packageName?.toString() != TARGET_PACKAGE) return

        val root = rootInActiveWindow ?: return
        try {
            val offer = extractOffer(root)
            FareBus.publish(offer)
            if (offer != null) {
                ensureOverlayRunning()
            }
        } finally {
            root.recycle()
        }
    }

    override fun onInterrupt() {
        FareBus.publish(null)
    }

    private fun extractOffer(root: AccessibilityNodeInfo): FareOffer? {
        val texts = mutableListOf<String>()
        collectText(root, texts)

        var price: Double? = null
        var distance: Double? = null

        for (t in texts) {
            if (price == null) {
                val m = PRICE_PATTERN.matcher(t)
                if (m.find()) {
                    price = m.group(1)?.replace(",", ".")?.toDoubleOrNull()
                }
            }
            if (distance == null) {
                val m = DISTANCE_PATTERN.matcher(t)
                if (m.find()) {
                    distance = m.group(1)?.replace(",", ".")?.toDoubleOrNull()
                }
            }
            if (price != null && distance != null) break
        }

        return if (price != null && distance != null && distance > 0) {
            FareOffer(priceEgp = price, distanceKm = distance)
        } else null
    }

    private fun collectText(node: AccessibilityNodeInfo?, out: MutableList<String>, depth: Int = 0) {
        if (node == null || depth > 40) return
        node.text?.toString()?.takeIf { it.isNotBlank() }?.let { out.add(it) }
        for (i in 0 until node.childCount) {
            collectText(node.getChild(i), out, depth + 1)
        }
    }

    private fun ensureOverlayRunning() {
        val intent = Intent(this, OverlayService::class.java)
        try {
            startService(intent)
        } catch (_: Exception) {
            // overlay permission not granted yet — MainActivity guides the user to grant it
        }
    }

    private fun startForeground() {
        // Accessibility services run persistently once enabled by the user
        // in system settings; no separate foreground notification needed here.
    }
}
