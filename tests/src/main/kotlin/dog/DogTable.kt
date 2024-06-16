package dog

import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.Table
import user.UserTable
import java.util.*

object DogTable : UUIDTable() {
    val user = reference("userId", UserTable, onDelete = ReferenceOption.CASCADE)
    val name = varchar("name", 128)
}

fun ResultRow.toDog() = Dog(
    id = this[DogTable.id].value,
    userId = this[DogTable.user].value,
    name = this[DogTable.name]
)