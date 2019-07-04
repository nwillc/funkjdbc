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

import java.sql.ResultSet

/**
 * This is an Iterator that iterates over a ResultSet returning elements with an Extractor. Additionally this
 * implements AutoCloseable.
 *
 * @property resultSet The ResultSet to iterate over.
 * @property extractor The function to extract type T from a row in ResultSet.
 */
class ResultSetIterator<T>(private val resultSet: ResultSet, private val extractor: Extractor<T>) :
    Iterator<T>, AutoCloseable {
    private var nextAvailable: Boolean? = null

    /** Returns true if the iteration has more elements. */
    override fun hasNext(): Boolean {
        if (nextAvailable == null) {
            nextAvailable = resultSet.next()
        }

        return nextAvailable!!
    }

    /**
     * Returns the next element in the iteration. Applies the extractor to the result set to
     * derive the element.
     */
    override fun next(): T =
        if (!hasNext()) {
            throw NoSuchElementException()
        } else {
            nextAvailable = null
            extractor(resultSet)
        }

    /**
     * Closes this resource, relinquishing any underlying resources.
     */
    override fun close() {
        resultSet.close()
    }
}
