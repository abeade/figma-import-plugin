package com.abeade.plugin.figma.utils

import com.android.sdklib.AndroidVersion
import com.android.tools.idea.model.AndroidModel
import com.android.tools.idea.model.MergedManifestManager
import com.android.tools.idea.model.queryMinSdkAndTargetSdkFromManifestIndex
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.LangDataKeys
import com.intellij.openapi.module.Module
import com.intellij.openapi.project.DumbService
import com.intellij.openapi.project.IndexNotReadyException
import org.jetbrains.android.facet.AndroidFacet
import org.jetbrains.kotlin.idea.base.util.isAndroidModule
import kotlin.math.min

fun AnActionEvent.getMinSdkVersion(): Int {
    val module: Module? = getData(LangDataKeys.MODULE) ?: getData(LangDataKeys.MODULE_CONTEXT)
    var minSdkVersion = Int.MAX_VALUE
    if (module != null && module.isAndroidModule()) {
        val facet = AndroidFacet.getInstance(module)
        if (facet != null) {
            val minSdk = getMinSdkVersion(facet)
            minSdkVersion = min(minSdkVersion.toDouble(), minSdk.featureLevel.toDouble()).toInt()
        }
    }
    return minSdkVersion
}

private fun getMinSdkVersion(facet: AndroidFacet): AndroidVersion {
    val androidModel = AndroidModel.get(facet)
    if (androidModel != null) {
        return androidModel.minSdkVersion
    }
    try {
        return DumbService.getInstance(facet.module.project)
            .runReadActionInSmartMode<AndroidVersion> { facet.queryMinSdkAndTargetSdkFromManifestIndex().minSdk }
    } catch (_: IndexNotReadyException) { }
    return MergedManifestManager.getSnapshot(facet).minSdkVersion
}