package user

import com.github.enteraname74.exposedflows.*
import dog.DogTable
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.*

class UserDao {
    suspend fun deleteById(id: UUID) {
        flowTransactionOn(UserTable, DogTable) {
            UserTable.deleteWhere {
                UserTable.id eq id
            }
        }
    }

    suspend fun insert(user: User) {
        flowTransactionOn {
            UserTable.insert {
                it[id] = user.id
                it[name] = user.name
                it[age] = user.age
            }
        }
    }

    fun getFromId(id: UUID): Flow<User?> = transaction {
        UserTable
            .selectAll()
            .where { UserTable.id eq id }
            .asFlow()
            .mapSingleResultRow { it.toUser() }
    }

    fun getFromAge(age: Int): Flow<User?> = transaction {
        UserTable
            .selectAll()
            .where { UserTable.age eq age }
            .asFlow()
            .mapResultRow { it.toUser() }
            .firstOrNull()
    }

    fun getAll(): Flow<List<User>> = transaction {
        UserTable
            .selectAll()
            .asFlow()
            .mapResultRow { it.toUser() }
    }

}