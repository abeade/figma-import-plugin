package com.abeade.plugin.figma

import com.intellij.ide.util.PropertiesComponent
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.openapi.ui.ValidationInfo
import com.intellij.ui.layout.panel
import java.awt.Color
import java.io.File
import java.util.zip.ZipFile
import javax.swing.*
import javax.swing.event.DocumentEvent
import javax.swing.event.DocumentListener
import javax.swing.filechooser.FileNameExtensionFilter

class ImportDialogWrapper(private val propertiesComponent: PropertiesComponent) : DialogWrapper(true) {

    private companion object {

        private const val LDPI_KEY = "#com.abeade.plugin.figma.importDialog.lpdi"
        private const val MDPI_KEY = "#com.abeade.plugin.figma.importDialog.mpdi"
        private const val HDPI_KEY = "#com.abeade.plugin.figma.importDialog.hpdi"
        private const val XHDPI_KEY = "#com.abeade.plugin.figma.importDialog.xhpdi"
        private const val XXHDPI_KEY = "#com.abeade.plugin.figma.importDialog.xxhpdi"
        private const val XXXHDPI_KEY = "#com.abeade.plugin.figma.importDialog.xxxhpdi"
        private const val SAVE_KEY = "#com.abeade.plugin.figma.importDialog.savehdpi"
        private const val DIRECTORY_KEY = "#com.abeade.plugin.figma.importDialog.directory"
        private const val DIMENSION_SERVICE_KEY = "#com.abeade.plugin.figma.importDialog"
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

    private lateinit var ldpiLabel: JLabel
    private lateinit var mdpiLabel: JLabel
    private lateinit var hdpiLabel: JLabel
    private lateinit var xhdpiLabel: JLabel
    private lateinit var xxhdpiLabel: JLabel
    private lateinit var xxxhdpiLabel: JLabel

    override fun getDimensionServiceKey(): String? = DIMENSION_SERVICE_KEY

    override fun createCenterPanel(): JComponent {
        val ldpi = propertiesComponent.getValue(LDPI_KEY).orEmpty()
        val mdpi = propertiesComponent.getValue(MDPI_KEY).orEmpty()
        val hdpi = propertiesComponent.getValue(HDPI_KEY).orEmpty()
        val xhdpi = propertiesComponent.getValue(XHDPI_KEY).orEmpty()
        val xxhdpi = propertiesComponent.getValue(XXHDPI_KEY).orEmpty()
        val xxxhdpi = propertiesComponent.getValue(XXXHDPI_KEY).orEmpty()
        val saveDensities = propertiesComponent.getBoolean(SAVE_KEY)
        val directory = propertiesComponent.getValue(DIRECTORY_KEY)

        rememberCheckBox = JCheckBox().apply { isSelected = saveDensities }
        fileField = JTextField(String.EMPTY).apply { isEditable = false }
        resourceField = JTextField(String.EMPTY)
        ldpiField = JTextField(ldpi).apply { document.addDocumentListener(object : DocumentListener {
            override fun changedUpdate(p0: DocumentEvent?) {
                updateLabels()
            }

            override fun insertUpdate(p0: DocumentEvent?) {
                updateLabels()
            }

            override fun removeUpdate(p0: DocumentEvent?) {
                updateLabels()
            }

        }) }
        mdpiField = JTextField(mdpi).apply { document.addDocumentListener(object : DocumentListener {
            override fun changedUpdate(p0: DocumentEvent?) {
                updateLabels()
            }

            override fun insertUpdate(p0: DocumentEvent?) {
                updateLabels()
            }

            override fun removeUpdate(p0: DocumentEvent?) {
                updateLabels()
            }

        }) }
        hdpiField = JTextField(hdpi).apply { document.addDocumentListener(object : DocumentListener {
            override fun changedUpdate(p0: DocumentEvent?) {
                updateLabels()
            }

            override fun insertUpdate(p0: DocumentEvent?) {
                updateLabels()
            }

            override fun removeUpdate(p0: DocumentEvent?) {
                updateLabels()
            }

        }) }
        xhdpiField = JTextField(xhdpi).apply { document.addDocumentListener(object : DocumentListener {
            override fun changedUpdate(p0: DocumentEvent?) {
                updateLabels()
            }

            override fun insertUpdate(p0: DocumentEvent?) {
                updateLabels()
            }

            override fun removeUpdate(p0: DocumentEvent?) {
                updateLabels()
            }

        }) }
        xxhdpiField = JTextField(xxhdpi).apply { document.addDocumentListener(object : DocumentListener {
            override fun changedUpdate(p0: DocumentEvent?) {
                updateLabels()
            }

            override fun insertUpdate(p0: DocumentEvent?) {
                updateLabels()
            }

            override fun removeUpdate(p0: DocumentEvent?) {
                updateLabels()
            }

        }) }
        xxxhdpiField = JTextField(xxxhdpi).apply { document.addDocumentListener(object : DocumentListener {
            override fun changedUpdate(p0: DocumentEvent?) {
                updateLabels()
            }

            override fun insertUpdate(p0: DocumentEvent?) {
                updateLabels()
            }

            override fun removeUpdate(p0: DocumentEvent?) {
                updateLabels()
            }

        }) }

        ldpiLabel = JLabel("ldpi suffix:")
        mdpiLabel = JLabel("mdpi suffix:")
        hdpiLabel = JLabel("hdpi suffix:")
        xhdpiLabel = JLabel("xhdpi suffix:")
        xxhdpiLabel = JLabel("xxhdpi suffix:")
        xxxhdpiLabel = JLabel("xxxhdpi suffix:")

        return panel {
            noteRow("Select zip file with figma exported resources")
            row("File:") { fileField() }
            row(String.EMPTY) { button("Select file") { openFile(directory) } }
            row("Resource name:") { resourceField() }
            noteRow("Select the suffixes used for each density (EMPTY densities will be skipped)")
            row {
                ldpiLabel()
                ldpiField()
            }
            row {
                mdpiLabel()
                mdpiField()
            }
            row {
                hdpiLabel()
                hdpiField()
            }
            row {
                xhdpiLabel()
                xhdpiField()
            }
            row {
                xxhdpiLabel()
                xxhdpiField()
            }
            row {
                xxxhdpiLabel()
                xxxhdpiField()
            }
            row("Remember suffixes") { rememberCheckBox() }
            row { }
            noteRow("""More info in <a href="https://github.com/abeade/figma-import-plugin">github repo</a>""")
        }
    }

    override fun doOKAction() {
        data = ImportData(file, resourceField.text, result)
        val remember = rememberCheckBox.isSelected
        propertiesComponent.setValue(LDPI_KEY, if (remember) ldpiField.text else null)
        propertiesComponent.setValue(MDPI_KEY, if (remember) mdpiField.text else null)
        propertiesComponent.setValue(HDPI_KEY, if (remember) hdpiField.text else null)
        propertiesComponent.setValue(XHDPI_KEY, if (remember) xhdpiField.text else null)
        propertiesComponent.setValue(XXHDPI_KEY, if (remember) xxhdpiField.text else null)
        propertiesComponent.setValue(XXXHDPI_KEY, if (remember) xxxhdpiField.text else null)
        propertiesComponent.setValue(SAVE_KEY, remember)
        propertiesComponent.setValue(DIRECTORY_KEY, file?.parent)
        super.doOKAction()
    }

    override fun doValidate(): ValidationInfo? {
        processResult()
        return when {
            fileField.text.isEmpty() -> ValidationInfo("Zip file required", fileField)
            resourceField.text.isBlank() -> ValidationInfo("Resource name required", resourceField)
            ldpiField.text.isBlank() && mdpiField.text.isBlank() && hdpiField.text.isBlank()
                    && xhdpiField.text.isBlank() && xxhdpiField.text.isBlank() && xxxhdpiField.text.isBlank() ->
                ValidationInfo("At least one density prefix should be defined")
            result.isEmpty() -> ValidationInfo("No resource matches!! Review the prefixes")
            else -> null
        }
    }

    private fun processResult() {
        result.clear()
        zipFilesList?.forEach {
            addFieldToResult(it, ldpiField.text, "drawable-ldpi")
            addFieldToResult(it, mdpiField.text, "drawable-mdpi")
            addFieldToResult(it, hdpiField.text, "drawable-hdpi")
            addFieldToResult(it, xhdpiField.text, "drawable-xhdpi")
            addFieldToResult(it, xxhdpiField.text, "drawable-xxhdpi")
            addFieldToResult(it, xxxhdpiField.text, "drawable-xxxhdpi")
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
            updateLabelFile(ldpiLabel, ldpiField, it)
            updateLabelFile(mdpiLabel, mdpiField, it)
            updateLabelFile(hdpiLabel, hdpiField, it)
            updateLabelFile(xhdpiLabel, xhdpiField, it)
            updateLabelFile(xxhdpiLabel, xxhdpiField, it)
            updateLabelFile(xxhdpiLabel, xxhdpiField, it)
        }
    }

    private fun updateLabelFile(label: JLabel, field: JTextField, filesList: MutableList<String>) {
        val suffix = field.text
        if (!suffix.isBlank()) {
            if (filesList.any { it.substringBeforeLast('.').endsWith(suffix) }) {
                label.foreground = Color.GREEN
            } else {
                label.foreground = Color.RED
            }
        }
    }

    private fun updateLabelField(label: JLabel, field: JTextField) {
        if (field.text.isBlank()) {
            label.foreground = Color.LIGHT_GRAY
        } else {
            label.foreground = Color.BLACK
        }
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
            zipFilesList = zipFile.entries().asIterator().asSequence().fold(mutableListOf()) { list, entry ->
                list.apply { add(entry.name) }
            }
            zipFile.close()
            zipFilesList?.let {
                resource = when {
                    it.size > 1 ->
                        it.subList(1, it.size).fold(it[0]) { prefix, item -> prefix.commonPrefixWith(item) }
                                .plus(it[0].substring(it[0].lastIndexOf('.')))
                    it.size > 0 -> it[0]
                    else -> String.EMPTY
                }.replace("[^A-Za-z0-9_\\.]".toRegex(), String.EMPTY).toLowerCase()
                resourceField.text = resource
            }
            updateLabels()
        }
    }
}
