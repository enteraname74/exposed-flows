package user

import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.Table
import java.util.*

object UserTable: UUIDTable() {
    val name = varchar("name", 128)
    val age = integer("age")
}

fun ResultRow.toUser(): User = User(
    id = this[UserTable.id].value,
    name = this[UserTable.name],
    age = this[UserTable.age]
)