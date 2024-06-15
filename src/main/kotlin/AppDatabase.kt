import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.transactions.transaction

object AppDatabase {
    fun connectToDatabase() {
        Database.connect("jdbc:sqlite:database.db", "org.sqlite.JDBC")
        transaction {
            SchemaUtils.create(
                UserTable,
                OtherTable
            )
        }
    }
}

suspend fun <T> dbQuery(block: suspend () -> T): T =
    newSuspendedTransaction(Dispatchers.IO) { block() }