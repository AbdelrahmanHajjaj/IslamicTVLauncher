package tv.afnon.core

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotEquals

class UiContractTest {
    @Test fun choiceValuesAreSemanticAndCoverageAccountsForExplicitGaps() {
        assertEquals(listOf("mihrab", "compact", "hidden"), UiContract.choices("appearance.countdown").toList())
        assertEquals(listOf("two_rows", "classic"), UiContract.choices("appearance.favourites").toList())
        UiContract.validateChoices("appearance.position", arrayOf("top", "bottom"))
        UiContract.validateChoiceCoverage("display.language", arrayOf("ar", "en"), arrayOf("system"))
        assertFailsWith<IllegalArgumentException> { UiContract.validateChoiceCoverage("display.language", arrayOf("ar", "en"), emptyArray()) }
        assertFailsWith<IllegalArgumentException> { UiContract.validateChoiceCoverage("display.language", arrayOf("ar", "en"), arrayOf("en", "system")) }
        assertFailsWith<IllegalArgumentException> { UiContract.validateChoices("display.language", arrayOf("ar", "ar")) }
        assertFailsWith<IllegalArgumentException> { UiContract.validateChoices("display.language", arrayOf("unknown")) }
        assertFailsWith<IllegalArgumentException> { UiContract.choices("appearance.lighting") }
        val copy = UiContract.choices("display.language")
        copy[0] = "changed"
        assertEquals("system", UiContract.choices("display.language")[0])
    }

    @Test fun numberRangesUseExistingDomainLimits() {
        for (name in arrayOf("fajr", "sunrise", "dhuhr", "asr", "maghrib", "isha")) {
            assertEquals(-PrayerRules.MAX_OFFSET_MINUTES, UiContract.minimum("offsets.$name"))
            assertEquals(PrayerRules.MAX_OFFSET_MINUTES, UiContract.maximum("offsets.$name"))
        }
        assertEquals(-PrayerRules.MAX_HIJRI_OFFSET_DAYS, UiContract.minimum("prayer.hijri"))
        assertEquals(PrayerRules.MAX_HIJRI_OFFSET_DAYS, UiContract.maximum("prayer.hijri"))
        for (id in arrayOf("reminders.volume", "appearance.lighting")) {
            assertEquals(0, UiContract.minimum(id))
            assertEquals(100, UiContract.maximum(id))
        }
        assertFailsWith<IllegalArgumentException> { UiContract.maximum("appearance.theme") }
    }

    @Test fun bindingsUseSemanticTypesAndSeparateStorageConcepts() {
        assertEquals("choice", UiContract.bind("appearance.favourites", "choice").kind)
        assertNotEquals(UiContract.control("appearance.favourites").id, UiContract.control("appearance.countdown").id)
        assertEquals("appearance", UiContract.control("appearance.countdown").save)
        assertEquals("immediate", UiContract.control("display.language").save)
        assertFailsWith<IllegalArgumentException> { UiContract.bind("appearance.favourites", "toggle") }
        assertFailsWith<IllegalArgumentException> { UiContract.control("unknown.control") }
    }

    @Test fun actualBindingsAndDeclaredGapsMustCoverTheWholeCatalogExactlyOnce() {
        val ids = UiContract.controls().map { it.id }.toTypedArray()
        UiContract.validateCoverage(ids, emptyArray())
        UiContract.validateCoverage(ids.take(4).toTypedArray(), ids.drop(4).toTypedArray())
        assertFailsWith<IllegalArgumentException> { UiContract.validateCoverage(ids.drop(1).toTypedArray(), emptyArray()) }
        assertFailsWith<IllegalArgumentException> { UiContract.validateCoverage(ids, arrayOf(ids[0])) }
        assertFailsWith<IllegalArgumentException> { UiContract.validateCoverage(ids, arrayOf("unknown.control")) }
    }
}
