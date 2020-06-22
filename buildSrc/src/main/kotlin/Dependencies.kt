
/*
 * Copyright (c) 2020, nwillc@gmail.com
 *
 * Permission to use, copy, modify, and/or distribute this software for any
 * purpose with or without fee is hereby granted, provided that the above
 * copyright notice and this permission notice appear in all copies.
 *
 * THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL WARRANTIES
 * WITH REGARD TO THIS SOFTWARE INCLUDING ALL IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR
 * ANY SPECIAL, DIRECT, INDIRECT, OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES
 * WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN
 * ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF
 * OR IN CONNECTION WITH THE USE OR PERFORMANCE OF THIS SOFTWARE.
 *
 */

object Constants {
    const val dokkaDir = "docs/dokka"
    const val group =  "com.github.nwillc"
    const val publicationName = "maven"
    const val version = "0.12.1-SNAPSHOT"
}

object PluginVersions {
    const val bintray = "1.8.5"
    const val detekt = "1.10.0-RC1"
    const val dokka = "0.10.1"
    const val kotlin = "1.3.72"
    const val vplugin = "3.0.5"
}

object ToolVersions {
    const val jacoco = "0.8.3"
}

object Versions {
    const val assertJ = "3.16.1"
    const val coroutines = "1.3.7"
    const val h2 = "1.4.200"
    const val jupiter = "5.7.0-M1"
}

object Dependencies {
    val plugins = mapOf(
        "com.github.nwillc.vplugin" to PluginVersions.vplugin,
        "com.jfrog.bintray" to PluginVersions.bintray,
        "io.gitlab.arturbosch.detekt" to PluginVersions.detekt,
        "org.jetbrains.dokka" to PluginVersions.dokka,
        "org.jetbrains.kotlin.jvm" to PluginVersions.kotlin
    )
    val artifacts = mapOf(
        "com.h2database:h2" to Versions.h2,
        "io.gitlab.arturbosch.detekt:detekt-cli" to PluginVersions.detekt,
        "io.gitlab.arturbosch.detekt:detekt-formatting" to PluginVersions.detekt,
        "org.assertj:assertj-core" to Versions.assertJ,
        "org.jetbrains.kotlin:kotlin-stdlib-jdk8" to PluginVersions.kotlin,
        "org.jetbrains.kotlinx:kotlinx-coroutines-core" to Versions.coroutines,
        "org.junit.jupiter:junit-jupiter" to Versions.jupiter
    )

    fun artifacts(vararg keys: String, block: (String) -> Unit) =
        keys
            .map { it to (artifacts[it] ?: error("No artifact $it registered in Dependencies.")) }
            .forEach { (n, v) ->
                block("$n:$v")
            }
}
