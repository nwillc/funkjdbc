package com.github.nwillc.funkjdbc

import java.sql.PreparedStatement

/**
 * A SQL string, with a binding block. The sql String excepts JDBC '?' value replacement syntax,
 * and the binding block allows you to bind values to those '?'s on a given PreparedStatement.
 */
class SqlStatement(val sql: String, val bind: (PreparedStatement) -> Unit = {})
