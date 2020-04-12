
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
    const val publicationName = "maven"
    const val dokkaDir = "docs/dokka"
}

object PluginVersions {
    const val bintray = "1.8.5"
    const val detekt = "1.7.4"
    const val dokka = "0.10.1"
    const val kotlin = "1.3.71"
    const val ktlint = "9.2.1"
    const val vplugin = "3.0.3"
}

object ToolVersions {
    const val ktlint = "0.36.0"
    const val jacoco = "0.8.3"
}

object Versions {
    const val assertJ = "3.15.0"
    const val coroutines = "1.3.5"
    const val h2 = "1.4.200"
    const val jupiter = "5.6.1"
}

object Dependencies {
    val plugins = mapOf(
        "org.jetbrains.kotlin.jvm" to PluginVersions.kotlin,
        "org.jetbrains.dokka" to PluginVersions.dokka,
        "org.jlleitschuh.gradle.ktlint" to PluginVersions.ktlint,
        "com.github.nwillc.vplugin" to PluginVersions.vplugin,
        "io.gitlab.arturbosch.detekt" to PluginVersions.detekt,
        "com.jfrog.bintray" to PluginVersions.bintray
    )
    val artifacts = mapOf(
        "org.jetbrains.kotlin:kotlin-stdlib-jdk8" to PluginVersions.kotlin,
        "org.jetbrains.kotlinx:kotlinx-coroutines-core" to Versions.coroutines,
        "org.junit.jupiter:junit-jupiter" to Versions.jupiter,
        "org.assertj:assertj-core" to Versions.assertJ,
        "com.h2database:h2" to Versions.h2
    )

    fun plugins(vararg keys: String, block: (Pair<String, String>) -> Unit) =
        keys
            .map { it to (plugins[it] ?: error("No plugin $it registered in Dependencies.")) }
            .forEach {
                block(it)
            }

    fun artifacts(vararg keys: String, block: (String) -> Unit) =
        keys
            .map { it to (artifacts[it] ?: error("No artifact $it registered in Dependencies.")) }
            .forEach { (n, v) ->
                block("$n:$v")
            }
}
