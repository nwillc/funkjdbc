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
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import java.sql.Connection

@ExtendWith(EmbeddedH2::class)
internal class ResultSetIteratorTest {
    private lateinit var connection: Connection

    @BeforeEach
    fun setup() {
        connection = EmbeddedH2.getConnection()
        assertThat(connection).isNotNull
        connection.update("CREATE TABLE WORDS ( WORD CHAR(20) )")
        connection.update("INSERT INTO WORDS (WORD) VALUES ('a')")
        connection.update("INSERT INTO WORDS (WORD) VALUES ('boo')")
    }

    @Test
    fun `should iterate the results of a query`() {
        connection.query("SELECT * FROM WORDS", { rs -> rs.getString(1) }) {
            assertThat(it.toList()).containsAll(listOf("a", "boo"))
        }
    }
}
