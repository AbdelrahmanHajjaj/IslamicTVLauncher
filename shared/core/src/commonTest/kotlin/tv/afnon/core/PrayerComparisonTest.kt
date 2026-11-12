package tv.afnon.core

import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class PrayerComparisonTest {
    @Test fun namedAuthoritiesMapWithoutFallback() {
        val names = UiContract.choices("prayer.method").filter { it != "city-default" }
        val expected = intArrayOf(5, 4, 3, 1, 16, 9, 10, 2, 15, 11, 12, 13, 14, 17, 20, 18, 19, 21, 8)
        assertEquals(expected.size, names.size)
        names.forEachIndexed { i, name -> assertEquals(expected[i], PrayerComparison.aladhanMethod(name)) }
        for (name in listOf("", "city-default", "egyptian", "UNKNOWN", "EGYPTIAN ")) {
            assertFailsWith<IllegalArgumentException> { PrayerComparison.aladhanMethod(name) }
        }
    }

    @Test fun comparesWholeEpochMinutesWithoutChangingSchedules() {
        val base = 1794434400000.0
        val calculated = doubleArrayOf(base + 30000, base + 60000, base + 120000, base + 180000, base + 240000, base + 300000)
        val reference = doubleArrayOf(base + 59999, base, base + 180000, base + 179999, base + 300000, base + 300000)
        val before = calculated.copyOf()
        val onlineBefore = reference.copyOf()
        assertContentEquals(intArrayOf(0, -1, 1, -1, 1, 0), PrayerComparison.minutes(calculated, reference))
        assertContentEquals(before, calculated)
        assertContentEquals(onlineBefore, reference)
    }

    @Test fun includesDateRolloverAndPreEpochFlooring() {
        assertContentEquals(IntArray(6) { 2 }, PrayerComparison.minutes(DoubleArray(6) { 86340000.0 }, DoubleArray(6) { 86460000.0 }))
        assertContentEquals(IntArray(6) { 1 }, PrayerComparison.minutes(DoubleArray(6) { -1.0 }, DoubleArray(6)))
    }

    @Test fun rejectsMissingUnavailableAndUnsafeValues() {
        val valid = DoubleArray(6) { 1794434400000.0 }
        for (invalid in listOf(DoubleArray(5), DoubleArray(7), DoubleArray(6) { Double.NaN },
            DoubleArray(6) { Double.POSITIVE_INFINITY }, DoubleArray(6) { Double.NEGATIVE_INFINITY },
            DoubleArray(6) { 8640000000000001.0 })) {
            assertFailsWith<IllegalArgumentException> { PrayerComparison.minutes(valid, invalid) }
            assertFailsWith<IllegalArgumentException> { PrayerComparison.minutes(invalid, valid) }
        }
        assertFailsWith<IllegalArgumentException> {
            PrayerComparison.minutes(DoubleArray(6) { -8640000000000000.0 }, DoubleArray(6) { 8640000000000000.0 })
        }
    }
}
