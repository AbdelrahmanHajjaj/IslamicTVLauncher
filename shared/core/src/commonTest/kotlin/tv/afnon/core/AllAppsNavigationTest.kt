package tv.afnon.core

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class AllAppsNavigationTest {
    @Test
    fun headerAndGridBoundariesFollowTheOwnerContract() {
        val n = AllAppsNavigation
        for (column in 0..4) assertEquals(n.BACK, n.move(column, 16, 0, 0, -1))
        for ((from, end, start) in listOf(Triple(n.BACK, n.PRAYER, n.SETTINGS), Triple(n.PRAYER, n.SETTINGS, n.BACK), Triple(n.SETTINGS, n.BACK, n.PRAYER))) {
            assertEquals(end, n.move(from, 16, 0, 1, 0))
            assertEquals(start, n.move(from, 16, 0, -1, 0))
            assertEquals(from, n.move(from, 16, 0, 0, -1))
            assertEquals(from, n.move(from, 0, 0, 0, 1))
        }
        assertEquals(20, n.move(n.BACK, 41, 20, 0, 1))
        assertEquals(24, n.move(n.PRAYER, 41, 20, 0, 1))
        assertEquals(24, n.move(n.SETTINGS, 41, 20, 0, 1))
        assertEquals(2, n.move(n.SETTINGS, 3, 0, 0, 1))
        assertEquals(15, n.move(14, 16, 0, 0, 1))
        assertEquals(15, n.move(15, 16, 0, 0, 1))
        assertEquals(10, n.move(15, 16, 0, 0, -1))
        assertEquals(15, n.move(15, 16, 0, -1, 0))
        assertEquals(4, n.move(4, 16, 0, 1, 0))
        assertEquals(0, n.move(n.NONE, 16, 0, 1, 0))
        assertEquals(n.BACK, n.move(n.NONE, 0, 0, 1, 0))
        assertEquals(Int.MAX_VALUE - 1, n.move(n.SETTINGS, Int.MAX_VALUE, Int.MAX_VALUE - 2, 0, 1))
    }

    @Test
    fun everyTransitionTargetsAPresentControlAndHorizontalGridMovesStayInTheirRow() {
        val n = AllAppsNavigation
        for (apps in 0..32) {
            val present = (0 until apps).toSet() + setOf(n.BACK, n.PRAYER, n.SETTINGS)
            for (from in present) for ((dx, dy) in listOf(-1 to 0, 1 to 0, 0 to -1, 0 to 1)) {
                val target = n.move(from, apps, 0, dx, dy)
                assertTrue(target in present, "Absent target $target for $from/$apps/$dx/$dy")
                if (from >= 0 && dx != 0) assertEquals(from / 5, target / 5)
            }
        }
    }

    @Test
    fun invalidRowsAndDirectionsAreRejected() {
        val n = AllAppsNavigation
        for ((apps, first) in listOf(-1 to 0, 3 to -5, 3 to 1, 3 to 5, 0 to 5)) {
            assertFailsWith<IllegalArgumentException> { n.move(n.BACK, apps, first, 0, 1) }
        }
        assertFailsWith<IllegalArgumentException> { n.move(0, 3, 0, 1, 1) }
        assertFailsWith<IllegalArgumentException> { n.move(0, 3, 0, 0, 0) }
    }
}
