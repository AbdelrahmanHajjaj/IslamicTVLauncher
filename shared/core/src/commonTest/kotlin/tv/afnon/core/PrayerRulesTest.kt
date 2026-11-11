package tv.afnon.core

import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class PrayerRulesTest {
    @Test fun onlyFiveExactIndexesArePrayers() {
        for (index in listOf(0.0, 2.0, 3.0, 4.0, 5.0)) assertEquals(true, PrayerRules.isPrayer(index))
        for (index in listOf(-1.0, 1.0, 6.0, 0.5, 4294967297.0, Double.NaN, Double.POSITIVE_INFINITY)) {
            assertEquals(false, PrayerRules.isPrayer(index))
        }
    }

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
    fun normalizesSavedOffsetsWithoutIntegerOverflowOrFractionTruncation() {
        val inputs = doubleArrayOf(-31.0, -30.0, -1.0, 0.0, 30.0, 31.0,
            Int.MIN_VALUE.toDouble(), Int.MAX_VALUE.toDouble(), 4294967297.0,
            Double.MAX_VALUE, Double.NaN, Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY, 1.5)
        val expected = intArrayOf(-30, -30, -1, 0, 30, 30, -30, 30, 30, 30, 0, 0, 0, 0)
        for (i in inputs.indices) assertEquals(expected[i], PrayerRules.normalizeOffset(inputs[i]))
        assertEquals(2, PrayerRules.MAX_HIJRI_OFFSET_DAYS)
    }

    @Test
    fun rejectsUnavailableTimesBeforeSelectingAnAlert() {
        assertFailsWith<IllegalArgumentException> { PrayerRules.select(DoubleArray(5), 12000.0, 0.0) }
        assertFailsWith<IllegalArgumentException> { PrayerRules.select(times, Double.NaN, 0.0) }
        assertFailsWith<IllegalArgumentException> { PrayerRules.select(times, 12000.0, Double.POSITIVE_INFINITY) }
        for (invalid in doubleArrayOf(Double.NaN, Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY)) {
            assertFailsWith<IllegalArgumentException> { PrayerRules.select(times, invalid, 0.0) }
            for (i in times.indices) {
                assertFailsWith<IllegalArgumentException> { PrayerRules.select(times.copyOf().also { it[i] = invalid }, 12000.0, 0.0) }
            }
        }
    }
}
