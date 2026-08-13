package com.fairkm.driver.data

data class FareOffer(
    val priceEgp: Double,
    val distanceKm: Double
) {
    val pricePerKm: Double
        get() = if (distanceKm > 0) priceEgp / distanceKm else 0.0

    fun netPricePerKm(companyCommissionPercent: Double): Double {
        val net = priceEgp * (1 - companyCommissionPercent / 100.0)
        return if (distanceKm > 0) net / distanceKm else 0.0
    }

    fun netTotal(companyCommissionPercent: Double): Double =
        priceEgp * (1 - companyCommissionPercent / 100.0)
}

object FareBus {
    private val listeners = mutableListOf<(FareOffer?) -> Unit>()

    fun subscribe(listener: (FareOffer?) -> Unit) {
        listeners.add(listener)
    }

    fun unsubscribe(listener: (FareOffer?) -> Unit) {
        listeners.remove(listener)
    }

    fun publish(offer: FareOffer?) {
        listeners.toList().forEach { it(offer) }
    }
}
