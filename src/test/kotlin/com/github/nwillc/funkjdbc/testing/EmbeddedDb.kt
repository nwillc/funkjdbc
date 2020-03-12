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

package com.github.nwillc.funkjdbc.testing

import com.github.nwillc.funkjdbc.DBConfig
import com.github.nwillc.funkjdbc.Sql
import com.github.nwillc.funkjdbc.sqlFor
import com.github.nwillc.funkjdbc.update
import org.junit.jupiter.api.extension.AfterEachCallback
import org.junit.jupiter.api.extension.BeforeEachCallback
import org.junit.jupiter.api.extension.ExtensionContext
import org.junit.jupiter.api.extension.ParameterContext
import org.junit.jupiter.api.extension.ParameterResolver
import java.sql.Connection
import java.util.logging.Logger

class EmbeddedDb : ParameterResolver, BeforeEachCallback, AfterEachCallback {
    private var dbConfig = DBConfig(
        driver = "org.h2.Driver"
    ) { config -> "jdbc:h2:mem:${config.database}" }
    private lateinit var connection: Connection

    override fun supportsParameter(parameterContext: ParameterContext?, extensionContext: ExtensionContext?): Boolean {
        return DBConfig::class.java.isAssignableFrom(parameterContext!!.parameter.type)
    }

    override fun resolveParameter(parameterContext: ParameterContext?, extensionContext: ExtensionContext?): Any {
        return dbConfig
    }

    override fun beforeEach(context: ExtensionContext) {
        connection = dbConfig.getConnection()
        context.requiredTestInstance.sqlFor(Sql.ExecutionPhase.SETUP)
            .forEach {
                logger.fine("Running $it")
                connection.update(it.readText())
            }
    }

    override fun afterEach(context: ExtensionContext) {
        context.requiredTestInstance.sqlFor(Sql.ExecutionPhase.TEARDOWN)
            .forEach {
                logger.fine("Running $it")
                connection.update(it.readText())
            }
        connection.close()
    }

    companion object {
        private val logger = Logger.getLogger(EmbeddedDb::class.java.simpleName)
    }
}
