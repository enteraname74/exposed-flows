import com.github.enteraname74.exposedflows.flowTransactionOn
import dog.*
import dog.dbQuery
import hat.Hat
import hat.HatTable
import hat.toNullableHat
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction
import user.User
import user.UserDao
import user.UserTable
import user.toUser
import java.util.*

suspend fun main() {
    AppDatabase.connectToDatabase()
    test()
    automaticFlowTransactionTest()
    transaction()
}

suspend fun test() {
    val userDao = UserDao()
    val dogDao = DogDao()

    val userId = UUID.randomUUID()

    CoroutineScope(Dispatchers.IO).launch {
        userDao.getFromId(userId).collect {
            println("Got from id ? $it")
        }
    }

    CoroutineScope(Dispatchers.IO).launch {
        userDao.getAll().collect {
            println("ALL USERS: $it")
        }
    }

    CoroutineScope(Dispatchers.IO).launch {
        dogDao.getAll().collect {
            println("ALL DOGS: $it")
        }
    }

    val job1 = CoroutineScope(Dispatchers.IO).launch {
        delay(3000)
        println("FIRST")
        val user = User(
            id = userId,
            name = "Jack",
            age = 12
        )
        val dog = Dog(
            id = UUID.randomUUID(),
            userId = userId,
            name = "Doggo"
        )
        userDao.insert(user)
        dogDao.insert(dog)
        delay(1000)
        userDao.deleteById(userId)
        delay(3000)
    }
    joinAll(job1)
}

suspend fun automaticFlowTransactionTest() {
    flowTransactionOn {
        val userId = UUID.randomUUID()
        val user = User(
            id = userId,
            name = "John",
            age = 12
        )
        UserTable.insert {
            it[id] = user.id
            it[name] = user.name
            it[age] = user.age
        }
        DogTable.deleteWhere {
            DogTable.user eq userId
        }
        UserTable.upsert {
            it[id] = user.id
            it[name] = user.name
            it[age] = user.age
        }
        UserTable.deleteWhere {
            UserTable.id eq id
        }
    }
}

suspend fun transaction() {
    val userDao = UserDao()
    val dogDao = DogDao()
    val userId = UUID.randomUUID()
    val userId2 = UUID.randomUUID()
    val user = User(
        id = userId,
        name = "John",
        age = 12
    )
    userDao.insert(user)
    val user2 = User(
        id = userId2,
        name = "Jack",
        age = 12
    )
    userDao.insert(user2)
    dogDao.insert(Dog(id = UUID.randomUUID(), userId = userId, name = "Doggo"))
    dogDao.insert(Dog(id = UUID.randomUUID(), userId = userId, name = "Dog"))

    dbQuery {
        HatTable.insert { table ->
            table[HatTable.user] = userId
            table[hat] = "Hat"
        }
        HatTable.insert { table ->
            table[HatTable.user] = userId2
            table[hat] = "HatDos"
        }

        println(userDao.getAll().first())

        val query = (UserTable fullJoin DogTable fullJoin HatTable).selectAll().toList().groupBy (
            { it }, { listOf(it.toNullableDog(), it.toNullableHat()) }
        )
        query.forEach { (k, v) ->
            println("KEY: $k, VALUE: $v")
        }
        UserTable.deleteAll()
        DogTable.deleteAll()
        HatTable.deleteAll()
    }
}

data class UserWithDogAndHat(
    val user: User,
    val dogs: List<Dog>,
    val hats: List<Hat>,
)

fun joinTable(tables: List<Table>): Join {
    return if (tables.size == 2) {
        tables[0] fullJoin tables[1]
    } else {
        tables[0] fullJoin joinTable(tables.subList(1, tables.size))
    }
}

fun <T>embeddedResult(vararg table: Table): Map<ResultRow, List<ResultRow>> {
    val tables: List<Table> = table.toList()
    val query: Query = if (tables.size > 1) {
        joinTable(tables).selectAll()
    } else if (tables.size == 1) {
        tables.first().selectAll()
    } else {
        return mapOf()
    }

    return mapOf()
}

