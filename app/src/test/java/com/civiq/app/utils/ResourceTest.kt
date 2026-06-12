package com.civiq.app.utils

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class ResourceTest {

    @Test
    fun `onSuccess invokes action for Success and returns same instance`() {
        val resource: Resource<Int> = Resource.Success(42)
        var captured: Int? = null

        val returned = resource.onSuccess { captured = it }

        assertThat(captured).isEqualTo(42)
        assertThat(returned).isSameInstanceAs(resource)
    }

    @Test
    fun `onSuccess does not invoke action for Error or Loading`() {
        var invoked = false

        (Resource.Error<Int>(UiText.DynamicString("oops")) as Resource<Int>).onSuccess { invoked = true }
        (Resource.Loading<Int>() as Resource<Int>).onSuccess { invoked = true }

        assertThat(invoked).isFalse()
    }

    @Test
    fun `onError invokes action for Error and returns same instance`() {
        val message = UiText.DynamicString("failure")
        val resource: Resource<Int> = Resource.Error(message)
        var captured: UiText? = null

        val returned = resource.onError { captured = it }

        assertThat(captured).isEqualTo(message)
        assertThat(returned).isSameInstanceAs(resource)
    }

    @Test
    fun `onError does not invoke action for Success or Loading`() {
        var invoked = false

        (Resource.Success(1) as Resource<Int>).onError { invoked = true }
        (Resource.Loading<Int>() as Resource<Int>).onError { invoked = true }

        assertThat(invoked).isFalse()
    }

    @Test
    fun `map transforms Success data`() {
        val resource: Resource<Int> = Resource.Success(2)

        val mapped = resource.map { it * 10 }

        assertThat(mapped).isEqualTo(Resource.Success(20))
    }

    @Test
    fun `map transforms non-null Error and Loading data, preserving message`() {
        val message = UiText.DynamicString("failure")
        val error: Resource<Int> = Resource.Error(message, data = 5)
        val loading: Resource<Int> = Resource.Loading(data = 7)

        assertThat(error.map { it * 2 }).isEqualTo(Resource.Error(message, data = 10))
        assertThat(loading.map { it * 2 }).isEqualTo(Resource.Loading(data = 14))
    }

    @Test
    fun `map preserves null data for Error and Loading without invoking transform`() {
        val message = UiText.DynamicString("failure")
        val error: Resource<Int> = Resource.Error(message, data = null)
        val loading: Resource<Int> = Resource.Loading(data = null)

        assertThat(error.map { it * 2 }).isEqualTo(Resource.Error<Int>(message, data = null))
        assertThat(loading.map { it * 2 }).isEqualTo(Resource.Loading<Int>(data = null))
    }
}
