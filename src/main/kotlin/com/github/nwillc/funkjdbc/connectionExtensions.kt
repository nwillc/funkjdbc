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

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import java.sql.Connection
import java.sql.PreparedStatement
import java.sql.ResultSet

/**
 * A mapper function to extract a type from a ResultSet. No magic here, use JDBC's getXXX methods here.
 */
typealias Extractor<T> = (ResultSet) -> T

/**
 * Execute SQL statement on a JDBC [Connection] and returns a row count of effected rows.
 * @param sql A simple SQL statement.
 */
fun Connection.update(sql: String): Int = update(SqlStatement(sql))

/**
 * Execute SQL statement on a JDBC [Connection] and returns a row count of effected rows.
 * @param sqlStatement A SqlStatement allowing parameters.
 */
fun Connection.update(sqlStatement: SqlStatement): Int =
    sqlStatement(sqlStatement).use { it.executeUpdate() }

/**
 * Executes a SQL query on a JDBC [Connection] and extract results as a [List] of type T.
 * @param sql A simple SQL query.
 * @param extractor A function to extract type T from the rows.
 * @return The matching rows.
 */
fun <T> Connection.query(sql: String, extractor: Extractor<T>): List<T> = query(SqlStatement(sql), extractor)

/**
 * Executes a SQL query on a JDBC [Connection] and extract results as a [List] of type T.
 * @param sqlStatement A SqlStatement containing a query allowing for parameters.
 * @param extractor A function to extract type T from the rows.
 * @return The matching rows.
 */
fun <T> Connection.query(sqlStatement: SqlStatement, extractor: Extractor<T>): List<T> =
    runBlocking {
        mutableListOf<T>().apply {
            asFlow(sqlStatement, extractor).toList(this)
        }
    }

/**
 * Executes a SQL query JDBC [Connection] and extract results as a [Flow] of type T.
 * @param sql The SQL to execute.
 * @param extractor A function to extract type T from row.
 * @return A Flow of T.
 * @since 0.9.1
 */
fun <T> Connection.asFlow(sql: String, extractor: Extractor<T>): Flow<T> = asFlow(SqlStatement(sql), extractor)

/**
 * Executes a SQL query JDBC [Connection] and extract results as a [Flow] of type T.
 * @param sqlStatement The SQL to execute.
 * @param extractor A function to extract type T from row.
 * @return A Flow of T.
 * @since 0.9.1
 */
fun <T> Connection.asFlow(sqlStatement: SqlStatement, extractor: Extractor<T>): Flow<T> = flow {
    sqlStatement(sqlStatement).use { statement ->
        statement.executeQuery().use { rs ->
            while (rs.next()) {
                emit(extractor(rs))
            }
        }
    }
}

/**
 * Create a [PreparedStatement] from with the SQL and bindings provided by the [SqlStatement].
 * @param sqlStatement The [SqlStatement] providing the SQL and [Binder].
 */
fun Connection.sqlStatement(sqlStatement: SqlStatement): PreparedStatement =
    prepareStatement(sqlStatement.sql).apply { sqlStatement(this) }

/**
 * Perform operations on connection within a transaction. This function will, if any exception
 * occurs, rollback the transaction and pass up the exception. If no exception occurs the transaction
 * is committed.
 * @param block The code block to perform within the transaction.
 */
@SuppressWarnings("TooGenericExceptionCaught")
fun Connection.transaction(block: (connection: Connection) -> Unit) {
    overrideAutoCommit {
        try {
            block(this)
            commit()
        } catch (e: Exception) {
            rollback()
            throw e
        }
    }
}

/**
 * Override connections auto commit setting for the duration of a block, restoring it at completion.
 * @param enabled The value to override to.
 * @param block The block to perform with the setting in place.
 */
fun Connection.overrideAutoCommit(enabled: Boolean = false, block: (connection: Connection) -> Unit) {
    val priorAutoCommit = autoCommit
    autoCommit = enabled
    try {
        block(this)
    } finally {
        autoCommit = priorAutoCommit
    }
}
