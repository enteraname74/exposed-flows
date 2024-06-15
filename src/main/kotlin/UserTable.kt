import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.Table
import java.util.*

object UserTable: Table() {
    val id = varchar("id", 128)
    val name = varchar("name", 128)
    val age = integer("age")

}

fun ResultRow.toUser(): User = User(
    id = UUID.fromString(this[UserTable.id]),
    name = this[UserTable.name],
    age = this[UserTable.age]
)