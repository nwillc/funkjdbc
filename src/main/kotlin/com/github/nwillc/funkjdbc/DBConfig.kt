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
import java.sql.DriverManager

/**
 * JDBC Database configuration information.
 * @property driver The JDBC driver class.
 * @property host THe host of the server.
 * @property database The database/schema name.
 * @property port The port of the server.
 * @property user The user name to connect with.
 * @property password The password for the user.
 * @property toUrl A function to create the JDBC url from this.
 */
data class DBConfig(
    val driver: String,
    val host: String = "localhost",
    val database: String = "public",
    val port: Int = 0,
    val user: String = "sa",
    val password: String = "",
    val toUrl: (config: DBConfig) -> String
) {
    val url: String
        get() = toUrl(this)

    /**
     * Return a connection for this [DBConfig].
     * @return A JDBC [Connection] based on this configuration.
     */
    fun getConnection(): Connection {
        Class.forName(driver)
        return DriverManager.getConnection(url, user, password)!!
    }
}
