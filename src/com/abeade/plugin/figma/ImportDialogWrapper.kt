package com.abeade.plugin.figma

import com.intellij.icons.AllIcons
import com.intellij.ide.util.PropertiesComponent
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.openapi.ui.ValidationInfo
import com.intellij.ui.layout.panel
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import java.io.File
import java.util.zip.ZipFile
import javax.swing.*
import javax.swing.event.DocumentEvent
import javax.swing.event.DocumentListener
import javax.swing.filechooser.FileNameExtensionFilter

class ImportDialogWrapper(private val propertiesComponent: PropertiesComponent) : DialogWrapper(true), DocumentListener {

    companion object {

        const val RESOURCE_PREFIX = "ic_"
        const val PREFIX_KEY = "#com.abeade.plugin.figma.importDialog.prefix"
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
        private const val FOLDER_LDPI = "drawable-ldpi"
        private const val FOLDER_MDPI = "drawable-mdpi"
        private const val FOLDER_HDPI = "drawable-hdpi"
        private const val FOLDER_XHDPI = "drawable-xhdpi"
        private const val FOLDER_XXHDPI = "drawable-xxhdpi"
        private const val FOLDER_XXXHDPI = "drawable-xxxhdpi"
    }

    init {
        init()
        title = "Import figma resources"
    }

    val importData: ImportData?
        get() = data

    private var data: ImportData? = null
    private var result = mutableMapOf<String, String>()
    private var file: File? = null
    private var zipFilesList: MutableList<String>? = null
    private var resource: String? = null

    private lateinit var fileField: JTextField
    private lateinit var resourceField: JTextField
    private lateinit var ldpiField: JTextField
    private lateinit var mdpiField: JTextField
    private lateinit var hdpiField: JTextField
    private lateinit var xhdpiField: JTextField
    private lateinit var xxhdpiField: JTextField
    private lateinit var xxxhdpiField: JTextField
    private lateinit var rememberCheckBox: JCheckBox
    private lateinit var overrideCheckBox: JCheckBox
    private lateinit var ldpiLabel: JLabel
    private lateinit var mdpiLabel: JLabel
    private lateinit var hdpiLabel: JLabel
    private lateinit var xhdpiLabel: JLabel
    private lateinit var xxhdpiLabel: JLabel
    private lateinit var xxxhdpiLabel: JLabel
    private lateinit var ldpiIconLabel: JLabel
    private lateinit var mdpiIconLabel: JLabel
    private lateinit var hdpiIconLabel: JLabel
    private lateinit var xhdpiIconLabel: JLabel
    private lateinit var xxhdpiIconLabel: JLabel
    private lateinit var xxxhdpiIconLabel: JLabel

    override fun getDimensionServiceKey(): String? = DIMENSION_SERVICE_KEY

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
        val prefix = propertiesComponent.getValue(ImportDialogWrapper.PREFIX_KEY) ?: RESOURCE_PREFIX
        rememberCheckBox = JCheckBox().apply { isSelected = saveDensities }
        overrideCheckBox = JCheckBox("Show confirmation dialog if any resource already exists (otherwise resources will be overwritten)").apply { isSelected = override }
        fileField = JTextField(String.EMPTY).apply {
            addMouseListener(object : MouseAdapter() {
                override fun mouseClicked(e: MouseEvent?) {
                    openFile(directory)
                }
            })
            isEditable = false

        }
        resourceField = JTextField(prefix)
        ldpiField = JTextField(ldpi).apply { document.addDocumentListener(this@ImportDialogWrapper) }
        mdpiField = JTextField(mdpi).apply { document.addDocumentListener(this@ImportDialogWrapper) }
        hdpiField = JTextField(hdpi).apply { document.addDocumentListener(this@ImportDialogWrapper) }
        xhdpiField = JTextField(xhdpi).apply { document.addDocumentListener(this@ImportDialogWrapper) }
        xxhdpiField = JTextField(xxhdpi).apply { document.addDocumentListener(this@ImportDialogWrapper) }
        xxxhdpiField = JTextField(xxxhdpi).apply { document.addDocumentListener(this@ImportDialogWrapper) }
        ldpiLabel = JLabel("ldpi suffix:")
        mdpiLabel = JLabel("mdpi suffix:")
        hdpiLabel = JLabel("hdpi suffix:")
        xhdpiLabel = JLabel("xhdpi suffix:")
        xxhdpiLabel = JLabel("xxhdpi suffix:")
        xxxhdpiLabel = JLabel("xxxhdpi suffix:")
        ldpiIconLabel = JLabel()
        mdpiIconLabel = JLabel()
        hdpiIconLabel = JLabel()
        xhdpiIconLabel = JLabel()
        xxhdpiIconLabel = JLabel()
        xxxhdpiIconLabel = JLabel()

        updateLabels()

