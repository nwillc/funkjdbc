[![Coverage](https://codecov.io/gh/nwillc/funkjdbc/branch/master/graphs/badge.svg?branch=master)](https://codecov.io/gh/nwillc/funkjdbc)
[![license](https://img.shields.io/github/license/nwillc/funkjdbc.svg)](https://tldrlegal.com/license/-isc-license)
[![Travis](https://img.shields.io/travis/nwillc/funkjdbc.svg)](https://travis-ci.org/nwillc/funkjdbc)
[![Download](https://api.bintray.com/packages/nwillc/maven/funkjdbc/images/download.svg)](https://bintray.com/nwillc/maven/funkjdbc/_latestVersion)
------

# Functional Kotlin JDBC Extensions

Nothing exciting but...

 - Row to object mapping with first order functions.
 - Simplified JDBC’s bloated Java API into clean Kotlin for common use cases:
   - update, find, transaction
 - Provide results as a List or Kotlin Flow.
 - Thoroughly handled resource closing to remove boiler plate code.
 - Supports raw String SQL as well as JDBC’s ? replacements.
 - Simple.
    - Implemented as a half dozen Connection extensions.
    - Under 150 lines of code.
    - No transitive dependencies (>30k binary).
 - Documented.

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

Having created the table and added some rows, now you want to display them:

```kotlin
fun pairExtractor(rs: ResultSet) = Pair(rs.getString("WORD")!!,rs.getInt("COUNT"))

connection.find("SELECT * FROM WORDS", ::pairExtractor).forEach {
   println("Word: ${it.first} Count: ${it.second}")
}
```

Or maybe put them in a Map:

```kotlin
val map = connection.find("SELECT * FROM WORDS", ::pairExtractor).toMap()
```

Or create a parameterized query based on the counts:

```kotlin
data class SelectCountLTE(var value: Int = 0) :
  SqlStatement("SELECT * FROM WORDS WHERE COUNT <= ?") {
  override val bind: Binder = { it.setInt(1, value) }
}

val sql = SelectCountLTE(1)
connection.find(sql, {rs -> rs.getInt("count") } ).forEach { println("$it <= ${sql.value}") }

sql.value = 200
connection.find(sql, {rs -> rs.getInt("count") } ).forEach { println("$it <= ${sql.value}") }

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
## See Also

- [API Docs](https://nwillc.github.io/funkjdbc/dokka/funkjdbc/index.html)
