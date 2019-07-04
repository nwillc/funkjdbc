# Functional Kotlin JDBC Extensions

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

Or better yet, add a row where the word and count depend on properties:

```kotlin
val word = "bar"
val count = 25
connection.update("INSERT INTO WORDS (WORD, COUNT) VALUES (?, ?)") {
  it.setString(1, word)
  it.setInt(2, count)  
}
```

Having created the table and added some rows, now you want to read rows our as Pairs, and put
them in a Map:

```kotlin
fun pairExtractor(rs: ResultSet) = Pair(rs.getString("WORD")!!,rs.getInt("COUNT"))

val map = connection.query("SELECT * FROM WORDS", ::pairExtractor) {
   it.toMap()
}
```
## See Also

- [API Docs](https://nwillc.github.io/funkjdbc/dokka/funkjdbc/com.github.nwillc.funkjdbc/index.html)
