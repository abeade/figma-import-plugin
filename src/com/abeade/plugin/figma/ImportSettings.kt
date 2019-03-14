package com.abeade.plugin.figma

import com.intellij.ide.util.PropertiesComponent
import com.intellij.openapi.options.SearchableConfigurable
import javax.swing.JComponent

class ImportSettings : SearchableConfigurable {

    private val panel = ImportSettingsPanel(PropertiesComponent.getInstance())

    override fun getDisplayName(): String? = "Figma import"

    override fun getHelpTopic(): String? = "Figma import"

    override fun getId(): String = "Figma import"

    override fun createComponent(): JComponent? = panel

    override fun isModified(): Boolean = panel.isModified

    override fun apply() {
        panel.apply()
    }

    override fun reset() {
        panel.reset()
    }
}