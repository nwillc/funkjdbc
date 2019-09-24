
rootProject.name = "funkjdbc"

val detektVersion: String by settings
val dokkaVersion: String by settings
val kotlinVersion: String by settings
val ktlintPluginVersion: String by settings
val vpluginVersion: String by settings

pluginManagement {
    plugins {
        kotlin("jvm") version kotlinVersion
        id("org.jetbrains.dokka") version dokkaVersion
        id("org.jlleitschuh.gradle.ktlint") version ktlintPluginVersion
        id("com.github.nwillc.vplugin") version vpluginVersion
        id("io.gitlab.arturbosch.detekt") version detektVersion
    }
}
