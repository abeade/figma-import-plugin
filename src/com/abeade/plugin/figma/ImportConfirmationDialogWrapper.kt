package com.abeade.plugin.figma

import com.abeade.plugin.figma.ui.ImportConfirmationDialog
import com.intellij.openapi.ui.DialogWrapper
import javax.swing.JComponent

class ImportConfirmationDialogWrapper(private val resource: String, private val affectedDensities: MutableList<String>) :
    DialogWrapper(true) {

    init {
        init()
        title = "Import figma resources"
    }

    override fun createCenterPanel(): JComponent?  {
        return ImportConfirmationDialog().apply {
            if (affectedDensities.size > 1) {
                itemsLabel.text = affectedDensities.joinToString(prefix = "<html><br/>", separator = "<br/>", postfix = "<br/><br/></html>")
                titleLabel.text = "Resource $resource already exists in the following densities:"
                questionLabel.text = "Do you want to overwrite them?"
            } else {
                titleLabel.text = "Resource $resource already exists in density ${affectedDensities[0]}"
                itemsLabel.text = " "
                questionLabel.text = "Do you want to overwrite it?"
            }
        }.mainPanel
    }
}
