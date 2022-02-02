package com.abeade.plugin.figma.utils

private const val EMPTY_STRING = ""

val String.Companion.EMPTY: String
    get() = EMPTY_STRING

fun String.containsAny(keywords: List<String>): Boolean =
    findFirstOf(keywords) != null

fun String.findFirstOf(keywords: List<String>): String? {
    for (keyword in keywords) {
        if (this.contains(keyword)) return keyword
    }
    return null
}