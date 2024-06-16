package dog

import asFlow
import dbQuery
import flowTransactionOn
import kotlinx.coroutines.flow.Flow
import mapResultRow
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.*

class DogDao {
    suspend fun deleteById(id: UUID) {
        dbQuery {
            DogTable.deleteWhere {
                DogTable.id eq id
            }
        }
    }
    suspend fun insert(dog: Dog) {
        flowTransactionOn(DogTable) {
            DogTable.insert {
                it[user] = dog.userId
                it[name] = dog.name
            }
        }
    }

    fun getAll(): Flow<List<Dog>> = transaction {
        DogTable
            .selectAll()
            .asFlow()
            .mapResultRow { it.toDog() }
    }
}