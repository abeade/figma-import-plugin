package com.abeade.plugin.figma

import com.intellij.ide.util.PropertiesComponent
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.ui.layout.panel
import java.io.File
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
    private lateinit var fileField: JTextField
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
        fileField = JTextField("").apply { isEditable = false }
        ldpiField = JTextField(ldpi)
        mdpiField = JTextField(mdpi)
        hdpiField = JTextField(hdpi)
        xhdpiField = JTextField(xhdpi)
        xxhdpiField = JTextField(xxhdpi)
        xxxhdpiField = JTextField(xxxhdpi)

        return panel {
            noteRow("Select zip file with figma exported resources")
            row("File:") { fileField() }
            row("") { button("Select file") { openFile(directory) } }
            noteRow("Select the suffixes used for each density (empty densities should be skipped)")
            row("ldpi suffix:") { ldpiField() }
            row("mdpi suffix:") { mdpiField() }
            row("hdpi suffix:") { hdpiField() }
            row("xhdpi suffix:") { xhdpiField() }
            row("xxhdpi suffix:") { xxhdpiField() }
            row("xxxhdpi suffix:") { xxxhdpiField() }
            row("Remember suffixes") { rememberCheckBox() }
            noteRow("""Do not have an account? <a href="https://account.jetbrains.com/login">Sign Up</a>""")
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
        if (rememberCheckBox.isSelected) {
            propertiesComponent.setValue(LDPI_KEY, ldpiField.text)
            propertiesComponent.setValue(MDPI_KEY, mdpiField.text)
            propertiesComponent.setValue(HDPI_KEY, hdpiField.text)
            propertiesComponent.setValue(XHDPI_KEY, xhdpiField.text)
            propertiesComponent.setValue(XXHDPI_KEY, xxhdpiField.text)
            propertiesComponent.setValue(XXXHDPI_KEY, xxxhdpiField.text)
            propertiesComponent.setValue(SAVE_KEY, true)
            propertiesComponent.setValue(DIRECTORY_KEY, file?.parent)
        } else {
            propertiesComponent.setValue(LDPI_KEY, null)
            propertiesComponent.setValue(MDPI_KEY, null)
            propertiesComponent.setValue(HDPI_KEY, null)
            propertiesComponent.setValue(XHDPI_KEY, null)
            propertiesComponent.setValue(XXHDPI_KEY, null)
            propertiesComponent.setValue(XXXHDPI_KEY, null)
            propertiesComponent.setValue(SAVE_KEY, null)
        }
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
        }
    }
}
