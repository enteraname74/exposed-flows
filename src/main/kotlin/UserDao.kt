import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction

class UserDao {
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

    fun getFromAge(age: Int): Flow<User?> = transaction {
        UserTable
            .selectAll()
            .where { UserTable.age eq age }
            .asFlow()
            .mapResultRow { it.toUser() }
            .map { it.firstOrNull() }
    }

    fun getSameAge(): Flow<List<User>> = transaction {
        UserTable
            .join(
                otherTable = OtherTable,
                joinType = JoinType.INNER,
                onColumn = UserTable.age,
                otherColumn = OtherTable.age,
            ).selectAll()
            .asFlow()
            .mapResultRow { it.toUser() }
    }


    fun getAllFlow(): Flow<List<User>> = transaction {
        UserTable
            .selectAll()
            .asFlow()
            .mapResultRow { it.toUser() }
    }

}