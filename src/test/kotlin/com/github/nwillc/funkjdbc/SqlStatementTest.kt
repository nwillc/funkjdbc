package com.github.nwillc.funkjdbc

import com.github.nwillc.funkjdbc.testing.EmbeddedH2
import org.assertj.core.api.Assertions
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import java.sql.Connection

@ExtendWith(EmbeddedH2::class)
class SqlStatementTest {
    private lateinit var connection: Connection

    @BeforeEach
    fun setUp() {
        connection = EmbeddedH2.getConnection()
        Assertions.assertThat(connection).isNotNull
        connection.update("CREATE TABLE WORDS ( WORD CHAR(20) NOT NULL, COUNT INTEGER DEFAULT 0)")
        connection.update("INSERT INTO WORDS (WORD, COUNT) VALUES ('a', 1)")
        connection.update("INSERT INTO WORDS (WORD, COUNT) VALUES ('b', 2)")
        connection.update("INSERT INTO WORDS (WORD, COUNT) VALUES ('c', 10)")
    }

    @Test
    fun `should bind arguments`() {
        val sqlStatement = SqlStatement("SELECT * FROM WORDS WHERE COUNT < ?") {
            it.setInt(1, 5)
        }

        connection.query(sqlStatement, { rs -> rs.getString(1) }) {
            assertThat(it.count()).isEqualTo(2)
        }
    }

    @Test
    fun `should rebind arguments`() {
        data class SelectCountLTE(var value: Int = 0) :
            SqlStatement("SELECT * FROM WORDS WHERE COUNT <= ?") {
            override val bind: Binder = { it.setInt(1, value) }
        }

        val sql = SelectCountLTE(2)
        connection.query(sql, { rs -> rs.getString(1) }) {
            assertThat(it.count()).isEqualTo(2)
        }

        sql.value = 20
        connection.query(sql, { rs -> rs.getString(1) }) {
            assertThat(it.count()).isEqualTo(3)
        }
    }
}
