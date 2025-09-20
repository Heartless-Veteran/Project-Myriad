package com.heartlessveteran.myriad.ui.common

import org.junit.Assert.*
import org.junit.Test

/**
 * Unit tests for UiState wrapper and extension functions
 */
class UiStateTest {
    @Test
    fun `UiState Success should contain data`() {
        val data = "test data"
        val uiState = UiState.Success(data)

        assertEquals(data, uiState.data)
        assertTrue(uiState is UiState.Success)
    }

    @Test
    fun `UiState Error should contain exception and message`() {
        val exception = RuntimeException("Test error")
        val customMessage = "Custom error message"
        val uiState = UiState.Error(exception, customMessage)

        assertEquals(exception, uiState.exception)
        assertEquals(customMessage, uiState.message)
        assertTrue(uiState is UiState.Error)
    }

    @Test
    fun `UiState Error should use exception message when custom message not provided`() {
        val exception = RuntimeException("Test error")
        val uiState = UiState.Error(exception)

        assertEquals(exception.message, uiState.message)
    }

    @Test
    fun `onSuccess should execute action for Success state`() {
        var actionExecuted = false
        var receivedData: String? = null

        val uiState = UiState.Success("test")

        uiState.onSuccess { data ->
            actionExecuted = true
            receivedData = data
        }

        assertTrue(actionExecuted)
        assertEquals("test", receivedData)
    }

    @Test
    fun `onSuccess should not execute action for Error state`() {
        var actionExecuted = false

        val uiState = UiState.Error(RuntimeException("test"))

        uiState.onSuccess {
            actionExecuted = true
        }

        assertFalse(actionExecuted)
    }

    @Test
    fun `onError should execute action for Error state`() {
        var actionExecuted = false
        var receivedException: Throwable? = null

        val exception = RuntimeException("test")
        val uiState = UiState.Error(exception)

        uiState.onError { ex ->
            actionExecuted = true
            receivedException = ex
        }

        assertTrue(actionExecuted)
        assertEquals(exception, receivedException)
    }

    @Test
    fun `onError should not execute action for Success state`() {
        var actionExecuted = false

        val uiState = UiState.Success("test")

        uiState.onError {
            actionExecuted = true
        }

        assertFalse(actionExecuted)
    }

    @Test
    fun `onLoading should execute action for Loading state`() {
        var actionExecuted = false

        val uiState = UiState.Loading

        uiState.onLoading {
            actionExecuted = true
        }

        assertTrue(actionExecuted)
    }

    @Test
    fun `map should transform Success data`() {
        val uiState = UiState.Success(5)

        val mapped = uiState.map { it * 2 }

        assertTrue(mapped is UiState.Success)
        assertEquals(10, (mapped as UiState.Success).data)
    }

    @Test
    fun `map should preserve Error state`() {
        val exception = RuntimeException("test")
        val uiState = UiState.Error(exception)

        val mapped = uiState.map { "transformed" }

        assertTrue(mapped is UiState.Error)
        assertEquals(exception, (mapped as UiState.Error).exception)
    }

    @Test
    fun `map should preserve Loading state`() {
        val uiState = UiState.Loading

        val mapped = uiState.map { "transformed" }

        assertTrue(mapped is UiState.Loading)
    }

    @Test
    fun `combineUiStates should combine two Success states`() {
        val state1 = UiState.Success(5)
        val state2 = UiState.Success(10)

        val combined = combineUiStates(state1, state2) { a, b -> a + b }

        assertTrue(combined is UiState.Success)
        assertEquals(15, (combined as UiState.Success).data)
    }

    @Test
    fun `combineUiStates should return Loading if any state is Loading`() {
        val state1 = UiState.Loading
        val state2 = UiState.Success(10)

        val combined = combineUiStates(state1, state2) { a: Any?, b: Int -> b + 1 }

        assertTrue(combined is UiState.Loading)
    }

    @Test
    fun `combineUiStates should return first Error state`() {
        val error1 = RuntimeException("Error 1")
        val error2 = RuntimeException("Error 2")
        val state1 = UiState.Error(error1)
        val state2 = UiState.Error(error2)

        val combined = combineUiStates(state1, state2) { a: String, b: String -> a + b }

        assertTrue(combined is UiState.Error)
        assertEquals(error1, (combined as UiState.Error).exception)
    }

    @Test
    fun `combineUiStates should return Error over Success`() {
        val error = RuntimeException("Error")
        val state1 = UiState.Error(error)
        val state2 = UiState.Success(10)

        val combined = combineUiStates(state1, state2) { a: Int, b: Int -> a + b }

        assertTrue(combined is UiState.Error)
        assertEquals(error, (combined as UiState.Error).exception)
    }
}
