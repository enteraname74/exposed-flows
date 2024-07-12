package com.github.enteraname74.exposedflows

import kotlinx.coroutines.flow.Flow
import org.jetbrains.exposed.sql.Query
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.transactions.transaction

internal val Query.tables: List<Table>
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