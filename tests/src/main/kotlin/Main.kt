import dog.Dog
import dog.DogDao
import kotlinx.coroutines.*
import user.User
import user.UserDao
import java.util.*

suspend fun main() {
    AppDatabase.connectToDatabase()
    test()
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
