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
fun interface Binder {
    operator fun invoke(preparedStatement: PreparedStatement)
}

/**
 * A SQL string, with a binding block to help with JDBC PreparedStatement. The sql String
 * excepts JDBC '?' value replacement syntax, and the binding block allows you to bind values
 * to those '?'s on a given PreparedStatement.
 *
 * A simple use might be:
 * ```
 * val sql = SqlStatement("SELECT * FROM WORDS WHERE COUNT < ?") {
 *   it.setInt(1, someValue)
 * }
 * ```
 * However, this class, and the bind property are `open`. This is done to allow subclassing to create specific
 * queries with typed arguments:
 *
 * ```
 * data class SelectCountLTE(var value: Int = 0) :
 *   SqlStatement("SELECT * FROM WORDS WHERE COUNT <= ?") {
 *    override val bind: Binder = { it.setInt(1, value) }
 * }
 * ```
 * To allow uses like:
 * ```
 * val sql = SelectCountLTE(1)
 * connection.query(sql, {rs -> rs.getInt("count") } ) { it.forEach { println("$it <= 1") } }
 *
 * sql.value = 200
 * connection.query(sql, {rs -> rs.getInt("count") } ) { it.forEach { println("$it <= 200") } }
 * ```
 *
 * @property sql The JDBC formatted SQL statements
 * @property bind The binding code block to bind values to SQL's '?'s
 */
class SqlStatement(val sql: String, val binder: Binder)
