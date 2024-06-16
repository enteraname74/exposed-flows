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
                UserTable.id eq id
            }
        }
    }

    suspend fun insert(user: User) {
        flowTransactionOn(UserTable) {
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

    fun getAll(): Flow<List<User>> = transaction {
        UserTable
            .selectAll()
            .asFlow()
            .mapResultRow { it.toUser() }
    }

}