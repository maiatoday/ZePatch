plugins {
    alias(libs.plugins.kotlin.jvm)
}

kotlin {
    jvmToolchain(21)
}

dependencies {
    implementation(project(":patch-annotations"))
    implementation(libs.symbol.processing.api)
    implementation(libs.kotlinpoet)
}
