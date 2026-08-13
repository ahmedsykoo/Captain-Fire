package com.fairkm.driver.service

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import com.fairkm.driver.data.FareBus
import com.fairkm.driver.data.FareOffer
import java.util.regex.Pattern

class FareAccessibilityService : AccessibilityService() {

    companion object {
        private const val TARGET_PACKAGE = "sinet.startup.inDriver"

        private val PRICE_PATTERN = Pattern.compile(
            "([0-9]+(?:[.,][0-9]+)?)\\s*(EGP|ج|جنيه)"
        )

        private val DISTANCE_PATTERN = Pattern.compile(
            "([0-9]+(?:[.,][0-9]+)?)\\s*كم"
        )
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
    }

    override fun onAccessibilityEvent(
        event: AccessibilityEvent
    ) {
        if (event.packageName?.toString() != TARGET_PACKAGE) {
            return
        }

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

    private fun extractOffer(
        root: AccessibilityNodeInfo
    ): FareOffer? {

        val texts = mutableListOf<String>()

        collectText(
            node = root,
            out = texts
        )

        var price: Double? = null
        var distance: Double? = null

        for (text in texts) {

            if (price == null) {
                val match = PRICE_PATTERN.matcher(text)

                if (match.find()) {
                    price = match
                        .group(1)
                        ?.replace(",", ".")
                        ?.toDoubleOrNull()
                }
            }

            if (distance == null) {
                val match = DISTANCE_PATTERN.matcher(text)

                if (match.find()) {
                    distance = match
                        .group(1)
                        ?.replace(",", ".")
                        ?.toDoubleOrNull()
                }
            }

            if (price != null && distance != null) {
                break
            }
        }

        return if (
            price != null &&
            distance != null &&
            distance > 0
        ) {
            FareOffer(
                priceEgp = price,
                distanceKm = distance
            )
        } else {
            null
        }
    }

    private fun collectText(
        node: AccessibilityNodeInfo?,
        out: MutableList<String>,
        depth: Int = 0
    ) {
        if (node == null || depth > 40) {
            return
        }

        node.text
            ?.toString()
            ?.takeIf { it.isNotBlank() }
            ?.let { out.add(it) }

        for (i in 0 until node.childCount) {
            collectText(
                node.getChild(i),
                out,
                depth + 1
            )
        }
    }

    private fun ensureOverlayRunning() {
        val intent = Intent(
            this,
            OverlayService::class.java
        )

        try {
            startService(intent)
        } catch (_: Exception) {
            // سيتم التعامل مع صلاحية Overlay من الشاشة الرئيسية
        }
    }
}
