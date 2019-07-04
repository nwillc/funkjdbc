package com.github.nwillc.funkjdbc.testing

import org.junit.jupiter.api.extension.AfterEachCallback
import org.junit.jupiter.api.extension.BeforeEachCallback
import org.junit.jupiter.api.extension.ExtensionContext
import java.sql.Connection
import java.sql.DriverManager

class EmbeddedH2 : BeforeEachCallback, AfterEachCallback {
    companion object {
        private var connection: Connection? = null
        fun getConnection(): Connection = connection!!
    }

    override fun beforeEach(context: ExtensionContext?) {
        val url = "jdbc:h2:mem:"

        connection = DriverManager.getConnection(url)
    }

    override fun afterEach(context: ExtensionContext?) {
        if (connection != null) {
            connection!!.close()
        }
    }
}
