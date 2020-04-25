[![Coverage](https://codecov.io/gh/nwillc/funkjdbc/branch/master/graphs/badge.svg?branch=master)](https://codecov.io/gh/nwillc/funkjdbc)
[![license](https://img.shields.io/github/license/nwillc/funkjdbc.svg)](https://tldrlegal.com/license/-isc-license)
[![Build Status](https://github.com/nwillc/funkjdbc/workflows/CICD/badge.svg)](https://github.com/nwillc/funkjdbc/actions?query=workflow%3ACICD)
[![Download](https://api.bintray.com/packages/nwillc/maven/funkjdbc/images/download.svg)](https://bintray.com/nwillc/maven/funkjdbc/_latestVersion)
------

# Functional Kotlin JDBC Extensions

Nothing exciting but...

 - Row to object mapping and query parameter binding via Lambdas.
 - Simplified JDBC’s API for common use cases:
   - update, query, transaction
 - Provide results as a List or Kotlin Flow.
 - Thoroughly handled resource closing to remove boiler plate code.
 - Supports raw String SQL as well as JDBC’s ? replacements.
 - Simple.
    - Implemented largely as JDBC Connection extensions.
    - Under 150 source lines of code.
    - No transitive dependencies (>30k binary).
 - Documented.
 - A testing artifact with JUnit 5 support for an embedded database.

## Using These Extensions

Assuming you've a JDBC Connection and wish to create a database to store word counts. Lets create
that table:

```kotlin
 connection.update("CREATE TABLE WORDS ( WORD CHAR(20) NOT NULL, COUNT INTEGER DEFAULT 0)")
```

Now you want to add a row noting 10 occurrences of the word `foo`:

```kotlin
 connection.update("INSERT INTO WORDS (WORD, COUNT) VALUES ('foo', 10)")
```

Having created the table and added some rows, maybe you want to see the words:

```kotlin
val words = connection.query("SELECT WORD FROM WORDS") { rs -> rs.getString(1) }
```

Or you want to display them as Pairs:

```kotlin
val pairExtractor: Extractor<Pair<String,Int>> = { rs -> Pair(rs.getString("WORD")!!,rs.getInt("COUNT")) }

connection.query("SELECT * FROM WORDS", pairExtractor).forEach {
   println("Word: ${it.first} Count: ${it.second}")
}
```

Or maybe put them in a Map:

```kotlin
val map = connection.query("SELECT * FROM WORDS", pairExtractor).toMap()
```

Or create a parameterized query based on the counts:

```kotlin
val sqlStatement = SqlStatement("SELECT * FROM WORDS WHERE COUNT < ?") {
    it.setInt(1, 5)
}

val count = connection.query(sqlStatement) { rs -> rs.getString(1) }.count()
```

Additionally, operations can be performed in a transaction:

```kotlin
try {
  connection.transaction {
    it.update("INSERT INTO WORDS (WORD, COUNT) VALUES ('foo', 10)")
    it.update("INSERT INTO WORDS (WORD, COUNT) VALUES ('foobar', 10)")
  }
} catch (e: Exception) {
  println("Transaction failed: $e")
}
```

## The Testing Jar

This code is tested in JUnit 5 with an embedded database. I've made that code available separately here. It
can be used with this framework, or any JDBC code that expects a JDBC Connection.  To get version `0.12.0` of
the test artifact for example use `com.github.nwillc:funkjdbc:0.12.0:test`. In a Gradle Kotlin DSL build this
would look like:

````kotlin
    testImplementation("com.github.nwillc:funkjdbc:0.12.0:test")
````

To employ it in a test:

```kotlin
@Sql("src/test/resources/db/migrations")
@ExtendWith(EmbeddedDb::class)
class ConnectionExtensionsTest {
    private lateinit var connection: Connection

    @BeforeEach
    fun setUp(dbConfig: DBConfig) {
        connection = dbConfig.getConnection()
    }

    @Test
    fun selectTest() {
        connection.query("SELECT count(*) FROM TABLE")
    }
}
```

The embedded database, H2 by default, will spin up before each test, run the SQL scripts indicated in the
`@Sql` annotation, and pass its `DBConfig` to your `@BeforeEach` so that you can get a Connection to it. It's
a very simple mechanism and has been tested with H2, Sqlite and even a Postgres test container.

## See Also

- [API Docs](https://nwillc.github.io/funkjdbc/dokka/funkjdbc/index.html)
