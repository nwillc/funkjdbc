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

import com.github.nwillc.funkjdbc.testing.Sql
import com.github.nwillc.funkjdbc.testing.WithConnection
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.assertj.core.api.Assertions.entry
import org.junit.jupiter.api.Test
import java.sql.ResultSet
import java.sql.SQLException

@Sql("src/test/resources/db/migrations")
class ConnectionExtensionsKtTest : WithConnection() {

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
            connection.query("blah blah", { rs -> rs.getInt(1) }) {}
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
    fun `should be able to query`() {
        connection.query("SELECT count(*) FROM WORDS", { rs -> rs.getInt(1) }) {
            assertThat(it.first()).isEqualTo(3)
        }
    }

    @Test
    fun `should be able to query with late bound arguments`() {
        var word = "a"
        val sqlStatement = SqlStatement("SELECT count(*) FROM WORDS WHERE WORD = ?") {
            it.setString(1, word)
        }
        connection.query(sqlStatement, { rs -> rs.getInt(1) }) {
            assertThat(it.first()).isEqualTo(1)
        }
        word = "foo"
        connection.query(sqlStatement, { rs -> rs.getInt(1) }) {
            assertThat(it.first()).isEqualTo(0)
        }
    }

    @Test
    fun `should be able to find a record`() {
        val found = connection.find("SELECT * FROM WORDS WHERE WORD = 'a'") { rs ->
            rs.getString(1)
        }

        assertThat(found.first()).isEqualTo("a")
    }

    @Test
    fun `should be able to not find a record`() {
        val found = connection.find("SELECT * FROM WORDS WHERE WORD = 'd'") { rs ->
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
    fun `should be able to find with params as flow`() {
        val word = "a"
        val sqlStatement = SqlStatement("SELECT * FROM WORDS WHERE WORD != ?") {
            it.setString(1, word)
        }
        runBlocking {
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
        val found = connection.find(sql) { rs ->
            rs.getString(1)
        }

        assertThat(found.first()).isEqualTo("a")
    }

    @Test
    fun `should be able to find a record with query arguments and closing connection`() {
        val sql = SqlStatement("SELECT * FROM WORDS WHERE WORD = ?") {
            it.setString(1, "a")
        }
        connection.use {
            val found = it.find(sql) { rs ->
                rs.getString(1)
            }

            assertThat(found.first()).isEqualTo("a")
        }
    }

    @Test
    fun `should be able to not find a record with query arguments`() {
        val sql = SqlStatement("SELECT * FROM WORDS WHERE WORD = ?") {
            it.setString(1, "d")
        }
        val found = connection.find(sql) { rs ->
            rs.getString(1)
        }

        assertThat(found).isEmpty()
    }

    @Test
    fun `should be able you use result of query processor`() {
        fun pairExtractor(rs: ResultSet) = Pair(
            rs.getString("WORD")!!,
            rs.getInt("COUNT")
        )

        val map = connection.query("SELECT * FROM WORDS", ::pairExtractor) { it.toMap() }
        assertThat(map).containsExactly(entry("a", 1), entry("b", 2), entry("c", 10))
    }

    @Test
    fun `should commit a transaction`() {
        connection.transaction {
            it.update("INSERT INTO WORDS (WORD, COUNT) VALUES ('d', 10)")
        }
        val found = connection.find(
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
        assertThat(ran).isTrue
        val found = connection.find(
            "SELECT * FROM WORDS WHERE WORD = 'd'"
        ) { rs -> rs.getString(1) }
        assertThat(found).hasSize(0)
    }
}
