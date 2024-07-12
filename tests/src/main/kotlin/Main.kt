import com.github.enteraname74.exposedflows.flowTransactionOn
import dog.Dog
import dog.DogDao
import dog.DogTable
import kotlinx.coroutines.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.upsert
import user.User
import user.UserDao
import user.UserTable
import java.util.*

suspend fun main() {
    AppDatabase.connectToDatabase()
    test()
    automaticFlowTransactionTest()
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
