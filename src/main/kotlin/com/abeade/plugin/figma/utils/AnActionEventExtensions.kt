package com.abeade.plugin.figma.utils

import com.android.sdklib.AndroidVersion
import com.android.tools.idea.model.AndroidModel
import com.android.tools.idea.model.MergedManifestManager
import com.android.tools.idea.model.queryMinSdkAndTargetSdkFromManifestIndex
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.LangDataKeys
import com.intellij.openapi.module.Module
import com.intellij.openapi.project.DumbService
import org.jetbrains.android.facet.AndroidFacet
import org.jetbrains.kotlin.idea.base.util.isAndroidModule

fun AnActionEvent.getMinSdkVersion(): Int {
    val module: Module? = getData(LangDataKeys.MODULE) ?: getData(LangDataKeys.MODULE_CONTEXT)
    var minSdkVersion: Int = Int.MAX_VALUE
    if (module != null && module.isAndroidModule()) {
        val facet = AndroidFacet.getInstance(module)
        if (facet != null) {
            val minSdk = getMinSdkVersion(facet)
            minSdkVersion = minSdk?.featureLevel ?: Int.MAX_VALUE
        }
    }
    return minSdkVersion
}

private fun getMinSdkVersion(facet: AndroidFacet): AndroidVersion? = AndroidModel.get(facet)?.minSdkVersion
    ?: try {
        DumbService.getInstance(facet.module.project)
            .runReadActionInSmartMode<AndroidVersion> { facet.queryMinSdkAndTargetSdkFromManifestIndex().minSdk }
    } catch (_: Exception) {
        try {
            MergedManifestManager.getMergedManifestSupplier(facet.module).get().get().minSdkVersion
        } catch (_: Exception) {
            null
        }
    }