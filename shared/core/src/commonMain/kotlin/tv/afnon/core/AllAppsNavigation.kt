@file:OptIn(kotlin.js.ExperimentalJsExport::class)

package tv.afnon.core

import kotlin.js.JsExport

/** Header roles are negative; nonnegative slots are app indexes in the five-column grid. */
@JsExport
object AllAppsNavigation {
    const val COLUMNS = 5
    const val NONE = -1
    const val BACK = -2
    const val PRAYER = -3
    const val SETTINGS = -4

    /** One logical cardinal step; shells translate RTL once and retain native paging. */
    fun move(from: Int, apps: Int, firstVisible: Int, dx: Int, dy: Int): Int {
        require(apps >= 0 && firstVisible >= 0 && firstVisible % COLUMNS == 0 &&
            (firstVisible == 0 || firstVisible < apps)) { "Invalid All apps rows" }
        require((dx == -1 || dx == 1) && dy == 0 || (dy == -1 || dy == 1) && dx == 0) { "Expected one All apps direction" }
        if (from in SETTINGS..BACK) {
            if (dy > 0 && apps > 0) return firstVisible + if (from == BACK) 0 else minOf(COLUMNS - 1, apps - 1 - firstVisible)
            return when (from) {
                BACK -> if (dx > 0) PRAYER else if (dx < 0) SETTINGS else from
                PRAYER -> if (dx > 0) SETTINGS else if (dx < 0) BACK else from
                else -> if (dx > 0) BACK else if (dx < 0) PRAYER else from
            }
        }
        if (from !in 0 until apps) return if (apps > 0) 0 else BACK
        return when {
            dx < 0 && from % COLUMNS > 0 -> from - 1
            dx > 0 && from % COLUMNS < COLUMNS - 1 && from + 1 < apps -> from + 1
            dy < 0 -> if (from < COLUMNS) BACK else from - COLUMNS
            dy > 0 && from / COLUMNS < (apps - 1) / COLUMNS -> from + minOf(COLUMNS, apps - 1 - from)
            else -> from
        }
    }
}
