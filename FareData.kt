package com.fairkm.driver.data

/** A single parsed ride offer read off the inDrive screen. */
data class FareOffer(
    val priceEgp: Double,
    val distanceKm: Double
) {
    val pricePerKm: Double
        get() = if (distanceKm > 0) priceEgp / distanceKm else 0.0

    /** Net price/km after the platform's own commission is deducted. */
    fun netPricePerKm(companyCommissionPercent: Double): Double {
        val net = priceEgp * (1 - companyCommissionPercent / 100.0)
        return if (distanceKm > 0) net / distanceKm else 0.0
    }

    fun netTotal(companyCommissionPercent: Double): Double =
        priceEgp * (1 - companyCommissionPercent / 100.0)
}

/** Simple in-process pub/sub so the Accessibility Service can push
 *  parsed offers to the Overlay Service without binding. */
object FareBus {
    private val listeners = mutableListOf<(FareOffer?) -> Unit>()

    fun subscribe(listener: (FareOffer?) -> Unit) {
        listeners.add(listener)
    }

    fun unsubscribe(listener: (FareOffer?) -> Unit) {
        listeners.remove(listener)
    }

    fun publish(offer: FareOffer?) {
        listeners.forEach { it(offer) }
    }
}
