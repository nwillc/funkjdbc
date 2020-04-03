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
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension
import java.sql.Connection

@Sql("src/test/resources/db/migrations")
class SqlStatementTest {
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
    fun `should bind arguments`() {
        val sqlStatement = SqlStatement("SELECT * FROM WORDS WHERE COUNT < ?") {
            it.setInt(1, 5)
        }

        val count = connection.find(sqlStatement) { rs -> rs.getString(1) }.count()
        assertThat(count).isEqualTo(2)
    }

    @Test
    fun `should rebind arguments`() {
        class SelectCountLTE(var value: Int = 0) :
            SqlStatement("SELECT * FROM WORDS WHERE COUNT <= ?") {
            override val bind: Binder = { it.setInt(1, value) }
        }

        val sql = SelectCountLTE(2)
        assertThat(connection.find(sql) { rs -> rs.getString(1) }.count()).isEqualTo(2)

        sql.value = 20
        assertThat(connection.find(sql) { rs -> rs.getString(1) }.count()).isEqualTo(3)
    }

    companion object {
        @JvmField
        @RegisterExtension
        val embeddedDb = EmbeddedDb(
            DBConfig(driver = "org.sqlite.JDBC") { config -> "jdbc:sqlite:${config.database}" }
        )
    }
}
