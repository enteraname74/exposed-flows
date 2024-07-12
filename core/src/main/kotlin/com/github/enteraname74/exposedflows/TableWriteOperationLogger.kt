package com.github.enteraname74.exposedflows

import org.jetbrains.exposed.sql.SqlLogger
import org.jetbrains.exposed.sql.Transaction
import org.jetbrains.exposed.sql.statements.StatementContext
import org.jetbrains.exposed.sql.statements.expandArgs
import org.jetbrains.exposed.sql.transactions.TransactionManager

/**
 * Logger used to retrieves the modified tables in a transaction block.
 */
internal object TableWriteOperationLogger : SqlLogger {
    private val accessedTables: MutableSet<String> = mutableSetOf()

    override fun log(context: StatementContext, transaction: Transaction) {
        val statement: String = context.expandArgs(TransactionManager.current())
        statement.table?.let {
            accessedTables.add(it)
        }

    }

    fun clear() = accessedTables.clear()

    fun getAccessedTables(): List<String> = accessedTables.toList()

    /**
     * Retrieves the table concerned by an sql statement
     */
    private val String.table: String?
        get() {
            val regex = Regex(
                """(insert into|update|delete from|truncate table|alter table)\s+(\S+)""",
                RegexOption.IGNORE_CASE
            )
            return regex.findAll(this).map { it.groupValues[2] }.firstOrNull()?.removeSurrounding("\"")
        }
}