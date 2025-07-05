package de.berlindroid.zepatch

import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable

/**
 * It's like Dagger, but way worse.
 *
 * A (hopefully temporary) dependency injection framework, with no runtime cost, cause everything is
 * hand-witten.
 */

val patchables = mapOf<String, @Composable () -> Unit>(
    "Testing1" to { BasicText("123") },
    "Testing2" to { BasicText("123") },
    "Testing3" to { BasicText("123") },
    "Testing4" to { BasicText("123") },
    "Testing5" to { BasicText("123") },
    "Testing6" to { BasicText("123") },
)
