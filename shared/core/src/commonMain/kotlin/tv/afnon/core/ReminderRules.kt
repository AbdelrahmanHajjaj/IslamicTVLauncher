@file:OptIn(kotlin.js.ExperimentalJsExport::class)

package tv.afnon.core

import kotlin.js.JsExport
import kotlin.math.floor

@JsExport
class ReminderEvent(val id: String, val kind: String, val prayer: Int, val time: Double, val prayerTime: Double)

@JsExport
/** Bell bits follow prayer indexes; Sunrise never alerts. Call windows exclude their endpoint. */
object ReminderRules {
    private const val MAX_SAFE_EPOCH = 9007199254740991.0

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

    /** Three raw days in adapter order; equal-time candidates keep that order. */
    fun nextScheduledEvent(days: Array<DoubleArray>, now: Double, minutes: Int, bellMask: Int,
        callEnabled: Boolean, handled: Array<String>): ReminderEvent? {
        require(days.size == 3 && now.isFinite() && minutes >= 0 && bellMask in 0..63) { "Invalid event inputs" }
        for (times in days) {
            PrayerRules.validateTimes(times)
            for (time in times) {
                require(time % 1.0 == 0.0 && time >= -MAX_SAFE_EPOCH && time <= MAX_SAFE_EPOCH) { "Invalid event epoch" }
            }
        }
        var best: ReminderEvent? = null
        fun add(kind: String, prayer: Int, time: Double, prayerTime: Double) {
            val previous = best
            if (time <= now || previous != null && time >= previous.time) return
            // Integer text preserves the installed LG's persisted IDs on JVM and JS.
            val id = "$kind:$prayer:${prayerTime.toLong()}"
            if (id !in handled) best = ReminderEvent(id, kind, prayer, time, prayerTime)
        }
        for (times in days) for (i in times.indices) {
            if (i == 1 || bellMask and (1 shl i) == 0) continue
            if (minutes > 0) add("before", i, times[i] - minutes * 60000.0, times[i])
            if (callEnabled) add("call", i, times[i], times[i])
        }
        return best
    }

    /** Delivery includes both endpoints; the caller owns the clock and persisted claim. */
    fun deliveryState(time: Double, now: Double, maxAgeMs: Double): String {
        require(time.isFinite() && now.isFinite() && maxAgeMs.isFinite() && maxAgeMs >= 0) { "Invalid delivery window" }
        return if (now < time) "early" else if (now > time + maxAgeMs) "expired" else "due"
    }

    private fun secondsUntil(nextTime: Double, now: Double): Double {
        require(nextTime.isFinite() && now.isFinite()) { "Invalid reminder time" }
        return maxOf(0.0, floor((nextTime - now + 999) / 1000))
    }
}
