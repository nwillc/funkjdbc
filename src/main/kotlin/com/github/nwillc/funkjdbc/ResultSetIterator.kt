package com.github.nwillc.funkjdbc

import java.sql.ResultSet

/**
 * This is an Iterator that iterates over a ResultSet returning elements with an Extractor. Additionally this
 * implements AutoCloseable.
 *
 * @param <T> The type of elements being extracted.
 * @param resultSet The ResultSet to iterate over.
 * @param extractor The function to extract type T from a row in ResultSet.
 */
class ResultSetIterator<T>(private val resultSet: ResultSet, private val extractor: Extractor<T>) :
    Iterator<T>, AutoCloseable {
    private var nextAvailable: Boolean? = null

    override fun hasNext(): Boolean {
        if (nextAvailable == null) {
            nextAvailable = resultSet.next()
        }

        return nextAvailable!!
    }

    override fun next(): T =
        if (!hasNext()) {
            throw NoSuchElementException()
        } else {
            nextAvailable = null
            extractor(resultSet)
        }

    override fun close() {
        resultSet.close()
    }
}
