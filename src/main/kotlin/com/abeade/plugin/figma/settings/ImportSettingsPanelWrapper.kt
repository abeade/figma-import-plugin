package com.abeade.plugin.figma.settings

import com.abeade.plugin.figma.ImportDialogWrapper
import com.abeade.plugin.figma.ui.ImportSettingsPanel
import com.intellij.ide.util.PropertiesComponent
import com.intellij.openapi.ui.panel.ComponentPanelBuilder
import java.awt.BorderLayout
import javax.swing.JPanel

class ImportSettingsPanelWrapper(private val propertiesComponent: PropertiesComponent) {

    private val settingsPanel = ImportSettingsPanel()

    init {
        settingsPanel.resourcePrefixHelpPanel.layout = BorderLayout()
        settingsPanel.resourcePrefixHelpPanel.add(
            ComponentPanelBuilder.createCommentComponent("Prefix used in purposed resource name.<br/>When empty no prefix will be added.", false)
        )
        settingsPanel.resourceCreateHelpPanel.layout = BorderLayout()
        settingsPanel.resourceCreateHelpPanel.add(
            ComponentPanelBuilder.createCommentComponent("Avoids to auto create missing density folders.<br/>When a resource has no destination density folder it will be skipped.", false)
        )
    }

    val isModified: Boolean
        get() = settingsPanel.resourcePrefixTextField.text != getCurrentPrefix() ||
                settingsPanel.resourceCreateCheckBoxField.isSelected != getCurrentCreate()

    fun apply() {
        propertiesComponent.setValue(ImportDialogWrapper.PREFIX_KEY, settingsPanel.resourcePrefixTextField.text)
        propertiesComponent.setValue(ImportDialogWrapper.SKIP_KEY, settingsPanel.resourceCreateCheckBoxField.isSelected)
    }

    fun reset() {
        settingsPanel.resourcePrefixTextField.text = getCurrentPrefix()
        settingsPanel.resourceCreateCheckBoxField.isSelected = getCurrentCreate()
    }

    fun createPanel(): JPanel = settingsPanel.mainPanel

    private fun getCurrentPrefix(): String =
        propertiesComponent.getValue(ImportDialogWrapper.PREFIX_KEY) ?: ImportDialogWrapper.RESOURCE_PREFIX

    private fun getCurrentCreate(): Boolean =
        propertiesComponent.isTrueValue(ImportDialogWrapper.SKIP_KEY)
}