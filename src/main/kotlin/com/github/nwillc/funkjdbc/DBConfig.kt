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
    /**
     * Return a connection for this [DBConfig].
     * @return A JDBC [Connection] based on this configuration.
     */
    fun getConnection(): Connection {
        Class.forName(driver)
        return DriverManager.getConnection(toUrl(this), user, password)!!
    }
}
