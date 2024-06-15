import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.Table
import java.util.*

object OtherTable: Table() {
    val id = varchar("id", 128)
    val age = integer("age")
}

fun ResultRow.toOther() = Other(
    id = UUID.fromString(this[OtherTable.id]),
    age = this[OtherTable.age]
)