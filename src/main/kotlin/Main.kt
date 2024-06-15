import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.*
import kotlin.coroutines.CoroutineContext

suspend fun main() {
    println("Hello World!")

    AppDatabase.connectToDatabase()
    val dao = UserDao()
    val otherDao = OtherDao()

    CoroutineScope(Dispatchers.IO).launch {
        dao.getFromAge(15).collect {
            println("Got from age ? $it")
        }
    }

    delay(2000)

    CoroutineScope(Dispatchers.IO).launch {
        dao.getAllFlow().collect {
            println("ALL: $it")
        }
    }

    val job1 = CoroutineScope(Dispatchers.IO).launch {
        delay(3000)
        println("FIRST")
        val user = User(
            id = UUID.randomUUID(),
            name = "Jack",
            age = 12
        )
        dao.insertFlow(user)
        delay(2000)
        println("SECOND")
        val userDos = User(
            id = UUID.randomUUID(),
            name = "Michel",
            age = 15
        )
        dao.insertFlow(userDos)
        delay(2000)
        dao.deleteFromAge(15)
        delay(3000)
    }
    joinAll(job1)
}

data class User(val id : UUID, val name: String, val age : Int)
