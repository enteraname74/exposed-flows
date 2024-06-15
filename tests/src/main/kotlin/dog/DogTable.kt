package dog

import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.Table
import user.UserTable
import java.util.*

object DogTable: Table() {
    val id = varchar("id", 128)
    val userId = reference("userId", UserTable.id, onDelete = ReferenceOption.CASCADE)
    val name = varchar("name", 128)
}

fun ResultRow.toDog() = Dog(
    id = UUID.fromString(this[DogTable.id]),
    userId = UUID.fromString(this[DogTable.userId]),
    name = this[DogTable.name]
)