@file:OptIn(kotlin.js.ExperimentalJsExport::class)

package tv.afnon.core

import kotlin.js.JsExport

/** Semantic controls, independent of widgets, stored preference keys and OS APIs. */
@JsExport
class UiControl(
    val id: String,
    val section: String,
    val kind: String,
    val labelKey: String,
    val save: String,
    val range: String,
    internal val options: Array<String>,
)

@JsExport
object UiContract {
    fun controls(): Array<UiControl> = UiCatalog.controls.copyOf()

    fun control(id: String): UiControl = UiCatalog.controls.firstOrNull { it.id == id }
        ?: throw IllegalArgumentException("Unknown UI control: $id")

    fun bind(id: String, kind: String): UiControl {
        val control = control(id)
        require(control.kind == kind) { "Wrong UI control kind: $id" }
        return control
    }

    fun minimum(id: String): Int = when (bind(id, "number").range) {
        "ui-scale" -> UiScale.MIN_PERCENT
        "percent", "bearing" -> 0
        else -> -maximum(id)
    }

    fun maximum(id: String): Int = when (bind(id, "number").range) {
        "ui-scale" -> UiScale.MAX_PERCENT
        "prayer-offset" -> PrayerRules.MAX_OFFSET_MINUTES
        "hijri-offset" -> PrayerRules.MAX_HIJRI_OFFSET_DAYS
        "percent" -> 100
        "bearing" -> 359
        else -> throw IllegalArgumentException("Unknown UI number range: $id")
    }

    fun numberEditor(id: String): String = if (bind(id, "number").range == "bearing") "bearing" else "inline"

    fun numberStep(id: String): Int {
        bind(id, "number")
        return when (id) {
            "display.size" -> UiScale.STEP_PERCENT
            "reminders.volume", "prayer.facing" -> 5
            else -> 1
        }
    }

    fun adjustNumber(id: String, value: Double, direction: Double): Int {
        val low = minimum(id)
        val high = maximum(id)
        require(value.isFinite() && value % 1.0 == 0.0) { "Invalid numeric draft: $id" }
        require(direction == -1.0 || direction == 0.0 || direction == 1.0) { "Invalid numeric direction: $id" }
        val current = value.coerceIn(low.toDouble(), high.toDouble()).toInt()
        val next = (current + direction.toInt() * numberStep(id)).coerceIn(low, high)
        return if (id == "display.size") UiScale.percent(next.toDouble()) else next
    }

    fun prayerAdhanId(prayer: Double): String = when (prayer) {
        0.0 -> "prayer.adhan-fajr"
        2.0 -> "prayer.adhan-dhuhr"
        3.0 -> "prayer.adhan-asr"
        4.0 -> "prayer.adhan-maghrib"
        5.0 -> "prayer.adhan-isha"
        else -> throw IllegalArgumentException("No adhan control for prayer index: $prayer")
    }

    fun choices(id: String): Array<String> = bind(id, "choice").options.copyOf()

    fun validateChoices(id: String, offered: Array<String>) {
        val allowed = bind(id, "choice").options
        for (value in offered) {
            require(value in allowed) { "Unknown UI choice: $id=$value" }
            require(offered.count { it == value } == 1) { "Duplicate UI choice: $id=$value" }
        }
    }

    /** Conformance aggregates conditionally available editors before accounting for explicit gaps. */
    fun validateChoiceCoverage(id: String, offered: Array<String>, gaps: Array<String>) {
        validateChoices(id, offered)
        validateChoices(id, gaps)
        for (value in offered) require(value !in gaps) { "Bound UI choice declared missing: $id=$value" }
        for (value in choices(id)) require(value in offered || value in gaps) { "Missing UI choice: $id=$value" }
    }

    /** Bindings come from real controls; omitted controls require explicit shell gap declarations. */
    fun validateCoverage(bound: Array<String>, gaps: Array<String>) {
        for (id in bound) {
            control(id)
            require(bound.count { it == id } == 1 && id !in gaps) { "Duplicate UI control: $id" }
        }
        for (id in gaps) {
            control(id)
            require(gaps.count { it == id } == 1) { "Duplicate UI control: $id" }
        }
        for (control in UiCatalog.controls) {
            require(control.id in bound || control.id in gaps) { "Missing UI control: ${control.id}" }
        }
    }
}
