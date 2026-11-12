@file:OptIn(kotlin.js.ExperimentalJsExport::class)

package tv.afnon.core

import kotlin.js.JsExport
import kotlin.math.floor

@JsExport
class PrayerSelection(val current: Int, val next: Int, val nextTime: Double)

@JsExport
object PrayerRules {
    const val MAX_OFFSET_MINUTES = 30
    const val MAX_HIJRI_OFFSET_DAYS = 2

    fun floorToMinute(epochMilliseconds: Double): Double {
        require(epochMilliseconds.isFinite()) { "Invalid prayer time" }
        return floor(epochMilliseconds / 60000.0) * 60000.0
    }

    // Exact numeric matches keep untrusted JS fractions/large indexes from becoming prayers.
    fun isPrayer(index: Double): Boolean = when (index) {
        0.0, 2.0, 3.0, 4.0, 5.0 -> true
        else -> false
    }

    // Double keeps untrusted JS numbers intact until after validation and clamping.
    fun normalizeOffset(minutes: Double): Int {
        if (!minutes.isFinite() || minutes % 1.0 != 0.0) return 0
        return minutes.coerceIn(-MAX_OFFSET_MINUTES.toDouble(), MAX_OFFSET_MINUTES.toDouble()).toInt()
    }

    fun validateOffsets(offsets: IntArray) {
        require(offsets.size == 6) { "Expected six prayer offsets" }
        for (offset in offsets) {
            require(offset in -MAX_OFFSET_MINUTES..MAX_OFFSET_MINUTES) { "Prayer offset out of range" }
        }
    }

    // Epoch milliseconds stay numeric on JVM and ES5 without a Long/BigInt bridge.
    fun select(times: DoubleArray, tomorrowFajr: Double, now: Double): PrayerSelection {
        validateTimes(times)
        require(now.isFinite() && tomorrowFajr.isFinite()) { "Invalid prayer time" }

        var current = -1
        var next = -1
        for (i in times.indices) {
            if (times[i] <= now) current = i
            if (next == -1 && isPrayer(i.toDouble()) && times[i] > now) next = i
        }
        return PrayerSelection(
            if (isPrayer(current.toDouble())) current else -1,
            if (next == -1) 0 else next,
            if (next == -1) tomorrowFajr else times[next],
        )
    }

    internal fun validateTimes(times: DoubleArray) {
        require(times.size == 6) { "Expected six prayer times" }
        for (time in times) require(time.isFinite()) { "Invalid prayer time" }
    }
}
