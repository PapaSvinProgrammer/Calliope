package com.mordva.connectivity

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow

class RetriableRequest<T> internal constructor(
    private val initialResult: Result<T>,
    private val retryResults: Flow<Result<T>>,
) {
    fun results(): Flow<Result<T>> = flow {
        emit(initialResult)
        emitAll(retryResults)
    }
}
