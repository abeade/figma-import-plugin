package com.abeade.plugin.figma

import com.intellij.ide.util.PropertiesComponent
import com.intellij.openapi.options.SearchableConfigurable
import javax.swing.JComponent

class ImportSettings : SearchableConfigurable {

    private val settingsWrapper = ImportSettingsPanelWrapper(PropertiesComponent.getInstance())

    override fun getDisplayName(): String? = "Figma import"

    override fun getHelpTopic(): String? = "Figma import"

    override fun getId(): String = "Figma import"

    override fun createComponent(): JComponent? = settingsWrapper.createPanel()

    override fun isModified(): Boolean = settingsWrapper.isModified

    override fun apply() {
        settingsWrapper.apply()
    }

    override fun reset() {
        settingsWrapper.reset()
    }
}