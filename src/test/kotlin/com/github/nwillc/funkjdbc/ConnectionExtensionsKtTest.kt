/*
 * Copyright (c) 2019, nwillc@gmail.com
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

import com.github.nwillc.funkjdbc.testing.EmbeddedH2
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.assertj.core.api.Assertions.entry
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import java.sql.Connection
import java.sql.ResultSet
import java.sql.SQLException

@ExtendWith(EmbeddedH2::class)
class ConnectionExtensionsKtTest {
    private lateinit var connection: Connection

    @BeforeEach
    fun setup() {
        connection = EmbeddedH2.getConnection()
        assertThat(connection).isNotNull
        connection.update("CREATE TABLE WORDS ( WORD CHAR(20), COUNT INTEGER DEFAULT 0)")
        connection.update("INSERT INTO WORDS (WORD, COUNT) VALUES ('a', 1)")
        connection.update("INSERT INTO WORDS (WORD, COUNT) VALUES ('b', 2)")
        connection.update("INSERT INTO WORDS (WORD, COUNT) VALUES ('c', 10)")
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
    internal fun `should be able you use a Pair extractor to create a Map`() {
        fun pairExtractor(rs: ResultSet) = Pair(
            rs.getString("WORD"),
            rs.getInt("COUNT")
        )

        connection.query("SELECT * FROM WORDS", ::pairExtractor) {
            val map = it.toMap()
            assertThat(map).containsExactly(entry("a", 1), entry("b", 2), entry("c", 10))
        }

    }
}
