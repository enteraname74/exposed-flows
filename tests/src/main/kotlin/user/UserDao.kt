package user

import asFlow
import dbQuery
import dog.DogTable
import flowTransactionOn
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import mapResultRow
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
                UserTable.id eq id.toString()
            }
            DogTable.deleteWhere {
                DogTable.userId eq id.toString()
            }
        }
    }

    suspend fun insertFlow(user: User) {
        flowTransactionOn(UserTable) {
            UserTable.insert {
                it[id] = user.id.toString()
                it[name] = user.name
                it[age] = user.age
            }
        }
    }

    suspend fun deleteFromAge(age: Int) {
        flowTransactionOn(UserTable) {
            UserTable.deleteWhere { UserTable.age eq age }
        }
    }

    fun getFromId(id: UUID): Flow<User?> = transaction {
        UserTable
            .selectAll()
            .where { UserTable.id eq id.toString() }
            .asFlow()
            .mapResultRow { it.toUser() }
            .map { it.firstOrNull() }
    }

    fun getFromAge(age: Int): Flow<User?> = transaction {
        UserTable
            .selectAll()
            .where { UserTable.age eq age }
            .asFlow()
            .mapResultRow { it.toUser() }
            .map { it.firstOrNull() }
    }

    suspend fun getAll(): List<User> = dbQuery {
        UserTable
            .selectAll()
            .map { it.toUser() }
    }

    fun getAllFlow(): Flow<List<User>> = transaction {
        UserTable
            .selectAll()
            .asFlow()
            .mapResultRow { it.toUser() }
    }

}