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

import java.sql.Connection
import java.sql.ResultSet
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

/**
 * A function to extract a type from a ResultSet. No magic here, use JDBC's getXXX methods here.
 */
typealias Extractor<T> = (ResultSet) -> T

/** A function accepting a sequence of type T to allow processing and returning the results R. */
typealias ResultsProcessor<T, R> = (Sequence<T>) -> R

/**
 * Execute a SQL statement on a JDBC Connection. The SQL is expected to be a statement
 * that updates the database, and therefore returns a row count.
 * @param sql A simple SQL statement.
 */
fun Connection.update(sql: String): Int = createStatement().use { it.executeUpdate(sql) }

/**
 * Execute a SQL statement on a JDBC Connection. The SQL is expected to be a statement
 * that updates the database, and therefore returns a row count.
 * @param sqlStatement A SqlStatement allowing parameters.
 */
fun Connection.update(sqlStatement: SqlStatement): Int = prepareStatement(sqlStatement.sql).use {
    sqlStatement.bind(it)
    it.executeUpdate()
}

/**
 * Execute a SQL query on a JDBC Connection. The SQL is expected to be a query
 * and therefore returns a result set that the extractor can extract type T from the rows,
 * and the resultsProcessor can then process them.
 * @param sql A simple SQL query.
 * @param extractor A function to extract type T from the rows.
 * @param resultsProcessor A function to process rows after extraction.
 */
fun <T, R> Connection.query(sql: String, extractor: Extractor<T>, resultsProcessor: ResultsProcessor<T, R>) =
    createStatement().use { statement ->
        ResultSetIterator(statement.executeQuery(sql), extractor).use { rs -> resultsProcessor(rs.asSequence()) }
    }

/**
 * Execute SQL query on a JDBC Connection. The SQL is expected to be a query
 * and therefore returns a result set that the extractor can extract type T from the rows,
 * and the resultsProcessor can then process them.
 * @param sqlStatement A SqlStatement containing a query allowing for parameters.
 * @param extractor A function to extract type T from the rows.
 * @param resultsProcessor A function to process rows after extraction.
 */
fun <T, R> Connection.query(
    sqlStatement: SqlStatement,
    extractor: Extractor<T>,
    resultsProcessor: ResultsProcessor<T, R>
) = prepareStatement(sqlStatement.sql).use { statement ->
    sqlStatement.bind(statement)
    ResultSetIterator(statement.executeQuery(), extractor).use { rs -> resultsProcessor(rs.asSequence()) }
}

/**
 * A convenience function to execute a SQL query on a JDBC Connection. The SQL is expected to be a query
 * with the aim of retrieving matching rows. This could alseo be achieved with query and an appropriate
 * results processor.
 * @param sql A simple SQL query.
 * @param extractor A function to extract type T from the rows.
 * @return The matching rows.
 */
fun <T> Connection.find(sql: String, extractor: Extractor<T>): List<T> =
    createStatement().use { statement ->
        ResultSetIterator(statement.executeQuery(sql), extractor).use { rs ->
            rs.asSequence().toList()
        }
    }

/**
 * A convenience function to execute a SQL query on a JDBC Connection. The SQL is a query
 * with the aim of retrieving matching rows. This could also be achieved with query and an appropriate
 * result processor.
 * @param sqlStatement A SqlStatement containing a query allowing for parameters.
 * @param extractor A function to extract type T from the rows.
 * @return The matching rows.
 */
fun <T> Connection.find(sqlStatement: SqlStatement, extractor: Extractor<T>): List<T> =
    prepareStatement(sqlStatement.sql).use { statement ->
        sqlStatement.bind(statement)
        ResultSetIterator(statement.executeQuery(), extractor).use { rs ->
            rs.asSequence().toList()
        }
    }

/**
 * Takes SQL string and extractor and returns a [Flow] of T of the resultant rows.
 * @param sql The SQL to execute.
 * @param extractor A function to extract type T from row.
 * @return A Flow of T.
 * @since 0.9.1
 */
fun <T> Connection.asFlow(sql: String, extractor: Extractor<T>): Flow<T> = flow {
    createStatement().use { statement ->
        statement.executeQuery(sql).use {
            while (it.next()) {
                emit(extractor(it))
            }
        }
    }
}

/**
 * Takes [SqlStatement] and extractor and returns a [Flow] of T of the resultant rows.
 * @param sqlStatement The SQL to execute.
 * @param extractor A function to extract type T from row.
 * @return A Flow of T.
 * @since 0.9.1
 */
fun <T> Connection.asFlow(sqlStatement: SqlStatement, extractor: Extractor<T>): Flow<T> = flow {
    prepareStatement(sqlStatement.sql).use { statement ->
        sqlStatement.bind(statement)
        statement.executeQuery().use {
            while (it.next()) {
                emit(extractor(it))
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
