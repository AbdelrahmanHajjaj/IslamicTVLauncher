package tv.afnon.core

import kotlin.test.Test
import kotlin.test.assertEquals

class UiScaleTest {
    @Test fun offeredSizesAndPreferencePolicyMatch() {
        assertEquals(100, UiScale.DEFAULT_PERCENT)
        assertEquals(listOf("80", "90", "100", "110"), UiContract.choices("display.size").toList())
        for (value in UiContract.choices("display.size")) {
            assertEquals(value.toInt(), UiScale.percent(value.toDouble()))
        }
    }

    @Test fun invalidSavedValuesKeepTheOriginalSize() {
        for (value in listOf(-1.0, 0.0, 79.0, 85.0, 99.9, 111.0, 1000.0, Double.NaN, Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY)) {
            assertEquals(100, UiScale.percent(value))
        }
    }
}
