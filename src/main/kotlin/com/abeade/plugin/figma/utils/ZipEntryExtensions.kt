package com.abeade.plugin.figma.utils

import java.util.zip.ZipEntry

fun ZipEntry.isValidEntry() = !isDirectory && (name.endsWith(".png", true) ||
    name.endsWith(".jpg", true))