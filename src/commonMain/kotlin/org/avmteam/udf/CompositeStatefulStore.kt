package org.avmteam.udf

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class CompositeStatefulStore<Action : Any, SideAction : Any, State : Any> private constructor(
    val state: StateObservable<State>,
    private val reducer: Reducer<State, SideAction>,
    private val sideEffects: SideEffects<Action, SideAction>,
    private val logger: Logger?
) : Store<Action> {

    class Builder<Action : Any, SideAction : Any, State : Any> private constructor() :
        BuildReducer<Action, SideAction, State>,
        BuildInitialState<Action, SideAction, State>,
        BuildStateObservableFactory<Action, SideAction, State>,
        Build<Action, SideAction, State> {

        companion object {
            fun <Action : Any, SideAction : Any, State : Any> getInstance():
                    BuildReducer<Action, SideAction, State> = Builder()
        }

        private lateinit var reducer: Reducer<State, SideAction>
        private lateinit var initialState: State
        private lateinit var stateObservableFactory: StateObservable.Factory
        private var logger: Logger? = null
        private val sideEffectsBuilder: SideEffects.Builder<Action, SideAction> = SideEffects.Builder()

        override fun reducer(reducer: Reducer<State, SideAction>): Builder<Action, SideAction, State> {
            this.reducer = reducer
            return this
        }

        override fun initialState(initialState: State): Builder<Action, SideAction, State> {
            this.initialState = initialState
            return this
        }

        override fun stateObservableFactory(
            stateObservableFactory: StateObservable.Factory
        ): Builder<Action, SideAction, State> {
            this.stateObservableFactory = stateObservableFactory
            return this
        }

        override fun logger(logger: Logger): Builder<Action, SideAction, State> {
            this.logger = logger
            return this
        }

        override fun appendSideEffect(
            sideEffect: SideEffect<out Action, out SideAction>
        ): Builder<Action, SideAction, State> {
            sideEffectsBuilder.appendSideEffect(sideEffect)
            return this
        }

        override fun build(): CompositeStatefulStore<Action, SideAction, State> = CompositeStatefulStore(
            reducer = reducer,
            state = stateObservableFactory.create(initialState),
            logger = logger,
            sideEffects = sideEffectsBuilder.build()
        )
    }

    override suspend fun process(action: Action) {
        logger?.log("process action: $action")
        sideEffects.execute(action, ::produceNextAction)
    }

    private suspend fun produceNextAction(sideAction: SideAction) {
        withContext(Dispatchers.Main) {
            logger?.log("current state: ${state.stateValue}; sideAction: $sideAction")
            val newState = reducer(state.stateValue, sideAction)
            state.stateValue = newState
            logger?.log("new state: $newState")
        }
    }

    interface BuildReducer<Action : Any, SideAction : Any, State : Any> {
        fun reducer(reducer: Reducer<State, SideAction>): BuildInitialState<Action, SideAction, State>
    }

    interface BuildInitialState<Action : Any, SideAction : Any, State : Any> {
        fun initialState(initialState: State): BuildStateObservableFactory<Action, SideAction, State>
    }

    interface BuildStateObservableFactory<Action : Any, SideAction : Any, State : Any> {
        fun stateObservableFactory(stateObservableFactory: StateObservable.Factory): Build<Action, SideAction, State>
    }

    interface Build<Action : Any, SideAction : Any, State : Any> {
        fun appendSideEffect(sideEffect: SideEffect<out Action, out SideAction>): Build<Action, SideAction, State>
        fun logger(logger: Logger): Build<Action, SideAction, State>
        fun build(): CompositeStatefulStore<Action, SideAction, State>
    }
}
