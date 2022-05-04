package com.abeade.plugin.figma

import com.abeade.plugin.figma.ui.FilePreviewDialog
import com.abeade.plugin.figma.ui.FilePreviewItem
import com.abeade.plugin.figma.utils.isValidEntry
import com.intellij.openapi.ui.DialogWrapper
import java.io.File
import java.util.zip.ZipFile
import javax.imageio.ImageIO
import javax.swing.Action
import javax.swing.BoxLayout
import javax.swing.JComponent

class FilePreviewDialogWrapper(
    private val file: File
) : DialogWrapper(true) {

    init {
        init()
        title = file.name
    }

    override fun createCenterPanel(): JComponent = FilePreviewDialog().apply {
        contentPanel.layout = BoxLayout(contentPanel, BoxLayout.X_AXIS)
        ZipFile(file).use { zipFile ->
            zipFile.entries().asSequence().filter { it.isValidEntry() }.sortedBy { it.size }.forEach { entry ->
                val preview = FilePreviewItem()
                try {
                    val image = ImageIO.read(zipFile.getInputStream(entry))
                    preview.quickDrawPanel.setImage(image)
                    preview.labelDescription.text = "${entry.name} (${image.width} x ${image.height})"
                } catch (e: Exception) {
                    preview.labelDescription.text = "${entry.name} (Failed to process file)"
                }
                contentPanel.add(preview.mainPanel)
            }
        }
    }.mainPanel

    override fun createActions(): Array<Action> = arrayOf(okAction)
}
