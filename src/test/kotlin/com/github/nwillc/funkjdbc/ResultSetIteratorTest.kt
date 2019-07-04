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
