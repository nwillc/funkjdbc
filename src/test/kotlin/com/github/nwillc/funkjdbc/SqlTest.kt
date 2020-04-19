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

package com.github.nwillc.funkjdbc

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.io.File

@Sql("foo", "bar")
@Sqls(
    Sql("src/test/resources/db/migrations"),
    Sql("bif", "baz", executionPhase = Sql.ExecutionPhase.TEARDOWN)
)
class SqlTest {
    lateinit var sql: Sql
    lateinit var sqls: Sqls

    @BeforeEach
    fun setUp() {
        sql = this.javaClass.getAnnotation(Sql::class.java)!!
        sqls = this.javaClass.getAnnotation(Sqls::class.java)!!
    }

    @Test
    fun `should be able to get scripts from annotation`() {
        assertThat(sql.value).contains("foo", "bar")
    }

    @Test
    fun `should retrieve sql for phase`() {
        val sql = sqlFor()
        assertThat(sql.map { it.name }).containsExactly("0__words_table.sql", "1__seed_data.sql")
    }

    @Test
    fun `should filter for single sql file`() {
        val scriptName = "src/test/resources/db/migrations/0__words_table.sql"
        val file = File(scriptName)
        val sqlScripts = file.sqlScripts()
        assertThat(sqlScripts).hasSize(1)
        assertThat(sqlScripts.last().path).endsWith(scriptName)
    }

    @Test
    fun `should walk folder`() {
        val folder = File("src/test/resources/db/migrations")
        val scripts = folder.sqlScripts()
        assertThat(scripts).hasSize(2)
    }

    @Test
    internal fun foo() {
        val op = Operation { x: Int -> x * x }
        op(10)
    }
}

fun interface Operation<T> {
            operator fun invoke(x: T): T
        }
