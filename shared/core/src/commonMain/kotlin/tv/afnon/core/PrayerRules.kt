@file:OptIn(kotlin.js.ExperimentalJsExport::class)

package tv.afnon.core

import kotlin.js.JsExport

@JsExport
class PrayerSelection(val current: Int, val next: Int, val nextTime: Double)

@JsExport
object PrayerRules {
    const val MAX_OFFSET_MINUTES = 30

    fun validateOffsets(offsets: IntArray) {
        require(offsets.size == 6) { "Expected six prayer offsets" }
        for (offset in offsets) {
            require(offset in -MAX_OFFSET_MINUTES..MAX_OFFSET_MINUTES) { "Prayer offset out of range" }
        }
    }

    // Epoch milliseconds stay numeric on JVM and ES5 without a Long/BigInt bridge.
    fun select(times: DoubleArray, tomorrowFajr: Double, now: Double): PrayerSelection {
        require(times.size == 6) { "Expected six prayer times" }
        require(now.isFinite() && tomorrowFajr.isFinite()) { "Invalid prayer time" }
        for (time in times) require(time.isFinite()) { "Invalid prayer time" }

        var current = -1
        var next = -1
        for (i in times.indices) {
            if (times[i] <= now) current = i
            if (next == -1 && i != 1 && times[i] > now) next = i
        }
        return PrayerSelection(
            if (current == 1) -1 else current,
            if (next == -1) 0 else next,
            if (next == -1) tomorrowFajr else times[next],
        )
    }
}
