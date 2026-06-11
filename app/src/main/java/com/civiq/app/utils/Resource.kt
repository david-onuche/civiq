package com.civiq.app.utils

/**
 * Generic wrapper representing the state of a data operation (typically a
 * Firestore call, AI request, or DataStore read) as it flows from the
 * data layer through use cases to the presentation layer.
 */
sealed class Resource<out T> {
    data class Success<out T>(val data: T) : Resource<T>()
    data class Error<out T>(val message: UiText, val data: T? = null) : Resource<T>()
    data class Loading<out T>(val data: T? = null) : Resource<T>()

    inline fun onSuccess(action: (T) -> Unit): Resource<T> {
        if (this is Success) action(data)
        return this
    }

    inline fun onError(action: (UiText) -> Unit): Resource<T> {
        if (this is Error) action(message)
        return this
    }

    fun <R> map(transform: (T) -> R): Resource<R> = when (this) {
        is Success -> Success(transform(data))
        is Error -> Error(message, data?.let(transform))
        is Loading -> Loading(data?.let(transform))
    }
}
