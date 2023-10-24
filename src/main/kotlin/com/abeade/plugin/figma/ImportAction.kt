package com.abeade.plugin.figma

import com.abeade.plugin.figma.utils.getMinSdkVersion
import com.android.tools.idea.rendering.webp.ConvertToWebpAction
import com.intellij.ide.util.PropertiesComponent
import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.PlatformDataKeys
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.io.FileUtil
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.vfs.VfsUtil
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiElement
import com.intellij.psi.impl.file.PsiDirectoryImpl
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
        val project = anActionEvent.project ?: return
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
                        showNotification(
                            project = project,
                            message = "Import was cancelled by user.<br/>No resources has been created nor updated.",
                            type = NotificationType.WARNING
                        )
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
                showNotification(
                    project = project,
                    message = "No resources has been created nor updated.",
                    type = NotificationType.ERROR
                )
            } else {
                VfsUtil.markDirtyAndRefresh(
                    /* async = */ !data.launchWebPConversion,
                    /* recursive = */ true,
                    /* reloadChildren = */ true,
                    /* ...files = */ virtualFileRes)
                val message = when {
                    updatedItems == 0 -> "$createdItems resources has been created"
                    createdItems == 0 -> "$updatedItems resources has been updated"
                    else -> "$createdItems resources has been created and $updatedItems resources has been updated"
                }
                showNotification(project = project, message = message, type = NotificationType.INFORMATION)
                if (data.launchWebPConversion) {
                    val localFileSystem = LocalFileSystem.getInstance()
                    val array = destinationFiles.mapNotNull { localFileSystem.findFileByIoFile(it) }.toTypedArray()
                    ConvertToWebpAction().perform(project, anActionEvent.getMinSdkVersion(), array)
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

    private fun showNotification(project: Project, message: String, type: NotificationType) {
        NotificationGroupManager.getInstance()
            .getNotificationGroup("Figma import")
            .createNotification(message, type)
            .notify(project)
    }

    private companion object {

        private const val RES_DIRECTORY = "res"
    }
}
