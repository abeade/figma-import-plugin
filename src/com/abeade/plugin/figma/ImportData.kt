package com.abeade.plugin.figma

import java.io.File

data class ImportData(
    val file: File?,
    val ldpiPrefix: String,
    val mdpiPrefix: String,
    val hdpiPrefix: String,
    val xhdpiPrefix: String,
    val xxhdpiPrefix: String,
    val xxxhdpiPrefix: String
)