        return panel {
            noteRow("Select zip file with figma exported resources (JPG or PNG)")
            row("File:") {
                fileField(pushX)
                button("Select file") { openFile(directory) }
            }
            row("Resource name:") { resourceField() }
            row(String.EMPTY) { overrideCheckBox()}
            row { }
            noteRow("Select the suffixes used for each density (empty densities will be skipped)")
            row {
                ldpiLabel()
                ldpiField(pushX, growX)
                ldpiIconLabel()
            }
            row {
                mdpiLabel()
                mdpiField(pushX)
                mdpiIconLabel()
            }
            row {
                hdpiLabel()
                hdpiField(pushX)
                hdpiIconLabel()
            }
            row {
                xhdpiLabel()
                xhdpiField(pushX)
                xhdpiIconLabel()
            }
            row {
                xxhdpiLabel()
                xxhdpiField(pushX)
                xxhdpiIconLabel()
            }
            row {
                xxxhdpiLabel()
                xxxhdpiField(pushX)
                xxxhdpiIconLabel()
            }
            row("Remember suffixes") { rememberCheckBox() }
            row { }
            noteRow("""More info in <a href="https://github.com/abeade/figma-import-plugin">GitHub repository</a>""")
        }
    }

    override fun doOKAction() {
        data = ImportData(file, resourceField.text, result, overrideCheckBox.isSelected)
        val remember = rememberCheckBox.isSelected
        propertiesComponent.setValue(LDPI_KEY, if (remember) ldpiField.text else null)
        propertiesComponent.setValue(MDPI_KEY, if (remember) mdpiField.text else null)
        propertiesComponent.setValue(HDPI_KEY, if (remember) hdpiField.text else null)
        propertiesComponent.setValue(XHDPI_KEY, if (remember) xhdpiField.text else null)
        propertiesComponent.setValue(XXHDPI_KEY, if (remember) xxhdpiField.text else null)
        propertiesComponent.setValue(XXXHDPI_KEY, if (remember) xxxhdpiField.text else null)
        propertiesComponent.setValue(SAVE_KEY, remember)
        propertiesComponent.setValue(OVERRIDE_KEY, overrideCheckBox.isSelected)
        propertiesComponent.setValue(DIRECTORY_KEY, file?.parent)
        super.doOKAction()
    }

    override fun doValidate(): ValidationInfo? {
        processResult()
        return when {
            fileField.text.isEmpty() -> ValidationInfo("Zip file required", fileField)
            resourceField.text.isBlank() -> ValidationInfo("Resource name required", resourceField)
            resourceField.text.contains('.') -> ValidationInfo("Resource name should not contain extension", resourceField)
            ldpiField.text.isBlank() && mdpiField.text.isBlank() && hdpiField.text.isBlank()
                    && xhdpiField.text.isBlank() && xxhdpiField.text.isBlank() && xxxhdpiField.text.isBlank() ->
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
            addFieldToResult(it, ldpiField.text, FOLDER_LDPI)
            addFieldToResult(it, mdpiField.text, FOLDER_MDPI)
            addFieldToResult(it, hdpiField.text, FOLDER_HDPI)
            addFieldToResult(it, xhdpiField.text, FOLDER_XHDPI)
            addFieldToResult(it, xxhdpiField.text, FOLDER_XXHDPI)
            addFieldToResult(it, xxxhdpiField.text, FOLDER_XXXHDPI)
        }
    }

    private fun addFieldToResult(fileName: String, suffix: String, folder: String) {
        if (suffix.isNotBlank() && fileName.substringBeforeLast('.').endsWith(suffix)) {
            result[fileName] = folder
        }
    }

    private fun updateLabels() {
        updateLabelField(ldpiLabel, ldpiField)
        updateLabelField(mdpiLabel, mdpiField)
        updateLabelField(hdpiLabel, hdpiField)
        updateLabelField(xhdpiLabel, xhdpiField)
        updateLabelField(xxhdpiLabel, xxhdpiField)
        updateLabelField(xxxhdpiLabel, xxxhdpiField)
        zipFilesList?.let {
            updateIconField(ldpiIconLabel, ldpiField, it)
            updateIconField(mdpiIconLabel, mdpiField, it)
            updateIconField(hdpiIconLabel, hdpiField, it)
            updateIconField(xhdpiIconLabel, xhdpiField, it)
            updateIconField(xxhdpiIconLabel, xxhdpiField, it)
            updateIconField(xxxhdpiIconLabel, xxxhdpiField, it)
        }
    }

    private fun updateIconField(iconLabel: JLabel, field: JTextField, filesList: MutableList<String>) {
        val suffix = field.text
        if (suffix.isBlank()) {
            iconLabel.icon = AllIcons.General.Warning
            iconLabel.toolTipText = "Empty suffix. Density will be skipped"
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

    private fun updateLabelField(label: JLabel, field: JTextField) {
        label.isEnabled = !field.text.isBlank()
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
            fileField.text = file.toString()
            val zipFile = ZipFile(file)
            zipFilesList = zipFile.entries().asSequence()
                .filter { !it.isDirectory && (it.name.endsWith(".png", true) || it.name.endsWith(".jpg", true)) }
                .fold(mutableListOf()) { list, entry -> list.apply { add(File(entry.name).name) } }
            zipFile.close()
            zipFilesList?.let {
                resource = when {
                    it.size > 1 -> it.subList(1, it.size).fold(it[0]) { prefix, item -> prefix.commonPrefixWith(item) }
                    it.size > 0 -> it[0]
                    else -> String.EMPTY
                }.replace("[^A-Za-z0-9_]".toRegex(), String.EMPTY).toLowerCase()
                val prefix = propertiesComponent.getValue(ImportDialogWrapper.PREFIX_KEY) ?: RESOURCE_PREFIX
                if (resource?.startsWith(prefix) == false) {
                    resource = prefix + resource
                }
                resourceField.text = resource
            }
            updateLabels()
        }
    }
}
