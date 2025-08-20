package de.berlindroid.zepatch.annotations

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.SOURCE)
annotation class Patch(
    val name: String = ""
)
