import dog.DogTable
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import user.UserTable

object AppDatabase {
    fun connectToDatabase() {
        Database.connect("jdbc:sqlite:database.db?foreign_keys=on", "org.sqlite.JDBC")
        transaction {
            SchemaUtils.create(
                UserTable,
                DogTable
            )
        }
    }
}