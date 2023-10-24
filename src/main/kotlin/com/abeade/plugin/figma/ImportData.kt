package com.abeade.plugin.figma

import java.io.File

data class ImportData(
    val file: File?,
    val resource: String,
    val matches: Map<String, String>,
    val confirmOverride: Boolean,
    val skipResourcesWithNoFolder: Boolean,
    val launchWebPConversion: Boolean,
)
