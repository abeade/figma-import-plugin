package com.abeade.plugin.figma.settings

import com.abeade.plugin.figma.ImportDialogWrapper
import com.abeade.plugin.figma.ui.ImportSettingsPanel
import com.intellij.ide.util.PropertiesComponent
import com.intellij.ui.RelativeFont
import com.intellij.util.ui.JBUI
import java.awt.Font
import javax.swing.JPanel
import javax.swing.plaf.FontUIResource

class ImportSettingsPanelWrapper(private val propertiesComponent: PropertiesComponent) {

    private val settingsPanel = ImportSettingsPanel()

    init {
        settingsPanel.resourcePrefixHelpLabel.apply {
            foreground = JBUI.CurrentTheme.ContextHelp.FOREGROUND
            font = getCommentFont(font)
        }
        settingsPanel.resourceCreateHelpLabel.apply {
            foreground = JBUI.CurrentTheme.ContextHelp.FOREGROUND
            font = getCommentFont(font)
        }
    }

    private fun getCommentFont(font: Font?): Font =
        FontUIResource(RelativeFont.NORMAL.fromResource("ContextHelp.fontSizeOffset", -2).derive(font))

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
