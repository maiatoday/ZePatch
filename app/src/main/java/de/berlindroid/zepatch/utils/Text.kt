package de.berlindroid.zepatch.utils

fun String.uppercaseWords() =
    replace("_", " ")
        .split(" ")
        .joinToString(separator = " ") {
            first().uppercase() + drop(1).lowercase()
        }
