package com.abeade.plugin.figma

import com.abeade.plugin.figma.utils.getMinSdkVersion
import com.android.tools.idea.rendering.webp.ConvertToWebpAction
import com.intellij.ide.util.PropertiesComponent
import com.intellij.notification.Notification
import com.intellij.notification.NotificationType
import com.intellij.notification.Notifications
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.PlatformDataKeys
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.MessageType
import com.intellij.openapi.ui.popup.Balloon
import com.intellij.openapi.ui.popup.JBPopupFactory
import com.intellij.openapi.util.io.FileUtil
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.vfs.VfsUtil
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.wm.WindowManager
import com.intellij.psi.PsiElement
import com.intellij.psi.impl.file.PsiDirectoryImpl
import com.intellij.ui.awt.RelativePoint
import java.io.File
import java.io.FileOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipFile

class ImportAction : AnAction() {

    private lateinit var virtualFileRes: VirtualFile

    override fun update(anActionEvent: AnActionEvent) {
        val project = anActionEvent.project
        val psiElement: PsiElement? = anActionEvent.dataContext.getData(PlatformDataKeys.PSI_ELEMENT)
        val isValid = project != null && psiElement != null &&
                (psiElement as? PsiDirectoryImpl)?.isDirectory == true &&
                (psiElement as? PsiDirectoryImpl)?.name == RES_DIRECTORY
        anActionEvent.presentation.isEnabledAndVisible = isValid
        if (isValid) {
            virtualFileRes = (psiElement as PsiDirectoryImpl).virtualFile
        }
    }

    override fun actionPerformed(anActionEvent: AnActionEvent) {
        val resPath = File(virtualFileRes.path)
        val dialog = ImportDialogWrapper(PropertiesComponent.getInstance(), resPath)
        val result = dialog.showAndGet()
        if (result) {
            val destinationFiles = mutableListOf<File>()
            var createdItems = 0
            var updatedItems = 0
            val data = dialog.importData!!
            ZipFile(data.file!!).use { zipFile ->
                val zipEntries = zipFile.entries().asSequence()
                    .filter { !it.isDirectory }
                    .toList()
                if (data.confirmOverride) {
                    val existingDensities = getExistingDensities(zipEntries, data, resPath)
                    if (existingDensities.isNotEmpty() &&
                        !ImportConfirmationDialogWrapper(data.resource, existingDensities).showAndGet()
                    ) {
                        showMessage(anActionEvent.project!!, "Import was cancelled by user. No resources has been created nor updated.", true)
                        return
                    }
                }
                zipEntries.forEach {
                    val fileEntry = File(it.name)
                    val density = data.matches[fileEntry.name]
                    if (density != null) {
                        val destination = File(resPath, density)
                        if (destination.exists() || !data.skipResourcesWithNoFolder) {
                            if (!destination.exists()) {
                                destination.mkdirs()
                            }
                            val destinationFile = File(destination, "${data.resource}.${fileEntry.extension}")
                            if (destinationFile.exists()) {
                                updatedItems++
                            } else {
                                createdItems++
                            }
                            destinationFiles.add(destinationFile)
                            val inStream = zipFile.getInputStream(it)
                            val outStream = FileOutputStream(destinationFile)
                            FileUtil.copy(inStream, outStream)
                            outStream.close()
                            inStream.close()
                        }
                    }
                }
            }
            if (updatedItems == 0 && createdItems == 0) {
                showMessage(anActionEvent.project!!, "Figma import no resources has been created or updated", true)
            } else {
                VfsUtil.markDirtyAndRefresh(
                    /* async = */ !data.launchWebPConversion,
                    /* recursive = */ true,
                    /* reloadChildren = */ true,
                    /* ...files = */ virtualFileRes)
                when {
                    updatedItems == 0 -> showMessage(anActionEvent.project!!, "$createdItems resources has been created", false)
                    createdItems == 0 -> showMessage(anActionEvent.project!!, "$updatedItems resources has been updated", false)
                    else -> showMessage(anActionEvent.project!!, "$createdItems resources has been created and $updatedItems resources has been updated", false)
                }
                if (data.launchWebPConversion) {
                    val localFileSystem = LocalFileSystem.getInstance()
                    val array = destinationFiles.mapNotNull { localFileSystem.findFileByIoFile(it) }.toTypedArray()
                    if (array.isNotEmpty()) {
                        ConvertToWebpAction().perform(anActionEvent.project!!, anActionEvent.getMinSdkVersion(), array)
                    }
                }
            }
        }
    }

    private fun getExistingDensities(
        zipEntries: List<ZipEntry>,
        data: ImportData,
        resPath: File
    ) = zipEntries.fold(mutableListOf<String>()) { list, entry ->
        val fileEntry = File(entry.name)
        val density = data.matches[fileEntry.name]
        if (density != null) {
            val destination = File(resPath, density)
            val destinationFile = File(destination, "${data.resource}.${fileEntry.extension}")
            if (destinationFile.exists()) {
                list.add(density)
            }
        }
        list
    }

    private fun showMessage(project: Project, message: String, isError: Boolean) {
        Notifications.Bus.notify(Notification(
            "Figma import",
            "Figma import finished",
            message,
            if (isError) NotificationType.ERROR else NotificationType.INFORMATION
        ))
        val statusBar = WindowManager.getInstance()
            ?.getStatusBar(project)
        JBPopupFactory.getInstance()
            ?.createHtmlTextBalloonBuilder(message, if (isError) MessageType.ERROR else MessageType.INFO, null)
            ?.setFadeoutTime(5000)
            ?.createBalloon()
            ?.show(RelativePoint.getCenterOf(statusBar?.component!!), Balloon.Position.above)
    }

    private companion object {

        private const val RES_DIRECTORY = "res"
    }
}
