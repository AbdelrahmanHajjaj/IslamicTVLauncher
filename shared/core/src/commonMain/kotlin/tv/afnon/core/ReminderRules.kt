@file:OptIn(kotlin.js.ExperimentalJsExport::class)

package tv.afnon.core

import kotlin.js.JsExport
import kotlin.math.floor

@JsExport
/** Bell bits follow prayer indexes; Sunrise never alerts. Call windows exclude their endpoint. */
object ReminderRules {
    fun dueCall(times: DoubleArray, now: Double, adhan: Boolean, bellMask: Int, windowMs: Double): Int =
        if (adhan) duePrayer(times, now, bellMask, windowMs) else -1

    fun dueAnnounce(times: DoubleArray, now: Double, adhan: Boolean, announce: Boolean, bellMask: Int, windowMs: Double): Int =
        if (announce) duePrayer(times, now, if (adhan) bellMask.inv() else 63, windowMs) else -1

    private fun duePrayer(times: DoubleArray, now: Double, eligibleMask: Int, windowMs: Double): Int {
        PrayerRules.validateTimes(times)
        require(now.isFinite() && windowMs.isFinite() && windowMs > 0) { "Invalid reminder window" }
        for (i in times.indices) {
            val since = now - times[i]
            if (i != 1 && since >= 0 && since < windowMs && eligibleMask and (1 shl i) != 0) return i
        }
        return -1
    }

    fun dueReminder(nextTime: Double, now: Double, minutes: Int): Boolean {
        if (minutes <= 0) return false
        val left = secondsUntil(nextTime, now)
        return left > 0 && left <= minutes * 60.0
    }

    fun minutesLeft(nextTime: Double, now: Double): Double = floor((secondsUntil(nextTime, now) + 59) / 60)

    private fun secondsUntil(nextTime: Double, now: Double): Double {
        require(nextTime.isFinite() && now.isFinite()) { "Invalid reminder time" }
        return maxOf(0.0, floor((nextTime - now + 999) / 1000))
    }
}
