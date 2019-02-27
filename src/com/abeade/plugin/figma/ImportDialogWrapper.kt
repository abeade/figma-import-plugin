package com.abeade.plugin.figma

import com.intellij.ide.util.PropertiesComponent
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.ui.layout.panel
import java.io.File
import java.util.zip.ZipFile
import javax.swing.JCheckBox
import javax.swing.JComponent
import javax.swing.JFileChooser
import javax.swing.JTextField
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
    private var file: File? = null
    private lateinit var zipFilesList: MutableList<String>
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

    override fun getDimensionServiceKey(): String? = DIMENSION_SERVICE_KEY

    override fun createCenterPanel(): JComponent {
        val ldpi = propertiesComponent.getValue(LDPI_KEY).orEmpty()
        val mdpi = propertiesComponent.getValue(MDPI_KEY).orEmpty()
        val hdpi = propertiesComponent.getValue(HDPI_KEY).orEmpty()
        val xhdpi = propertiesComponent.getValue(XHDPI_KEY).orEmpty()
        val xxhdpi = propertiesComponent.getValue(XXHDPI_KEY).orEmpty()
        val xxxhdpi = propertiesComponent.getValue(XXXHDPI_KEY).orEmpty()
        val savehdpi = propertiesComponent.getBoolean(SAVE_KEY)
        val directory = propertiesComponent.getValue(DIRECTORY_KEY)

        rememberCheckBox = JCheckBox().apply { isSelected = savehdpi }
        fileField = JTextField(String.empty).apply { isEditable = false }
        resourceField = JTextField(String.empty)
        ldpiField = JTextField(ldpi)
        mdpiField = JTextField(mdpi)
        hdpiField = JTextField(hdpi)
        xhdpiField = JTextField(xhdpi)
        xxhdpiField = JTextField(xxhdpi)
        xxxhdpiField = JTextField(xxxhdpi)

        return panel {
            noteRow("Select zip file with figma exported resources")
            row("File:") { fileField() }
            row(String.empty) { button("Select file") { openFile(directory) } }
            row("Resource name:") { resourceField() }
            noteRow("Select the suffixes used for each density (empty densities will be skipped)")
            row("ldpi suffix:") { ldpiField() }
            row("mdpi suffix:") { mdpiField() }
            row("hdpi suffix:") { hdpiField() }
            row("xhdpi suffix:") { xhdpiField() }
            row("xxhdpi suffix:") { xxhdpiField() }
            row("xxxhdpi suffix:") { xxxhdpiField() }
            row("Remember suffixes") { rememberCheckBox() }
            row { }
            noteRow("""Do you like this plugin? <a href="https://github.com/abeade/figma-import-plugin">Star the repo and contribute</a>""")
        }
    }

    override fun doOKAction() {
        data = ImportData(
            file,
            ldpiField.text,
            mdpiField.text,
            hdpiField.text,
            xhdpiField.text,
            xxhdpiField.text,
            xxxhdpiField.text
        )
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
            resource = when {
                zipFilesList.size > 1 ->
                    zipFilesList.subList(1, zipFilesList.size).fold(zipFilesList[0]) { prefix, item ->
                        prefix.commonPrefixWith(item)
                    }
                zipFilesList.size > 0 -> zipFilesList[0]
                else -> String.empty
            }.replace("[^A-Za-z0-9_]".toRegex(), String.empty).toLowerCase()
            resourceField.text = resource
        }
    }
}