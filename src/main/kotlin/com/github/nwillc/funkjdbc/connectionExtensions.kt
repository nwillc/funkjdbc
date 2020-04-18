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

/**
 * Execute a SQL statement on a JDBC Connection. The SQL is a statement
 * that updates the database, and therefore returns a row count.
 * @param sql A simple SQL statement.
 */
fun Connection.update(sql: String): Int = createStatement().use { it.executeUpdate(sql) }

/**
 * Execute SQL statement on a JDBC [Connection]. The SQL is a statement
 * that updates the database, and therefore returns a row count.
 * @param sqlStatement A SqlStatement allowing parameters.
 */
fun Connection.update(sqlStatement: SqlStatement): Int = prepareStatement(sqlStatement.sql).use { statement ->
    sqlStatement.bind(statement)
    statement.executeUpdate()
}

/**
 * Execute SQL on a JDBC [Connection] and extract results. The SQL is a query
 * with the aim of retrieving a [List] of type T.
 * @param sql A simple SQL query.
 * @param extractor A function to extract type T from the rows.
 * @return The matching rows.
 */
fun <T> Connection.find(sql: String, extractor: Extractor<T>): List<T> =
    runBlocking {
        mutableListOf<T>().apply {
            asFlow(sql, extractor).toList(this)
        }
    }

/**
 * Execute SQL on a JDBC [Connection] and extract results. The SQL is a query
 * with the aim of retrieving a [List] of type T.
 * @param sqlStatement A SqlStatement containing a query allowing for parameters.
 * @param extractor A function to extract type T from the rows.
 * @return The matching rows.
 */
fun <T> Connection.find(sqlStatement: SqlStatement, extractor: Extractor<T>): List<T> =
    runBlocking {
        mutableListOf<T>().apply {
            asFlow(sqlStatement, extractor).toList(this)
        }
    }

/**
 * Execute SQL on a JDBC [Connection] and extract results. The SQL is a query
 * with the aim of retrieving a [Flow] of type T.
 * @param sql The SQL to execute.
 * @param extractor A function to extract type T from row.
 * @return A Flow of T.
 * @since 0.9.1
 */
fun <T> Connection.asFlow(sql: String, extractor: Extractor<T>): Flow<T> = flow {
    createStatement().use { statement ->
        statement.executeQuery(sql).use { rs ->
            while (rs.next()) {
                emit(extractor(rs))
            }
        }
    }
}

/**
 * Execute SQL on a JDBC [Connection] and extract results. The SQL is a query
 * with the aim of retrieving a [Flow] of type T.
 * @param sqlStatement The SQL to execute.
 * @param extractor A function to extract type T from row.
 * @return A Flow of T.
 * @since 0.9.1
 */
fun <T> Connection.asFlow(sqlStatement: SqlStatement, extractor: Extractor<T>): Flow<T> = flow {
    prepareStatement(sqlStatement.sql).use { statement ->
        sqlStatement.bind(statement)
        statement.executeQuery().use { rs ->
            while (rs.next()) {
                emit(extractor(rs))
            }
        }
    }
}

/**
 * Perform operations on connection within a transaction. This function will, if any exception
 * occurs, rollback the transaction and pass up the exception. If no exception occurs the transaction
 * is committed.
 * @param block The code block to perform within the transaction.
 */
@SuppressWarnings("TooGenericExceptionCaught")
fun Connection.transaction(block: (connection: Connection) -> Unit) {
    val priorAutoCommit = autoCommit
    autoCommit = false
    try {
        block(this)
        commit()
    } catch (e: Exception) {
        rollback()
        throw e
    } finally {
        autoCommit = priorAutoCommit
    }
}
