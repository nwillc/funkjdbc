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

import com.github.nwillc.funkjdbc.testing.EmbeddedDb
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import java.sql.Connection
import java.sql.SQLException

@OptIn(ExperimentalCoroutinesApi::class)
@Sql("src/test/resources/db/migrations")
@ExtendWith(EmbeddedDb::class)
class ConnectionExtensionsTest {
    private lateinit var connection: Connection

    @BeforeEach
    fun setUp(dbConfig: DBConfig) {
        connection = dbConfig.getConnection()
    }

    @AfterEach
    internal fun tearDown() {
        connection.close()
    }

    @Test
    fun `should be able to update`() {
        assertThat(connection.update("INSERT INTO WORDS (WORD) VALUES ('d')")).isEqualTo(1)
    }

    @Test
    fun `should throw exception on bad update sql`() {
        assertThatThrownBy { connection.update("blah blah") }
            .isInstanceOf(SQLException::class.java)
    }

    @Test
    fun `should throw exception on bad query sql`() {
        assertThatThrownBy {
            connection.query("blah blah") { rs -> rs.getInt(1) }
        }
            .isInstanceOf(SQLException::class.java)
    }

    @Test
    fun `should be able to update with bound arguments`() {
        val sqlStatement = SqlStatement("INSERT INTO WORDS (WORD) VALUES (?)") {
            it.setString(1, "d")
        }
        assertThat(connection.update(sqlStatement)).isEqualTo(1)
    }

    @Test
    fun `should be able to find a record`() {
        val found = connection.query("SELECT * FROM WORDS WHERE WORD = 'a'") { rs ->
            rs.getString(1)
        }

        assertThat(found.first()).isEqualTo("a")
    }

    @Test
    fun `should be able to not find a record`() {
        val found = connection.query("SELECT * FROM WORDS WHERE WORD = 'd'") { rs ->
            rs.getString(1)
        }

        assertThat(found).isEmpty()
    }

    @Test
    fun `should be able to find as flow`() {
        runBlocking {
            val flowContained = mutableListOf<String>()
            connection.asFlow("SELECT * FROM WORDS") { rs -> rs.getString(1) }
                .toList(flowContained)

            assertThat(flowContained).containsExactly("a", "b", "c")
        }
    }

    @Test
    fun `should be able to flow where some extractions are null`() {
        runBlocking {
            val noBe: Extractor<String?> = { rs ->
                val word = rs.getString(1)
                if (word == "b") null else word
            }
            val flowContained = mutableListOf<String?>()
            connection.asFlow("SELECT * FROM WORDS", noBe)
                .toList(flowContained)

            assertThat(flowContained).containsExactly("a", null, "c")
        }
    }

    @Test
    fun `should be able to find with params as flow`() {
        runBlocking {
            val word = "a"
            val sqlStatement = SqlStatement("SELECT * FROM WORDS WHERE WORD != ?") {
                it.setString(1, word)
            }
            val flowContained = mutableListOf<String>()

            connection.asFlow(sqlStatement) { rs -> rs.getString(1) }
                .collect {
                    flowContained.add(it)
                }

            assertThat(flowContained).containsExactly("b", "c")
        }
    }

    @Test
    fun `should be able to find a record with query arguments`() {
        val sql = SqlStatement("SELECT * FROM WORDS WHERE WORD = ?") {
            it.setString(1, "a")
        }
        val found = connection.query(sql) { rs ->
            rs.getString(1)
        }

        assertThat(found.first()).isEqualTo("a")
    }

    @Test
    fun `should be able to find a record with query arguments and closing connection`() {
        val sql = SqlStatement("SELECT * FROM WORDS WHERE WORD = ?") {
            it.setString(1, "a")
        }

        val found = connection.query(sql) { rs ->
            rs.getString(1)
        }

        assertThat(found.first()).isEqualTo("a")
    }

    @Test
    fun `should be able to not find a record with query arguments`() {
        val sql = SqlStatement("SELECT * FROM WORDS WHERE WORD = ?") {
            it.setString(1, "d")
        }
        val found = connection.query(sql) { rs ->
            rs.getString(1)
        }

        assertThat(found).isEmpty()
    }

    @Test
    fun `should commit a transaction`() {
        connection.transaction {
            it.update("INSERT INTO WORDS (WORD, COUNT) VALUES ('d', 10)")
        }
        val found = connection.query(
            "SELECT * FROM WORDS WHERE WORD = 'd'"
        ) { rs -> rs.getString(1) }
        assertThat(found).hasSize(1)
    }

    @Test
    fun `should rollback a transaction`() {
        var ran = false
        try {
            connection.transaction {
                it.update("INSERT INTO WORDS (WORD, COUNT) VALUES ('d', 10)")
                ran = true
                throw IllegalStateException()
            }
        } catch (e: Exception) {
        }
        assertThat(ran).isTrue()
        val found = connection.query(
            "SELECT * FROM WORDS WHERE WORD = 'd'"
        ) { rs -> rs.getString(1) }
        assertThat(found).hasSize(0)
    }

    @Test
    fun `batch update`() {
        val sql = "INSERT INTO WORDS (WORD, COUNT) VALUES (?, ?)"
        val one: Binder = {
            it.setString(1, "one")
            it.setInt(2, 10)
        }
        val two: Binder = {
            it.setString(1, "two")
            it.setInt(2, 20)
        }
        val three: Binder = {
            it.setString(1, "three")
            it.setInt(2, 30)
        }

        val rows = connection.update(sql, one, two, three)
        assertThat(rows).isEqualTo(arrayOf(1,1,1))

        val pairs = connection.query("SELECT * FROM WORDS") {
            Pair(it.getString(1), it.getInt(2))
        }

        assertThat(pairs).contains(Pair("one", 10), Pair("two", 20), Pair("three", 30))
    }
}
