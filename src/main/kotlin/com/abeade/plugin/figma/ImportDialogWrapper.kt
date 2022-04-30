package com.abeade.plugin.figma

import com.abeade.plugin.figma.ui.ImportDialog
import com.abeade.plugin.figma.ui.PreviewPanel
import com.abeade.plugin.figma.ui.autocomplete.Autocomplete
import com.abeade.plugin.figma.utils.EMPTY
import com.abeade.plugin.figma.utils.containsAny
import com.abeade.plugin.figma.utils.findFirstOf
import com.intellij.icons.AllIcons
import com.intellij.ide.util.PropertiesComponent
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.openapi.ui.ValidationInfo
import com.intellij.openapi.ui.popup.JBPopup
import com.intellij.openapi.ui.popup.JBPopupFactory
import com.intellij.ui.IdeBorderFactory
import com.intellij.vcs.commit.NonModalCommitPanel.Companion.showAbove
import java.awt.Desktop
import java.awt.Dimension
import java.awt.event.*
import java.io.File
import java.io.IOException
import java.net.URI
import java.util.zip.ZipFile
import javax.imageio.ImageIO
import javax.swing.*
import javax.swing.event.DocumentEvent
import javax.swing.event.DocumentListener
import javax.swing.filechooser.FileNameExtensionFilter

