package tv.afnon.core

import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.test.assertNull

class ReminderRulesTest {
    private val times = DoubleArray(6) { it * 3600000.0 }

    @Test
    fun preservesCallsAnnouncementsBellsAndExclusiveWindow() {
        for (mask in 0..63) for (adhan in listOf(false, true)) for (announce in listOf(false, true)) {
            for (i in times.indices) for (delta in doubleArrayOf(-1.0, 0.0, 119999.0, 120000.0)) {
                val due = i != 1 && delta >= 0 && delta < 120000
                val bell = mask and (1 shl i) != 0
                assertEquals(if (due && adhan && bell) i else -1,
                    ReminderRules.dueCall(times, times[i] + delta, adhan, mask, 120000.0))
                assertEquals(if (due && announce && !(adhan && bell)) i else -1,
                    ReminderRules.dueAnnounce(times, times[i] + delta, adhan, announce, mask, 120000.0))
            }
        }
        assertContentEquals(DoubleArray(6) { it * 3600000.0 }, times)
    }

    @Test
    fun overlappingWindowsKeepFirstEligiblePrayerAndExcludeSunrise() {
        val close = doubleArrayOf(0.0, 1.0, 10.0, 11.0, 12.0, 13.0)
        assertEquals(0, ReminderRules.dueCall(close, 13.0, true, 63, 20.0))
        assertEquals(2, ReminderRules.dueCall(close, 13.0, true, 12, 20.0))
        assertEquals(-1, ReminderRules.dueCall(close, 13.0, true, 2, 20.0))
    }

    @Test
    fun preservesReminderAndCeilingMinuteBoundaries() {
        for (left in doubleArrayOf(-1.0, 0.0, 1.0, 999.0, 1000.0, 299999.0, 300000.0, 300001.0)) {
            assertEquals(left > 0 && left <= 300000, ReminderRules.dueReminder(left, 0.0, 5))
        }
        assertFalse(ReminderRules.dueReminder(1.0, 0.0, 0))
        assertFalse(ReminderRules.dueReminder(1.0, 0.0, -5))
        assertTrue(ReminderRules.dueReminder(1.0, 0.0, Int.MAX_VALUE))
        for ((left, minutes) in listOf(-1.0 to 0.0, 0.0 to 0.0, 1.0 to 1.0, 60000.0 to 1.0, 60001.0 to 2.0)) {
            assertEquals(minutes, ReminderRules.minutesLeft(left, 0.0))
        }
    }

    @Test
    fun selectsFutureScheduledEventsWithStableIdsHandledExclusionAndTies() {
        val epoch = 1794355200000.0
        val days = Array(3) { day -> doubleArrayOf(1000.0, 2000.0, 4000.0, 6000.0, 8000.0, 10000.0).map { epoch + day * 86400000 + it }.toDoubleArray() }
        val before = ReminderRules.nextScheduledEvent(days, epoch - 60000, 1, 63, true, emptyArray())!!
        assertEquals("before:0:1794355201000", before.id)
        assertEquals("before", before.kind)
        assertEquals(epoch - 59000, before.time)
        assertEquals(epoch + 1000, before.prayerTime)
        assertEquals(2, ReminderRules.nextScheduledEvent(days, epoch - 60000, 1, 63, true, arrayOf(before.id))!!.prayer)
        assertEquals(2, ReminderRules.nextScheduledEvent(days, before.time, 1, 63, true, emptyArray())!!.prayer)
        assertEquals(epoch + 86401000, ReminderRules.nextScheduledEvent(days, epoch + 10000, 0, 63, true, emptyArray())!!.time)
        assertNull(ReminderRules.nextScheduledEvent(days, epoch, 15, 0, true, emptyArray()))
        assertNull(ReminderRules.nextScheduledEvent(days, epoch, 0, 63, false, emptyArray()))
        val ties = Array(3) { DoubleArray(6) { epoch + 10000 } }
        val first = ReminderRules.nextScheduledEvent(ties, epoch, 0, 63, true, emptyArray())!!
        assertEquals(0, first.prayer)
        assertEquals(2, ReminderRules.nextScheduledEvent(ties, epoch, 0, 63, true, arrayOf(first.id))!!.prayer)
        assertEquals(epoch + 1000, days[0][0], "selection must not alter raw times")
        val edge = Array(3) { DoubleArray(6) { 9007199254740991.0 } }
        assertEquals("call:0:9007199254740991", ReminderRules.nextScheduledEvent(edge, 0.0, 0, 1, true, emptyArray())!!.id)
        val past = Array(3) { DoubleArray(6) { -1000.0 } }
        assertEquals("call:0:-1000", ReminderRules.nextScheduledEvent(past, -1001.0, 0, 1, true, emptyArray())!!.id)
    }

    @Test
    fun classifiesInclusiveDeliveryWindowsAndRejectsUnavailableInputs() {
        for ((delta, state) in listOf(-1.0 to "early", 0.0 to "due", 90000.0 to "due", 90001.0 to "expired")) {
            assertEquals(state, ReminderRules.deliveryState(1000.0, 1000 + delta, 90000.0))
        }
        assertFailsWith<IllegalArgumentException> { ReminderRules.deliveryState(Double.NaN, 0.0, 90000.0) }
        assertFailsWith<IllegalArgumentException> { ReminderRules.deliveryState(0.0, Double.POSITIVE_INFINITY, 90000.0) }
        assertFailsWith<IllegalArgumentException> { ReminderRules.deliveryState(0.0, 0.0, -1.0) }
        assertFailsWith<IllegalArgumentException> { ReminderRules.nextScheduledEvent(emptyArray(), 0.0, 15, 63, true, emptyArray()) }
        val days = Array(3) { times.copyOf() }
        days[2][5] = Double.NaN
        assertFailsWith<IllegalArgumentException> { ReminderRules.nextScheduledEvent(days, 0.0, 15, 63, true, emptyArray()) }
        for (invalid in doubleArrayOf(0.5, 9007199254740992.0)) {
            days[2][5] = invalid
            assertFailsWith<IllegalArgumentException> { ReminderRules.nextScheduledEvent(days, 0.0, 15, 63, true, emptyArray()) }
        }
    }

    @Test
    fun rejectsUnavailableTimesBeforeProducingEligibility() {
        assertFailsWith<IllegalArgumentException> { ReminderRules.dueCall(DoubleArray(5), 0.0, true, 63, 120000.0) }
        for (invalid in doubleArrayOf(Double.NaN, Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY)) {
            assertFailsWith<IllegalArgumentException> { ReminderRules.dueCall(times, invalid, true, 63, 120000.0) }
            assertFailsWith<IllegalArgumentException> { ReminderRules.dueCall(times, 0.0, true, 63, invalid) }
            assertFailsWith<IllegalArgumentException> { ReminderRules.dueReminder(invalid, 0.0, 15) }
            assertFailsWith<IllegalArgumentException> { ReminderRules.minutesLeft(1.0, invalid) }
            for (i in times.indices) assertFailsWith<IllegalArgumentException> {
                ReminderRules.dueCall(times.copyOf().also { it[i] = invalid }, 0.0, true, 63, 120000.0)
            }
        }
    }
}
