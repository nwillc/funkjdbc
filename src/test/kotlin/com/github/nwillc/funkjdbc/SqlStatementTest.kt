package com.github.nwillc.funkjdbc

import com.github.nwillc.funkjdbc.testing.WithConnection
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class SqlStatementTest : WithConnection() {

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
