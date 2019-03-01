package com.abeade.plugin.figma

import com.intellij.openapi.ui.DialogWrapper
import com.intellij.ui.layout.panel
import javax.swing.JComponent

class ImportConfirmationDialogWrapper(private val resource: String, private val affectedDensities: MutableList<String>) :
    DialogWrapper(true) {

    init {
        init()
        title = "Import figma resources"
    }

    override fun createCenterPanel(): JComponent? =
        panel {
            if (affectedDensities.size > 1) {
                val densities = StringBuilder()
                affectedDensities.forEach { densities.appendln(it) }
                noteRow("Resource $resource already exists in the following densities:\n\n$densities\nDo you want to overwrite them?")
            } else {
                noteRow("Resource $resource already exists in density ${affectedDensities[0]}\n\nDo you want to overwrite it?")
            }
            row { }
        }
}
