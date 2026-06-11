package com.civiq.app.utils

import android.content.Context
import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

/**
 * Represents a piece of text that can either be a runtime string (e.g. an
 * error message returned from Firestore/AI APIs) or a string resource
 * (for localized, user-facing copy). This lets ViewModels emit user-facing
 * text without holding a [Context] reference.
 */
sealed class UiText {
    data class DynamicString(val value: String) : UiText()
    class StringResource(
        @StringRes val resId: Int,
        val args: List<Any> = emptyList(),
    ) : UiText()

    @Composable
    fun asString(): String = when (this) {
        is DynamicString -> value
        is StringResource -> stringResourceCompat(resId, args)
    }

    fun asString(context: Context): String = when (this) {
        is DynamicString -> value
        is StringResource -> context.getString(resId, *args.toTypedArray())
    }
}

@Composable
private fun stringResourceCompat(@StringRes resId: Int, args: List<Any>): String {
    val context = LocalContext.current
    return context.getString(resId, *args.toTypedArray())
}
