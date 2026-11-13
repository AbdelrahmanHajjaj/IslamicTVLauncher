package tv.afnon.core

import kotlin.test.Test
import kotlin.test.assertEquals

class UiScaleTest {
    @Test fun offeredSizesAndPreferencePolicyMatch() {
        assertEquals(100, UiScale.DEFAULT_PERCENT)
        assertEquals(80, UiContract.minimum("display.size"))
        assertEquals(150, UiContract.maximum("display.size"))
        assertEquals(10, UiScale.STEP_PERCENT)
        for (value in 80..150 step 10) {
            assertEquals(value, UiScale.percent(value.toDouble()))
        }
    }

    @Test fun invalidSavedValuesKeepTheOriginalSize() {
        for (value in listOf(-1.0, 0.0, 79.0, 85.0, 99.9, 111.0, 149.9, 151.0, 160.0, 1000.0, Double.NaN, Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY)) {
            assertEquals(100, UiScale.percent(value))
        }
    }
}
