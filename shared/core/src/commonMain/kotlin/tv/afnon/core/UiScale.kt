@file:OptIn(kotlin.js.ExperimentalJsExport::class)

package tv.afnon.core

import kotlin.js.JsExport

/** One size preference for app-owned UI; rendering and persistence stay in each shell. */
@JsExport
object UiScale {
    const val DEFAULT_PERCENT = 100

    fun percent(value: Double): Int = when (value) {
        80.0, 90.0, 100.0, 110.0 -> value.toInt()
        else -> DEFAULT_PERCENT
    }
}
