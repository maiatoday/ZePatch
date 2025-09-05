package de.berlindroid.zepatch.utils

fun String.uppercaseWords() =
    replace("_", " ")
        .split(" ")
        .joinToString(separator = " ") {
            it.first().uppercase() + it.drop(1).lowercase()
        }
