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

package com.github.nwillc.funkjdbc.testing

import com.github.nwillc.funkjdbc.testing.Sql.Companion.FILE_EXTENSION
import java.io.File
import java.lang.annotation.Inherited

/**
 * An annotation to associate SQL scripts with a class.
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class Sql(
    /** Script paths. */
    vararg val value: String,
    /** The execution phase of these scripts. */
    val executionPhase: ExecutionPhase = ExecutionPhase.BEFORE_TEST_METHOD
) {
    /**
     * Enumeration indicating if a script should be executed before or after a method.
     */
    enum class ExecutionPhase {
        BEFORE_TEST_METHOD,
        AFTER_TEST_METHOD
    }
    companion object {
        const val FILE_EXTENSION = ".sql"
    }
}

/**
 * An annotation to associate a number of [Sql] with a class.
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@Inherited
annotation class Sqls(
    /** The [Sql] instances. */
    vararg val value: Sql
)

/**
 * For any instance, check for [Sql] and [Sqls] annotations of the provided [Sql.ExecutionPhase]
 * and list out all the scripts provided.
 * @param executionPhase which phase, before or after.
 * @return list of SQL scripts.
 * @since 0.9.1
 */
fun Any.sqlFor(executionPhase: Sql.ExecutionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD): List<File> {
    val list = mutableListOf<String>()

    val sql = javaClass.getAnnotation(Sql::class.java)
    val scripts = if (sql != null && sql.executionPhase == executionPhase) sql.value else emptyArray()

    val sqls2 = javaClass.getAnnotation(Sqls::class.java)?.value ?: emptyArray()
    val scripts2 = sqls2.asSequence()
        .filter { it.executionPhase == executionPhase }
        .map { it.value.asSequence() }
        .flatten()
        .toList()

    list += scripts
    list += scripts2

    return list.map { File(it) }
        .map { it.sqlScripts() }
        .flatten()
        .toList()
}

/**
 * Return a list of SQL scripts (i.e. files with .sql extension) based on the [File].
 * If the file is a SQL script return the file. If the file is a directory, walk the directory
 * returning SQL scripts in the directory.
 * @return list of actual SQL scripts.
 * @since 0.9.1
 */
fun File.sqlScripts(): List<File> = when {
    this.isFile && this.name.endsWith(FILE_EXTENSION, true) -> listOf(this)
    this.isDirectory ->
        this.walk()
            .filter { it.isFile }
            .map { it.sqlScripts() }
            .flatten()
            .sorted()
            .toList()
    else -> emptyList()
}

