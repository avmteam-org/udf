package org.avmteam.udf.test

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import io.mockk.Runs
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runBlockingTest
import kotlinx.coroutines.test.setMain
import org.junit.Rule
import org.avmteam.udf.CompositeStatefulStore
import org.avmteam.udf.Logger
import org.avmteam.udf.Reducer
import org.avmteam.udf.SideEffect
import org.avmteam.udf.StateObservable
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

@ExperimentalCoroutinesApi
class CompositeStatefulStoreTest {

    @get:Rule
    val testRule = InstantTaskExecutorRule()

    sealed class Action {
        object FirstAction : Action()
        object SecondAction : Action()
    }

    sealed class SideAction {
        object FirstSideAction : SideAction()
        object SecondSideAction : SideAction()
    }

    data class State(val id: String)

    class FirstSideEffect : SideEffect<Action.FirstAction, SideAction> {
        override fun actionId() = Action.FirstAction::class

        override suspend fun execute(
            action: Action.FirstAction,
            reducerCallback: suspend (SideAction) -> Unit
        ) {
            reducerCallback(SideAction.FirstSideAction)
        }
    }

    class SecondSideEffect : SideEffect<Action.SecondAction, SideAction> {

        override fun actionId() = Action.SecondAction::class

        override suspend fun execute(
            action: Action.SecondAction,
            reducerCallback: suspend (SideAction) -> Unit
        ) {
            reducerCallback(SideAction.SecondSideAction)
        }
    }

    private val testCoroutineDispatcher = TestCoroutineDispatcher()

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(testCoroutineDispatcher)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun initialState() = runBlockingTest {
        val reducer: Reducer<State, SideAction> = mockk()
        val compositeStatefulStore =
            CompositeStatefulStore.Builder.getInstance<Action, SideAction, State>()
                .reducer(reducer)
                .initialState(State("initial"))
                .stateObservableFactory(StateObservable.Factory())
                .build()
        assertEquals(compositeStatefulStore.state.stateValue.id, "initial")

        confirmVerified(reducer)
    }

    @Test
    fun logging() = runBlockingTest {
        val logger: Logger = mockk()
        val reducer: Reducer<State, SideAction> = mockk()
        val compositeStatefulStore =
            CompositeStatefulStore.Builder.getInstance<Action, SideAction, State>()
                .reducer(reducer)
                .initialState(State("initial"))
                .stateObservableFactory(StateObservable.Factory())
                .appendSideEffect(FirstSideEffect())
                .logger(logger)
                .build()

        every { logger.log("process action: ${Action.FirstAction}") } just Runs
        every { logger.log("current state: ${State("initial")}; sideAction: ${SideAction.FirstSideAction}") } just Runs
        every { logger.log("new state: ${State("first side action")}") } just Runs
        every {
            reducer.invoke(
                State("initial"),
                SideAction.FirstSideAction
            )
        } answers { State("first side action") }

        compositeStatefulStore.process(Action.FirstAction)
        verify { logger.log("process action: ${Action.FirstAction}") }
        verify { logger.log("current state: ${State("initial")}; sideAction: ${SideAction.FirstSideAction}") }
        verify { logger.log("new state: ${State("first side action")}") }
        verify { reducer.invoke(State("initial"), SideAction.FirstSideAction) }

        confirmVerified(logger, reducer)
    }

    @Test
    fun severalActions() = runBlockingTest {
        val reducer: Reducer<State, SideAction> = mockk()
        val compositeStatefulStore =
            CompositeStatefulStore.Builder.getInstance<Action, SideAction, State>()
                .reducer(reducer)
                .initialState(State("initial"))
                .stateObservableFactory(StateObservable.Factory())
                .appendSideEffect(FirstSideEffect())
                .appendSideEffect(SecondSideEffect())
                .build()

        every {
            reducer.invoke(
                State("initial"),
                SideAction.SecondSideAction
            )
        } answers { State("second side action") }
        every {
            reducer.invoke(
                State("second side action"),
                SideAction.FirstSideAction
            )
        } answers { State("first side action") }

        confirmVerified(reducer)
        assertEquals(State("initial"), compositeStatefulStore.state.stateValue)

        compositeStatefulStore.process(Action.SecondAction)

        verify { reducer(State("initial"), SideAction.SecondSideAction) }
        confirmVerified(reducer)
        assertEquals(State("second side action"), compositeStatefulStore.state.stateValue)

        compositeStatefulStore.process(Action.FirstAction)

        verify { reducer(State("second side action"), SideAction.FirstSideAction) }
        confirmVerified(reducer)
        assertEquals(State("first side action"), compositeStatefulStore.state.stateValue)
    }
}
