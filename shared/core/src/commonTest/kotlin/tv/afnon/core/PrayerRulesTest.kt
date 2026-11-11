package tv.afnon.core

import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class PrayerRulesTest {
    private val times = doubleArrayOf(1000.0, 2000.0, 4000.0, 6000.0, 8000.0, 10000.0)

    @Test
    fun selectsAcrossPrayerAndSunriseBoundaries() {
        val moments = doubleArrayOf(999.0, 1000.0, 1999.0, 2000.0, 3999.0, 4000.0, 9999.0, 10000.0, 10001.0)
        val current = intArrayOf(-1, 0, 0, -1, -1, 2, 4, 5, 5)
        val next = intArrayOf(0, 2, 2, 2, 2, 3, 5, 0, 0)
        for (i in moments.indices) {
            val result = PrayerRules.select(times, 12000.0, moments[i])
            assertEquals(current[i], result.current, "current at ${moments[i]}")
            assertEquals(next[i], result.next, "next at ${moments[i]}")
            assertEquals(if (moments[i] >= 10000.0) 12000.0 else times[next[i]], result.nextTime)
        }
        assertContentEquals(doubleArrayOf(1000.0, 2000.0, 4000.0, 6000.0, 8000.0, 10000.0), times)
    }

    @Test
    fun preservesEpochMillisecondsAcrossHosts() {
        val epoch = 1794355200000.0
        val result = PrayerRules.select(DoubleArray(6) { epoch + times[it] }, epoch + 12000, epoch + 10000)
        assertEquals(epoch + 12000, result.nextTime)
    }

    @Test
    fun validatesAllSixOffsetsWithoutChangingThem() {
        val offsets = intArrayOf(-30, 30, 0, -1, 1, 0)
        PrayerRules.validateOffsets(offsets)
        assertContentEquals(intArrayOf(-30, 30, 0, -1, 1, 0), offsets)
        for (invalid in listOf(intArrayOf(), IntArray(5), intArrayOf(31, 0, 0, 0, 0, 0), intArrayOf(Int.MIN_VALUE, 0, 0, 0, 0, 0))) {
            assertFailsWith<IllegalArgumentException> { PrayerRules.validateOffsets(invalid) }
        }
    }

    @Test
    fun rejectsUnavailableTimesBeforeSelectingAnAlert() {
        assertFailsWith<IllegalArgumentException> { PrayerRules.select(DoubleArray(5), 12000.0, 0.0) }
        assertFailsWith<IllegalArgumentException> { PrayerRules.select(times, Double.NaN, 0.0) }
        assertFailsWith<IllegalArgumentException> { PrayerRules.select(times, 12000.0, Double.POSITIVE_INFINITY) }
        assertFailsWith<IllegalArgumentException> { PrayerRules.select(times.copyOf().also { it[3] = Double.NaN }, 12000.0, 0.0) }
    }
}
