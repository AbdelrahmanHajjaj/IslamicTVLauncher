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

    fun minimum(id: String): Int = if (bind(id, "number").range == "percent") 0 else -maximum(id)

    fun maximum(id: String): Int = when (bind(id, "number").range) {
        "prayer-offset" -> PrayerRules.MAX_OFFSET_MINUTES
        "hijri-offset" -> PrayerRules.MAX_HIJRI_OFFSET_DAYS
        "percent" -> 100
        else -> throw IllegalArgumentException("Unknown UI number range: $id")
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
