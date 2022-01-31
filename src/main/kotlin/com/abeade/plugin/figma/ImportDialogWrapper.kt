package com.abeade.plugin.figma

import com.abeade.plugin.figma.ui.ImportDialog
import com.intellij.icons.AllIcons
import com.intellij.ide.util.PropertiesComponent
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.openapi.ui.ValidationInfo
import com.intellij.ui.IdeBorderFactory
import java.awt.Desktop
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import java.io.File
import java.net.URI
import java.util.zip.ZipFile
import javax.swing.*
import javax.swing.event.DocumentEvent
import javax.swing.event.DocumentListener
import javax.swing.filechooser.FileNameExtensionFilter

class ImportDialogWrapper(private val propertiesComponent: PropertiesComponent, private val resPath: File) :
    DialogWrapper(true),
    DocumentListener
{

    init {
        init()
        title = "Import Figma Resources"
    }

    val importData: ImportData?
        get() = data

    private lateinit var dialog: ImportDialog

    private var data: ImportData? = null
    private var result = mutableMapOf<String, String>()
    private var file: File? = null
    private var zipFilesList: MutableList<String>? = null
    private var resource: String? = null

    override fun getDimensionServiceKey(): String = DIMENSION_SERVICE_KEY

    @Suppress("DialogTitleCapitalization")
    override fun createCenterPanel(): JComponent {
        val ldpi = propertiesComponent.getValue(LDPI_KEY).orEmpty()
        val mdpi = propertiesComponent.getValue(MDPI_KEY).orEmpty()
        val hdpi = propertiesComponent.getValue(HDPI_KEY).orEmpty()
        val xhdpi = propertiesComponent.getValue(XHDPI_KEY).orEmpty()
        val xxhdpi = propertiesComponent.getValue(XXHDPI_KEY).orEmpty()
        val xxxhdpi = propertiesComponent.getValue(XXXHDPI_KEY).orEmpty()
        val saveDensities = propertiesComponent.getBoolean(SAVE_KEY)
        val override = propertiesComponent.getBoolean(OVERRIDE_KEY, true)
        val directory = propertiesComponent.getValue(DIRECTORY_KEY)
        val prefix = propertiesComponent.getValue(PREFIX_KEY) ?: RESOURCE_PREFIX

        dialog = ImportDialog().apply {
            selectFileButton.addActionListener { openFile(directory) }
            rememberCheckBox.isSelected = saveDensities
            overrideCheckBox.isSelected = override
            comboBoxField.addActionListener {
                updateLabels()
            }
            resourceField.text = prefix
            fileField.addMouseListener(object : MouseAdapter() {
                override fun mouseClicked(e: MouseEvent?) {
                    openFile(directory)
                }
            })
            ldpiField.text = ldpi
            mdpiField.text = mdpi
            hdpiField.text = hdpi
            xhdpiField.text = xhdpi
            xxhdpiField.text = xxhdpi
            xxxhdpiField.text = xxxhdpi
            ldpiField.document.addDocumentListener(this@ImportDialogWrapper)
            mdpiField.document.addDocumentListener(this@ImportDialogWrapper)
            hdpiField.document.addDocumentListener(this@ImportDialogWrapper)
            xhdpiField.document.addDocumentListener(this@ImportDialogWrapper)
            xxhdpiField.document.addDocumentListener(this@ImportDialogWrapper)
            xxxhdpiField.document.addDocumentListener(this@ImportDialogWrapper)
            filePanel.border = IdeBorderFactory.createTitledBorder("Select zip file with figma exported resources (JPG or PNG)")
            resourcesPanel.border = IdeBorderFactory.createTitledBorder("Select the suffixes used for each density (empty densities will be skipped)")
            moreInfoLabel.addMouseListener(object : MouseAdapter() {
                override fun mouseClicked(e: MouseEvent?) {
                    Desktop.getDesktop().browse(URI("https://github.com/abeade/figma-import-plugin"))
                }
            })
        }
        updateLabels()
        return dialog.mainPanel
    }

    override fun doOKAction() {
        val skip =  propertiesComponent.isTrueValue(SKIP_KEY)
        data = ImportData(file, dialog.resourceField.text, result, dialog.overrideCheckBox.isSelected, skip)
        val remember = dialog.rememberCheckBox.isSelected
        propertiesComponent.setValue(LDPI_KEY, dialog.ldpiField.text.takeIf { remember })
        propertiesComponent.setValue(MDPI_KEY, dialog.mdpiField.text.takeIf { remember })
        propertiesComponent.setValue(HDPI_KEY, dialog.hdpiField.text.takeIf { remember })
        propertiesComponent.setValue(XHDPI_KEY, dialog.xhdpiField.text.takeIf { remember })
        propertiesComponent.setValue(XXHDPI_KEY, dialog.xxhdpiField.text.takeIf { remember })
        propertiesComponent.setValue(XXXHDPI_KEY, dialog.xxxhdpiField.text.takeIf { remember })
        propertiesComponent.setValue(SAVE_KEY, remember)
        propertiesComponent.setValue(OVERRIDE_KEY, dialog.overrideCheckBox.isSelected)
        propertiesComponent.setValue(DIRECTORY_KEY, file?.parent)
        super.doOKAction()
    }

    override fun doValidate(): ValidationInfo? {
        processResult()
        return when {
            dialog.fileField.text.isEmpty() -> ValidationInfo("Zip file required", dialog.fileField)
            dialog.resourceField.text.isBlank() -> ValidationInfo("Resource name required", dialog.resourceField)
            dialog.resourceField.text.contains('.') -> ValidationInfo("Resource name should not contain extension", dialog.resourceField)
            dialog.ldpiField.text.isBlank() && dialog.mdpiField.text.isBlank() && dialog.hdpiField.text.isBlank()
                    && dialog.xhdpiField.text.isBlank() && dialog.xxhdpiField.text.isBlank() && dialog.xxxhdpiField.text.isBlank() ->
                ValidationInfo("At least one density prefix should be defined")
            result.isEmpty() -> ValidationInfo("No resource matches found! Review the prefixes and ensure you're using PNG or JPG")
            else -> null
        }
    }

    override fun changedUpdate(p0: DocumentEvent?) {
        updateLabels()
    }

    override fun insertUpdate(p0: DocumentEvent?) {
        updateLabels()
    }

    override fun removeUpdate(p0: DocumentEvent?) {
        updateLabels()
    }

    private fun processResult() {
        result.clear()
        zipFilesList?.forEach {
            addFieldToResult(it, dialog.ldpiField.text, Density.LDPI)
            addFieldToResult(it, dialog.mdpiField.text, Density.MDPI)
            addFieldToResult(it, dialog.hdpiField.text, Density.HDPI)
            addFieldToResult(it, dialog.xhdpiField.text, Density.XHDPI)
            addFieldToResult(it, dialog.xxhdpiField.text, Density.XXHDPI)
            addFieldToResult(it, dialog.xxxhdpiField.text, Density.XXXHDPI)
        }
    }

    private fun addFieldToResult(fileName: String, suffix: String, density: Density) {
        if (suffix.isNotBlank() && fileName.substringBeforeLast('.').endsWith(suffix)) {
            result[fileName] = density.getFolder()
        }
    }

    private fun updateLabels() {
        updateLabelField(dialog.ldpiLabel, dialog.ldpiField)
        updateLabelField(dialog.mdpiLabel, dialog.mdpiField)
        updateLabelField(dialog.hdpiLabel, dialog.hdpiField)
        updateLabelField(dialog.xhdpiLabel, dialog.xhdpiField)
        updateLabelField(dialog.xxhdpiLabel, dialog.xxhdpiField)
        updateLabelField(dialog.xxxhdpiLabel, dialog.xxxhdpiField)
        zipFilesList?.let {
            val skip = propertiesComponent.isTrueValue(SKIP_KEY)
            updateIconField(dialog.ldpiIconLabel, dialog.ldpiField, it, skip, Density.LDPI)
            updateIconField(dialog.mdpiIconLabel, dialog.mdpiField, it, skip, Density.MDPI)
            updateIconField(dialog.hdpiIconLabel, dialog.hdpiField, it, skip, Density.HDPI)
            updateIconField(dialog.xhdpiIconLabel, dialog.xhdpiField, it, skip, Density.XHDPI)
            updateIconField(dialog.xxhdpiIconLabel, dialog.xxhdpiField, it, skip, Density.XXHDPI)
            updateIconField(dialog.xxxhdpiIconLabel, dialog.xxxhdpiField, it, skip, Density.XXXHDPI)
        }
    }

    private fun updateIconField(
        iconLabel: JLabel,
        field: JTextField,
        filesList: MutableList<String>,
        skipWhenNotExists: Boolean,
        density: Density
    ) {
        val destinationExists = File(resPath, density.getFolder()).exists()
        val suffix = field.text
        if (suffix.isBlank()) {
            iconLabel.icon = AllIcons.General.Warning
            iconLabel.toolTipText = "Empty suffix. Density will be skipped"
        } else if (!destinationExists && skipWhenNotExists) {
            iconLabel.icon = AllIcons.General.Warning
            iconLabel.toolTipText = "Density folder not found, resource will be skipped<br/>You can change this in plugin settings"
        } else {
            if (filesList.any { it.substringBeforeLast('.').endsWith(suffix) }) {
                iconLabel.icon = AllIcons.General.InspectionsOK
                iconLabel.toolTipText = "Resource found"
            } else {
                iconLabel.icon = AllIcons.General.Error
                iconLabel.toolTipText = "Resource not found"
            }
        }
    }

    private fun Density.getFolder(): String =
        FOLDER_DRAWABLE + (if (dialog.comboBoxField.selectedIndex == 1) DARK_MODIFIER else "") + value

    private fun updateLabelField(label: JLabel, field: JTextField) {
        label.isEnabled = field.text.isNotBlank()
    }

    private fun openFile(directory: String?) {
        val fileDialog = JFileChooser().apply {
            directory?.let { currentDirectory = File(directory) }
            fileSelectionMode = JFileChooser.FILES_ONLY
            fileFilter = FileNameExtensionFilter("ZIP Files", "zip", "zip")
            isAcceptAllFileFilterUsed = false
            dialogTitle = "Select figma exported zip file"
        }
        val result = fileDialog.showOpenDialog(contentPane)
        if (result == JFileChooser.APPROVE_OPTION) {
            file = fileDialog.selectedFile
            dialog.fileField.text = fileDialog.selectedFile.toString()
            ZipFile(fileDialog.selectedFile).use { zipFile ->
                zipFilesList = zipFile.entries().asSequence()
                    .filter {
                        !it.isDirectory && (it.name.endsWith(".png", true) ||
                                it.name.endsWith(".jpg", true))
                    }
                    .fold(mutableListOf()) { list, entry -> list.apply { add(File(entry.name).name) } }
            }
            zipFilesList?.let {
                resource = when {
                    it.size > 1 -> it.subList(1, it.size).fold(it[0]) { prefix, item -> prefix.commonPrefixWith(item) }
                    it.size > 0 -> it[0]
                    else -> String.EMPTY
                }.replace("[^A-Za-z0-9_]".toRegex(), String.EMPTY).lowercase()
                val prefix = propertiesComponent.getValue(PREFIX_KEY) ?: RESOURCE_PREFIX
                if (resource?.startsWith(prefix) == false) {
                    resource = prefix + resource
                }
                dialog.resourceField.text = resource
            }
            updateLabels()
        }
    }

    private enum class Density(val value: String) {
        LDPI("ldpi"),
        MDPI("mdpi"),
        HDPI("hdpi"),
        XHDPI("xhdpi"),
        XXHDPI("xxhdpi"),
        XXXHDPI("xxxhdpi")
    }

    companion object {

        const val RESOURCE_PREFIX = "ic_"

        const val PREFIX_KEY = "#com.abeade.plugin.figma.importDialog.prefix"
        const val SKIP_KEY = "#com.abeade.plugin.figma.importDialog.create"

        private const val LDPI_KEY = "#com.abeade.plugin.figma.importDialog.lpdi"
        private const val MDPI_KEY = "#com.abeade.plugin.figma.importDialog.mpdi"
        private const val HDPI_KEY = "#com.abeade.plugin.figma.importDialog.hpdi"
        private const val XHDPI_KEY = "#com.abeade.plugin.figma.importDialog.xhpdi"
        private const val XXHDPI_KEY = "#com.abeade.plugin.figma.importDialog.xxhpdi"
        private const val XXXHDPI_KEY = "#com.abeade.plugin.figma.importDialog.xxxhpdi"
        private const val SAVE_KEY = "#com.abeade.plugin.figma.importDialog.savehdpi"
        private const val OVERRIDE_KEY = "#com.abeade.plugin.figma.importDialog.override"
        private const val DIRECTORY_KEY = "#com.abeade.plugin.figma.importDialog.directory"
        private const val DIMENSION_SERVICE_KEY = "#com.abeade.plugin.figma.importDialog"

        private const val FOLDER_DRAWABLE = "drawable-"
        private const val DARK_MODIFIER = "night-"
    }
}
