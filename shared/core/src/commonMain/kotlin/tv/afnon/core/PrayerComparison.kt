@file:OptIn(kotlin.js.ExperimentalJsExport::class)

package tv.afnon.core

import kotlin.js.JsExport
import kotlin.math.abs

/** Advisory differences only; adapters retain the date, source, settings and schedule. */
@JsExport
object PrayerComparison {
    fun aladhanMethod(method: String): Int = ReferenceMethods.aladhan(method)

    /** Positive means the online reference is later than the calculated time. */
    fun minutes(calculated: DoubleArray, reference: DoubleArray): IntArray {
        PrayerRules.validateTimes(calculated)
        PrayerRules.validateTimes(reference)
        return IntArray(6) { i ->
            require(abs(calculated[i]) <= 8640000000000000.0 && abs(reference[i]) <= 8640000000000000.0) {
                "Invalid comparison epoch"
            }
            val difference = (PrayerRules.floorToMinute(reference[i]) - PrayerRules.floorToMinute(calculated[i])) / 60000.0
            require(difference >= Int.MIN_VALUE && difference <= Int.MAX_VALUE) { "Comparison out of range" }
            difference.toInt()
        }
    }
}
