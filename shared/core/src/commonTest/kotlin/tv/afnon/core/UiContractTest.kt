package tv.afnon.core

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotEquals

class UiContractTest {
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
