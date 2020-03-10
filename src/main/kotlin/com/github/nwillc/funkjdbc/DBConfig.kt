package com.github.nwillc.funkjdbc

data class DBConfig(
    val driver: String,
    val host: String = "localhost",
    val database: String = "public",
    val port: Int = 0,
    val user: String = "sa",
    val password: String = ""
)
