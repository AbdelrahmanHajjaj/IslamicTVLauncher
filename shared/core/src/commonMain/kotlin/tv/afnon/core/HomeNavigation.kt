@file:OptIn(kotlin.js.ExperimentalJsExport::class)

package tv.afnon.core

import kotlin.js.JsExport

/** Semantic focus slots; shells bind them to their own controls, never screen coordinates. */
@JsExport
object HomeNavigation {
    const val TODAY = 0
    const val NETWORK = 1
    const val SETTINGS = 2
    const val FIRST_APP = 3
    const val FIRST_RECENT = 12
    const val ALL_APPS = 17
    const val RADIO = 18
    const val INPUTS = 19
    const val WATCH_INPUT = 20
    const val NODE_COUNT = 21
    const val MAX_APPS = 9
    const val MAX_RECENT = 5

    fun initial(apps: Int, recent: Int): Int {
        require(apps in 0..MAX_APPS && recent in 0..MAX_RECENT) { "Invalid Home app counts" }
        return if (apps > 0) FIRST_APP else if (recent > 0) FIRST_RECENT else ALL_APPS
    }

    /** dx/dy are one logical cardinal step; the shell swaps physical horizontal keys in RTL. */
    fun move(from: Int, apps: Int, recent: Int, columns: Int, lastApp: Int,
             dx: Int, dy: Int, watchInput: Boolean): Int {
        require(apps in 0..MAX_APPS && recent in 0..MAX_RECENT && columns in 1..maxOf(1, apps)) { "Invalid Home rows" }
        require((dx == -1 || dx == 1) && dy == 0 || (dy == -1 || dy == 1) && dx == 0) { "Expected one Home direction" }
        if (!available(from, apps, recent, watchInput)) return initial(apps, recent)
        val upTarget = if (apps == 0) TODAY else lastApp.coerceIn(FIRST_APP, FIRST_APP + apps - 1)
        if (dy > 0 && isTop(from) && apps == 0) return initial(apps, recent)
        val to = when (from) {
            TODAY -> if (dx > 0) INPUTS else if (dy > 0) FIRST_APP else from
            INPUTS -> if (dx < 0) TODAY else if (dx > 0) RADIO else if (dy > 0) FIRST_APP else from
            RADIO -> if (dx < 0) INPUTS else if (dx > 0) NETWORK else if (dy > 0) FIRST_APP else from
            NETWORK -> if (dx < 0) RADIO else if (dx > 0) SETTINGS else if (dy > 0) FIRST_APP + columns / 2 else from
            SETTINGS -> if (dx < 0) NETWORK else if (dy > 0) FIRST_APP + columns - 1 else from
            ALL_APPS -> if (dx < 0 && watchInput) WATCH_INPUT else if (dx < 0 && recent > 0) FIRST_RECENT + recent - 1 else if (dy < 0) upTarget else from
            WATCH_INPUT -> if (dx < 0 && recent > 0) FIRST_RECENT + recent - 1 else if (dx > 0 || dy > 0) ALL_APPS else if (dy < 0) upTarget else from
            else -> if (from in FIRST_APP until FIRST_APP + apps) {
                val slot = from - FIRST_APP
                val column = slot % columns
                when {
                    dx < 0 && column > 0 -> from - 1
                    dx > 0 && column < columns - 1 && slot + 1 < apps -> from + 1
                    dy < 0 && slot >= columns -> from - columns
                    dy < 0 -> if (column == 0) RADIO else if (column == columns - 1) SETTINGS else NETWORK
                    dy > 0 && (slot / columns + 1) * columns < apps -> FIRST_APP + minOf(apps - 1, slot + columns)
                    dy > 0 -> if (recent == 0) ALL_APPS else FIRST_RECENT + minOf(recent - 1, column * MAX_RECENT / columns)
                    else -> from
                }
            } else {
                when {
                    dx < 0 && from > FIRST_RECENT -> from - 1
                    dx > 0 && from < FIRST_RECENT + recent - 1 -> from + 1
                    dx > 0 -> if (watchInput) WATCH_INPUT else ALL_APPS
                    dy < 0 -> upTarget
                    dy > 0 -> ALL_APPS
                    else -> from
                }
            }
        }
        return if (available(to, apps, recent, watchInput)) to else from
    }

    private fun isTop(node: Int): Boolean =
        node == TODAY || node == INPUTS || node == RADIO || node == NETWORK || node == SETTINGS

    private fun available(node: Int, apps: Int, recent: Int, watchInput: Boolean): Boolean =
        isTop(node) || node == ALL_APPS || node == WATCH_INPUT && watchInput ||
            node in FIRST_APP until FIRST_APP + apps || node in FIRST_RECENT until FIRST_RECENT + recent
}
