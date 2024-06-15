# ExposedFlows

This project aims to provide flows support for [Exposed](https://github.com/JetBrains/Exposed).

## Functionalities

### Retrieving a flow

You can retrieve a flow on a `Query` with the `asFlow()` extension function.
The `asFlow()` function returns a `List<ResultRow>`. 
You can map the `ResultRow` elements with the `mapResultRow()` function.

```kotlin
fun getAll(): Flow<List<User>> = transaction {
        UserTable
            .selectAll()
            .asFlow()
            .mapResultRow { it.toUser() }
    }
```
```kotlin
fun getFromId(id: UUID): Flow<User?> = transaction {
    UserTable
        .selectAll()
        .where { UserTable.id eq id.toString() }
        .asFlow()
        .mapResultRow { it.toUser() }
        .map { it.firstOrNull() }
}
```

### Updating tables

When modifying a table, the flows that depend on the table value should emit updated values.
For this, when modifying a table, you should use the `flowTransactionOn` method, used to replace the `transaction` one.

It takes as argument the tables used by the system to update the concerned flows.
```kotlin
suspend fun insert(user: User) {
    flowTransactionOn(UserTable) {
        UserTable.insert {
            it[id] = user.id.toString()
            it[name] = user.name
            it[age] = user.age
        }
    }
}
```
```kotlin
suspend fun deleteById(id: UUID) {
    flowTransactionOn(UserTable, DogTable) {
        UserTable.deleteWhere {
            UserTable.id eq id.toString()
        }
        DogTable.deleteWhere {
            DogTable.userId eq id.toString()
        }
    }
}
```