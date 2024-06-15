import org.jetbrains.exposed.sql.insert

class OtherDao {
    suspend fun insertFlow(other: Other) {
        flowTransactionOn(OtherTable) {
            OtherTable.insert {
                it[id] = other.id.toString()
                it[age] = other.age
            }
        }
    }
}