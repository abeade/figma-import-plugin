package com.abeade.plugin.figma

import com.intellij.ide.util.PropertiesComponent
import com.intellij.openapi.options.SearchableConfigurable
import javax.swing.JComponent

class ImportSettings : SearchableConfigurable {

    private val panel = ImportSettingsPanel()

    override fun getDisplayName(): String? = "Figma import"

    override fun getHelpTopic(): String? = "Figma import"

    override fun getId(): String = "Figma import"

    override fun enableSearch(p0: String?): Runnable? = null

    override fun createComponent(): JComponent? = panel.createPanel(PropertiesComponent.getInstance())

    override fun isModified(): Boolean = panel.isModified

    override fun apply() {
        panel.apply()
    }

    override fun reset() {
        panel.reset()
    }

    override fun disposeUIResources() {
        // Empty
    }
}