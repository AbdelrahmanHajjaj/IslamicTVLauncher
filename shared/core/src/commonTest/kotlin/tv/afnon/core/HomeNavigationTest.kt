package tv.afnon.core

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class HomeNavigationTest {
    @Test
    fun homeBoundariesMatchTheAndroidContract() {
        val n = HomeNavigation
        assertEquals(n.FIRST_APP, n.initial(3, 0))
        assertEquals(n.FIRST_RECENT, n.initial(0, 2))
        assertEquals(n.ALL_APPS, n.initial(0, 0))
        for ((slot, top) in listOf(0 to n.RADIO, 1 to n.NETWORK, 2 to n.SETTINGS)) {
            assertEquals(top, n.move(n.FIRST_APP + slot, 3, 0, 3, n.FIRST_APP, 0, -1, true))
            assertEquals(n.ALL_APPS, n.move(n.FIRST_APP + slot, 3, 0, 3, n.FIRST_APP, 0, 1, true))
            assertEquals(n.FIRST_APP + slot, n.move(n.ALL_APPS, 3, 0, 3, n.FIRST_APP + slot, 0, -1, true))
        }
        assertEquals(n.FIRST_APP + 5, n.move(n.FIRST_APP + 3, 6, 0, 4, n.FIRST_APP, 0, 1, false))
        assertEquals(n.FIRST_APP, n.move(-1, 3, 0, 3, n.FIRST_APP, 1, 0, true))
        assertEquals(n.TODAY, n.move(n.ALL_APPS, 0, 0, 1, n.FIRST_APP, 0, -1, false))
        assertEquals(n.FIRST_APP, n.move(n.SETTINGS, 1, 0, 1, n.FIRST_APP, 0, 1, false))
        assertFailsWith<IllegalArgumentException> { n.move(n.FIRST_APP, 10, 0, 3, n.FIRST_APP, 0, 1, false) }
        assertFailsWith<IllegalArgumentException> { n.move(n.FIRST_APP, 3, 0, 0, n.FIRST_APP, 0, 1, false) }
        assertFailsWith<IllegalArgumentException> { n.move(n.FIRST_APP, 3, 0, 3, n.FIRST_APP, 1, 1, false) }
    }

    @Test
    fun optionalWatchActionDoesNotStealTheDownwardAllAppsTarget() {
        val n = HomeNavigation
        assertEquals(n.WATCH_INPUT, n.move(n.ALL_APPS, 3, 0, 3, n.FIRST_APP, -1, 0, true))
        assertEquals(n.ALL_APPS, n.move(n.WATCH_INPUT, 3, 0, 3, n.FIRST_APP, 1, 0, true))
        assertEquals(n.ALL_APPS, n.move(n.WATCH_INPUT, 3, 0, 3, n.FIRST_APP, 0, 1, true))
        assertEquals(n.ALL_APPS, n.move(n.ALL_APPS, 3, 0, 3, n.FIRST_APP, -1, 0, false))
    }

    @Test
    fun allAndOnlyPresentControlsAreReachableAcrossEmptyAndPartialRows() {
        val n = HomeNavigation
        for (apps in 0..n.MAX_APPS) for (recent in 0..n.MAX_RECENT)
            for (columns in 1..maxOf(1, apps)) for (watch in listOf(false, true)) {
                val present = mutableSetOf(n.TODAY, n.INPUTS, n.RADIO, n.NETWORK, n.SETTINGS, n.ALL_APPS)
                repeat(apps) { present.add(n.FIRST_APP + it) }
                repeat(recent) { present.add(n.FIRST_RECENT + it) }
                if (watch) present.add(n.WATCH_INPUT)
                val reached = mutableSetOf<Int>()
                val queue = mutableListOf(n.initial(apps, recent))
                while (queue.isNotEmpty()) {
                    val from = queue.removeAt(0)
                    if (!reached.add(from)) continue
                    for ((dx, dy) in listOf(-1 to 0, 1 to 0, 0 to -1, 0 to 1)) {
                        val target = n.move(from, apps, recent, columns, n.FIRST_APP, dx, dy, watch)
                        assertTrue(target in present, "hidden target $target for $apps/$recent/$columns/$watch")
                        if (target !in reached) queue.add(target)
                    }
                }
                assertEquals(present, reached, "unreachable control for $apps/$recent/$columns/$watch")
            }
    }
}
