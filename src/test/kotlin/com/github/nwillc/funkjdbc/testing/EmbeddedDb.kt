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

package com.github.nwillc.funkjdbc.testing

import com.github.nwillc.funkjdbc.update
import org.junit.jupiter.api.extension.AfterEachCallback
import org.junit.jupiter.api.extension.BeforeEachCallback
import org.junit.jupiter.api.extension.ExtensionContext
import java.io.File
import java.sql.DriverManager

class EmbeddedDb : BeforeEachCallback, AfterEachCallback {
    companion object {
        private val url = "jdbc:h2:mem:test"
        private val driver = "org.h2.Driver"
        private val user = "sa"
        private val password = ""
        private val migrations = "src/test/resources/db/migrations"
    }

    override fun beforeEach(context: ExtensionContext) {
        Class.forName(driver)
        val connection = DriverManager.getConnection(url, user, password)!!
        File(migrations)
            .walk()
            .filter { it.isFile && it.path.endsWith(".sql", true) }
            .sorted()
            .forEach {
                connection.update(it.readText())
            }
        (context.requiredTestInstance as WithConnection).connection = connection
    }

    override fun afterEach(context: ExtensionContext) {
        (context.requiredTestInstance as WithConnection).connection.close()
    }
}
