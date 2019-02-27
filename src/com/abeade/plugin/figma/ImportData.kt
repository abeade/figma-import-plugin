package com.abeade.plugin.figma

import java.io.File

data class ImportData(
    val file: File?,
    val matches: Map<String, String>
)
