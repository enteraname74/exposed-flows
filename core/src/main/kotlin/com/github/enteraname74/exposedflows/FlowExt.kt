package com.github.enteraname74.exposedflows

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.jetbrains.exposed.sql.ResultRow

/**
 * Transforms a flow of a list of ResultRow to a flow of a list of another type.
 */
fun <R> Flow<List<ResultRow>>.mapResultRow(transform: (ResultRow) -> R): Flow<List<R>> =
    this.map { list ->
        list.map {
            transform(it)
        }
    }

/**
 * Transforms and retrieve a single element of the flow. It will return null if the list in the flow is empty.
 */
fun <R> Flow<List<ResultRow>>.mapSingleResultRow(transform: (ResultRow) -> R): Flow<R?> =
    this.mapResultRow(transform).firstOrNull()

/**
 * Returns a flow of the first element in the list of the given flow, or null if the list is empty.
 */
fun <T>Flow<List<T>>.firstOrNull(): Flow<T?> =
    this.map { list -> list.firstOrNull() }