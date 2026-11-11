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
