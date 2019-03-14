package com.abeade.plugin.figma

import com.intellij.ide.util.PropertiesComponent
import com.intellij.openapi.ui.panel.ComponentPanelBuilder
import com.intellij.ui.layout.LCFlags
import com.intellij.ui.layout.panel
import javax.swing.BoxLayout
import javax.swing.JPanel
import javax.swing.JTextField

class ImportSettingsPanel(private val propertiesComponent: PropertiesComponent) : JPanel() {

    private val prefixField: JTextField = JTextField()

    init {
        createPanel()
        layout = BoxLayout(this, BoxLayout.Y_AXIS)
    }

    private fun createPanel() {
        add(
            panel(LCFlags.fillX) {
                row("Resource prefix") { prefixField() }
                row("") { ComponentPanelBuilder.createCommentComponent("Prefix used in purposed resource name. When empty no prefix will be added.", false)() }
            }
        )
    }

    val isModified: Boolean
        get() = prefixField.text != getCurrentPrefix()

    fun apply() {
        propertiesComponent.setValue(ImportDialogWrapper.PREFIX_KEY, prefixField.text)
    }

    fun reset() {
        prefixField.text = getCurrentPrefix()
    }

    private fun getCurrentPrefix(): String {
        var prefix = propertiesComponent.getValue(ImportDialogWrapper.PREFIX_KEY)
        if (prefix == null) {
            prefix = ImportDialogWrapper.RESOURCE_PREFIX
        }
        return prefix
    }
}