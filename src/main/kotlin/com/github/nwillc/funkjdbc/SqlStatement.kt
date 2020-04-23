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
 */

package com.github.nwillc.funkjdbc

import java.sql.PreparedStatement

/** A function that accepts a PreparedStatement and binds the values to the '?'s in the previously declared SQL. */
typealias Binder = (PreparedStatement) -> Unit

/**
 * A SQL string, with a binding block in needed to help with JDBC [PreparedStatement]. The sql String
 * excepts JDBC '?' value replacement syntax, and the binding block allows you to bind values
 * to those '?'s on a given PreparedStatement.
 *
 * In the simplest case:
 * ```
 * val sql = SqlStatement("SELECT * FROM WORDS")
 * ```
 *
 * An example with bindings:
 * ```
 * val sql = SqlStatement("SELECT * FROM WORDS WHERE COUNT < ?") {
 *   it.setInt(1, someValue)
 * }
 * ```
 *
 * @property sql The SQL statement, with JDBC bindings allowed.
 * @property binder The optional binding code block to bind values to SQL's '?'s
 */
data class SqlStatement(val sql: String, val binder: Binder? = null) {
    /**
     * Make the default behavior of the [SqlStatement] to be to apply the [Binder] to the SQL.
     * @param preparedStatement The [PreparedStatement] with the SQL and [Binder].
     */
    operator fun invoke(preparedStatement: PreparedStatement) =
        preparedStatement.apply { binder?.invoke(preparedStatement) }
}
