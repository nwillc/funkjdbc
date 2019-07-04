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

import java.sql.Connection
import java.sql.ResultSet

/**
 * A function to extract a type from a ResultSet. No magic here, use JDBC's getXXX methods here.
 */
typealias Extractor<T> = (ResultSet) -> T

/** A function accepting a sequence of type T to allow processing and returning the results. */
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
 * Execute a SQL query on a JDBC Connection. The SQL is expected to be a query
 * and therefore returns a result set that the extractor can extract type T from the rows,
 * and the resultsProcessor can then process them.
 * @param sqlStatement A SqlStatement containing a query allowing for parameters.
 * @param extractor A function to extract type T from the rows.
 * @param resultsProcessor A function to process rows after extraction.
 */
fun <T, R> Connection.query(sqlStatement: SqlStatement, extractor: Extractor<T>, resultsProcessor: ResultsProcessor<T, R>) =
    prepareStatement(sqlStatement.sql).use { statement ->
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
 * A convenience function to execute a SQL query on a JDBC Connection. The SQL is expected to be a query
 * with the aim of retrieving matching rows. This could also be achieved with query and an appropriate
 * results processor.
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
