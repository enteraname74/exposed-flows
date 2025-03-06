package hat

import dog.Dog
import dog.DogTable
import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.ResultRow
import user.UserTable
import java.util.*

object HatTable : UUIDTable() {
    val user = reference("userId", UserTable, onDelete = ReferenceOption.CASCADE)
    val hat = varchar("hat", 128)
}

data class Hat(
    val id: UUID,
    val userId: UUID,
    val hat: String,
)

fun ResultRow.toNullableHat(): Hat? = try {
    Hat(
        id = this[HatTable.id].value,
        userId = this[HatTable.user].value,
        hat = this[HatTable.hat]
    )
} catch (e: NullPointerException) { null}