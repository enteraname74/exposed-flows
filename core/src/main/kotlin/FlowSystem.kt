import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import org.jetbrains.exposed.sql.Query
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.transactions.transaction

/**
 * Represent information about a flow.
 * A flow is linked to a list of tables and a query.
 * - The list of tables identify the flow and is used to define if the flow should be updated when one of
 * its table has changed.
 * - The query is used to fetch new data and update the flow.
 */
internal data class FlowInformation(
    val referencedTablesNames: List<String>,
    val query: Query,
) {
    val flow: MutableStateFlow<List<ResultRow>> = MutableStateFlow(query.toList())

    /**
     * Fetch new values from the FlowInformation query and update the flow with it.
     */
    fun update() {
        flow.value = transaction { query.toList() }
    }
}

/**
 * Flow system for Exposed.
 */
internal object FlowSystem {
    private val allFlows: ArrayList<FlowInformation> = arrayListOf()

    /**
     * Adds a flow to the list of flows of the system.
     */
    fun addFlow(flowInformation: FlowInformation) {
        allFlows.add(flowInformation)
    }

    /**
     * Update all flows that are linked to one of the referenced tables names.
     */
    fun update(referencedTablesNames: List<String>) {
        allFlows.forEach { flowInformation ->
            if (flowInformation.referencedTablesNames.any { referencedTablesNames.contains(it) }) {
                flowInformation.update()
            }
        }
    }
}

/**
 * Used to do a transaction on a table.
 * It will update all flows that uses values related to the table.
 */
suspend fun <T> flowTransactionOn(vararg table: Table, block: () -> T) {
    dbQuery {
        block()
        FlowSystem.update(referencedTablesNames = table.asList().map { it.tableName })
    }
}

private val Query.tables: List<Table>
    get() = this.set.source.columns.map { it.table }.distinct()

/**
 * Returns the result of a query as a flow.
 * When one of the table of the query change,
 * the flow returned from this function will emit a new value.
 */
fun Query.asFlow(): Flow<List<ResultRow>> = transaction {
    val referencedTablesNames = this@asFlow.tables.map { it.tableName }
    val newFlow = FlowInformation(
        referencedTablesNames = referencedTablesNames,
        query = this@asFlow
    )
    FlowSystem.addFlow(newFlow)

    newFlow.flow
}

fun <R> Flow<List<ResultRow>>.mapResultRow(transform: (ResultRow) -> R): Flow<List<R>> =
    this.map { list ->
        list.map {
            transform(it)
        }
    }