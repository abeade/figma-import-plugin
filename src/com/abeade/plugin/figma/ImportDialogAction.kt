package com.abeade.plugin.figma

import com.intellij.ide.util.PropertiesComponent
import com.intellij.notification.Notification
import com.intellij.notification.NotificationType
import com.intellij.notification.Notifications
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.PlatformDataKeys
import com.intellij.openapi.util.io.FileUtil
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiElement
import com.intellij.psi.impl.file.PsiJavaDirectoryImpl
import java.io.File
import java.io.FileOutputStream
import java.util.zip.ZipFile

class ImportDialogAction : AnAction() {

    private companion object {

        private const val RES_DIRECTORY = "res"
    }

    private lateinit var virtualFileRes: VirtualFile

    override fun update(anActionEvent: AnActionEvent) {
        val project = anActionEvent.project
        val psiElement: PsiElement? = anActionEvent.dataContext.getData(PlatformDataKeys.PSI_ELEMENT)
        val isValid = project != null && psiElement != null &&
                (psiElement as? PsiJavaDirectoryImpl)?.isDirectory == true &&
                (psiElement as? PsiJavaDirectoryImpl)?.name == RES_DIRECTORY
        anActionEvent.presentation.isEnabledAndVisible = isValid
        if (isValid) {
            virtualFileRes = (psiElement as PsiJavaDirectoryImpl).virtualFile
        }
    }

    override fun actionPerformed(anActionEvent: AnActionEvent) {
        val dialog = ImportDialogWrapper(PropertiesComponent.getInstance())
        val result = dialog.showAndGet()
        if (result) {
            val resPath = File(virtualFileRes.path)
            var createdItems = 0
            var updatedItems = 0
            val data = dialog.importData!!
            val zipFile = ZipFile(data.file)
            zipFile.entries().asSequence()
                .filter { !it.isDirectory }
                .toList()
                .map {
                    val density = data.matches[File(it.name).name]
                    if (density != null) {
                        val destination = File(resPath, density)
                        if (!destination.exists()) {
                            destination.mkdirs()
                        }
                        val destinationFile = File(destination, data.resource)
                        if (destinationFile.exists()) {
                            updatedItems++
                        } else {
                            createdItems++
                        }
                        val inStream = zipFile.getInputStream(it)
                        val outStream = FileOutputStream(destinationFile)
                        FileUtil.copy(inStream, outStream)
                        outStream.close()
                        inStream.close()
                    }
                }
            zipFile.close()
            val notification = if (updatedItems == 0 && createdItems == 0) {
                Notification("Figma import", "Figma import finished","Figma import no resources has benn created or updated", NotificationType.ERROR)
            } else {
                virtualFileRes.refresh(false, false)
                when {
                    updatedItems == 0 -> Notification("Figma import", "Figma import finished", "$createdItems resources has been created", NotificationType.INFORMATION)
                    createdItems == 0 -> Notification("Figma import", "Figma import finished", "$updatedItems resources has been updated", NotificationType.INFORMATION)
                    else -> Notification("Figma import", "Figma import finished", "$createdItems resources has been created and $updatedItems resources has been updated", NotificationType.INFORMATION)
                }
            }
            Notifications.Bus.notify(notification)
        }
    }
}
