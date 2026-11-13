@file:OptIn(kotlin.js.ExperimentalJsExport::class)

package tv.afnon.core

import kotlin.js.JsExport

/** One size preference for app-owned UI; rendering and persistence stay in each shell. */
@JsExport
object UiScale {
    const val MIN_PERCENT = 80
    const val MAX_PERCENT = 150
    const val STEP_PERCENT = 10
    const val DEFAULT_PERCENT = 100

    fun percent(value: Double): Int = if (value >= MIN_PERCENT && value <= MAX_PERCENT && value % STEP_PERCENT == 0.0)
        value.toInt() else DEFAULT_PERCENT
}