class ImportDialogWrapper(
    private val propertiesComponent: PropertiesComponent,
    private val resPath: File
) : DialogWrapper(true),
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
            qualifierBeforeField.setupAutocomplete(findPreQualifiers().toList())
            qualifierAfterField.setupAutocomplete(findPostQualifiers().toList())
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
            filePanel.border = IdeBorderFactory.createTitledBorder("Select ZIP file with figma exported resources (JPG or PNG)")
            qualifiersPanel.border = IdeBorderFactory.createTitledBorder("Select resource folders qualifiers (optional)")
            resourcesPanel.border = IdeBorderFactory.createTitledBorder("Select the suffixes used for each density (empty densities will be skipped)")
            moreInfoLabel.addMouseListener(object : MouseAdapter() {
                override fun mouseClicked(e: MouseEvent?) {
                    Desktop.getDesktop().browse(URI("https://github.com/abeade/figma-import-plugin"))
                }
            })
            ldpiIconViewLabel.icon = AllIcons.General.InspectionsEye
            mdpiIconViewLabel.icon = AllIcons.General.InspectionsEye
            hdpiIconViewLabel.icon = AllIcons.General.InspectionsEye
            xhdpiIconViewLabel.icon = AllIcons.General.InspectionsEye
            xxhdpiIconViewLabel.icon = AllIcons.General.InspectionsEye
            xxxhdpiIconViewLabel.icon = AllIcons.General.InspectionsEye
        }
        updateLabels()
        return dialog.mainPanel
    }

    private fun JTextField.setupAutocomplete(keywords: List<String>) {
        val autocomplete = Autocomplete(this, keywords, object : DocumentListener {
            override fun insertUpdate(e: DocumentEvent?) = onChanged()

            override fun removeUpdate(e: DocumentEvent?) = onChanged()

            override fun changedUpdate(e: DocumentEvent?) = onChanged()
        })
        document.addDocumentListener(autocomplete)
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "commit")
        actionMap.put("commit", object : AbstractAction() {
            override fun actionPerformed(ev: ActionEvent) {
                autocomplete.complete()
            }
        })
        addFocusListener(object : FocusListener {
            override fun focusGained(e: FocusEvent?) = Unit

            override fun focusLost(e: FocusEvent?) {
                autocomplete.complete()
            }
        })
    }

    private fun onChanged() {
        val prefix = dialog.qualifierBeforeField.text.sanitizeQualifier()
        val suffix = dialog.qualifierAfterField.text.sanitizeQualifier()
        val strPrefix = if (prefix.isNotBlank()) "$prefix$QUALIFIER_SEPARATOR" else ""
        val strSuffix = if (suffix.isNotBlank()) "$QUALIFIER_SEPARATOR$suffix" else ""
        dialog.qualifierResultLabel.text = "$FOLDER_DRAWABLE${strPrefix}hdpi$strSuffix"
        updateLabels()
    }

    private fun String.sanitizeQualifier() =
        filter { it.isDigit() || it.isLetter() || it == QUALIFIER_SEPARATOR[0] }
        .removePrefix(QUALIFIER_SEPARATOR)
        .removeSuffix(QUALIFIER_SEPARATOR)

    private fun findPreQualifiers(): Set<String> {
        val densities = Density.values().map { it.value }.sortedDescending()
        @Suppress("RECEIVER_NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
        return resPath.listFiles(File::isDirectory)
            .map { it.name }
            .filter { it.startsWith(FOLDER_DRAWABLE) && it.containsAny(densities) }
            .map { it.removePrefix(FOLDER_DRAWABLE)
                .substringBefore(it.findFirstOf(densities)!!)
                .removePrefix(QUALIFIER_SEPARATOR)
                .removeSuffix(QUALIFIER_SEPARATOR)
            }
            .flatMap { it.split(QUALIFIER_SEPARATOR) }
            .filter { it.isNotBlank() }
            .toSet()
    }

    private fun findPostQualifiers(): Set<String> {
        val densities = Density.values().map { it.value }.sortedDescending()
        @Suppress("RECEIVER_NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
        return resPath.listFiles(File::isDirectory)
            .map { it.name }
            .filter { it.startsWith(FOLDER_DRAWABLE) && it.containsAny(densities) }
            .map { it.substringAfter(it.findFirstOf(densities)!!).removePrefix(QUALIFIER_SEPARATOR) }
            .flatMap { it.split(QUALIFIER_SEPARATOR) }
            .filter { it.isNotBlank() }
            .toSet()
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
        val duplicated = dialog.findFirstDuplicatedSuffix()
        return when {
            dialog.fileField.text.isEmpty() -> ValidationInfo("Zip file required", dialog.fileField)
            dialog.resourceField.text.isBlank() -> ValidationInfo("Resource name required", dialog.resourceField)
            dialog.resourceField.text.contains('.') -> ValidationInfo("Resource name should not contain extension", dialog.resourceField)
            dialog.ldpiField.text.isBlank() && dialog.mdpiField.text.isBlank() && dialog.hdpiField.text.isBlank()
                    && dialog.xhdpiField.text.isBlank() && dialog.xxhdpiField.text.isBlank() && dialog.xxxhdpiField.text.isBlank() ->
                ValidationInfo("At least one density suffix should be defined")
            result.isEmpty() -> ValidationInfo("No resource matches found! Review the prefixes and ensure you're using PNG or JPG")
            duplicated != null -> ValidationInfo("This suffix is duplicated", duplicated)
            else -> null
        }
    }

    private fun ImportDialog.findFirstDuplicatedSuffix(): JComponent? {
        val items = listOf(ldpiField, mdpiField, hdpiField, xhdpiField, xxhdpiField, xxxhdpiField).filterNot { it.text.isBlank() }
        for (i in 0 until items.size - 1) {
            for (j in i +  1 until items.size) {
                if (items[i].text == items[j].text) return items[i]
            }
        }
        return null
    }

    override fun changedUpdate(e: DocumentEvent?) {
        updateLabels()
    }

    override fun insertUpdate(e: DocumentEvent?) {
        updateLabels()
    }

    override fun removeUpdate(e: DocumentEvent?) {
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
            updateIconField(dialog.ldpiIconLabel, dialog.ldpiIconViewLabel, dialog.ldpiField, it, skip, Density.LDPI)
            updateIconField(dialog.mdpiIconLabel, dialog.mdpiIconViewLabel, dialog.mdpiField, it, skip, Density.MDPI)
            updateIconField(dialog.hdpiIconLabel, dialog.hdpiIconViewLabel, dialog.hdpiField, it, skip, Density.HDPI)
            updateIconField(dialog.xhdpiIconLabel, dialog.xhdpiIconViewLabel, dialog.xhdpiField, it, skip, Density.XHDPI)
            updateIconField(dialog.xxhdpiIconLabel, dialog.xxhdpiIconViewLabel, dialog.xxhdpiField, it, skip, Density.XXHDPI)
            updateIconField(dialog.xxxhdpiIconLabel, dialog.xxxhdpiIconViewLabel, dialog.xxxhdpiField, it, skip, Density.XXXHDPI)
        } ?: run {
            dialog.ldpiIconViewLabel.isVisible = false
            dialog.mdpiIconViewLabel.isVisible = false
            dialog.hdpiIconViewLabel.isVisible = false
            dialog.xhdpiIconViewLabel.isVisible = false
            dialog.xxhdpiIconViewLabel.isVisible = false
            dialog.xxxhdpiIconViewLabel.isVisible = false
        }
    }

    private fun updateIconField(
        iconLabel: JLabel,
        viewIconLabel: JLabel,
        field: JTextField,
        filesList: MutableList<String>,
        skipWhenNotExists: Boolean,
        density: Density
    ) {
        val destinationExists = File(resPath, density.getFolder()).exists()
        val suffix = field.text
        if (suffix.isBlank()) {
            iconLabel.icon = AllIcons.General.Warning
            viewIconLabel.isVisible = false
            iconLabel.toolTipText = "Empty suffix. Density will be skipped"
        } else if (!destinationExists && skipWhenNotExists) {
            iconLabel.icon = AllIcons.General.Warning
            viewIconLabel.isVisible = false
            iconLabel.toolTipText = "Density folder not found, resource will be skipped<br/>You can change this in plugin settings"
        } else {
            val file = filesList.firstOrNull { it.substringBeforeLast('.').endsWith(suffix) }
            if (file != null) {
                iconLabel.icon = AllIcons.General.InspectionsOK
                iconLabel.toolTipText = "Resource found"
                viewIconLabel.isVisible = true
                viewIconLabel.mouseListeners.forEach { viewIconLabel.removeMouseListener(it) }
                viewIconLabel.addMouseListener(object : MouseAdapter() {
                    override fun mouseClicked(e: MouseEvent?) {
                        showPreviewPopup(file, viewIconLabel)
                    }
                })
            } else {
                iconLabel.icon = AllIcons.General.Error
                viewIconLabel.isVisible = false
                iconLabel.toolTipText = "Resource not found"
            }
        }
    }

    private fun showPreviewPopup(fileName: String, target: JComponent): JBPopup? =
        getBufferedImage(fileName)?.let { image ->
            val previewPanel = PreviewPanel()
            previewPanel.quickDrawPanel.setImage(image)
            previewPanel.labelSize.text = "${image.width} x ${image.height}"
            val popupBuilder = JBPopupFactory.getInstance().createComponentPopupBuilder(previewPanel.mainPanel, null)
            popupBuilder.createPopup().apply {
                size = Dimension(
                    /* width = */ minOf(image.width, MAX_PREVIEW_SIZE) + WIDTH_PREVIEW_MARGIN,
                    /* height = */ minOf(image.height, MAX_PREVIEW_SIZE) + HEIGHT_PREVIEW_MARGIN
                )
                showAbove(target)
            }
        }

    private fun getBufferedImage(densityFile: String) = try {
        file?.let { selectedZip ->
            ZipFile(selectedZip).use { zipFile ->
                zipFile.entries().toList().find { it.name.endsWith(densityFile) }?.let { entry ->
                    ImageIO.read(zipFile.getInputStream(entry))
                }
            }
        }
    } catch (e: IOException) {
        null
    }

    private fun Density.getFolder(): String {
        val preValue = dialog.qualifierBeforeField.text.sanitizeQualifier()
        val preModifier = if (preValue.isBlank()) "" else "$preValue$QUALIFIER_SEPARATOR"
        val postValue = dialog.qualifierAfterField.text.sanitizeQualifier()
        val postModifier = if (postValue.isBlank()) "" else "$QUALIFIER_SEPARATOR$postValue"
        return "$FOLDER_DRAWABLE$preModifier$value$postModifier"
    }

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

        private const val QUALIFIER_SEPARATOR = "-"
        private const val FOLDER_DRAWABLE = "drawable$QUALIFIER_SEPARATOR"

        private const val MAX_PREVIEW_SIZE = 400
        private const val WIDTH_PREVIEW_MARGIN = 4
        private const val HEIGHT_PREVIEW_MARGIN = 26
    }
}
