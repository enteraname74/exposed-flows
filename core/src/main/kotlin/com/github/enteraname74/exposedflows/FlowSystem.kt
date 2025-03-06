package com.github.enteraname74.exposedflows

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.concurrent.CopyOnWriteArrayList

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
    private val mutex: Mutex = Mutex()

    /**
     * Adds a flow to the list of flows of the system.
     */
    suspend fun addFlow(flowInformation: FlowInformation) {
        mutex.withLock {
            allFlows.add(flowInformation)
        }
    }

    /**
     * Update all flows that are linked to one of the referenced tables names.
     */
    fun update(referencedTablesNames: List<String>) {
        val snapshot: List<FlowInformation>
        synchronized(allFlows) {
            snapshot = allFlows.toList()
        }

        snapshot.forEach { flowInformation ->
            if (flowInformation.referencedTablesNames.any { referencedTablesNames.contains(it) }) {
                flowInformation.update()
            }
        }
    }
}

internal suspend fun <T> updateFromLogger(block: () -> T) {
    dbQuery {
        addLogger(TableWriteOperationLogger)
        block()
        val referencedTablesNames: List<String> = TableWriteOperationLogger.getAccessedTables()
        FlowSystem.update(referencedTablesNames = referencedTablesNames)
        TableWriteOperationLogger.clear()
    }
}

internal suspend fun <T> updateFromGivenTablesNames(tables: List<String>, block: () -> T) {
    dbQuery {
        block()
        FlowSystem.update(referencedTablesNames = tables)
    }
}

/**
 * Used to do a transaction on multiple tables and update all flows that use values related to the given tables.
 *
 * If not tables are given, the system will try to find the concerned table in the [block].
 * Beware that tables may be not recognized or that some SQL statements may be too complex.
 * This approach should be used for relatively simple SQL statements.
 */
suspend fun <T> flowTransactionOn(vararg table: Table, block: () -> T) {
    val tableNames = table.asList().map { it.tableName }
    if (tableNames.isEmpty()) {
        updateFromLogger { block() }
    } else {
        updateFromGivenTablesNames(tableNames) { block() }
    }
}

internal suspend fun <T> dbQuery(block: suspend Transaction.() -> T): T =
    newSuspendedTransaction(Dispatchers.IO) { block() }