package org.avmteam.udf

expect class StateObservable<State : Any>(initialState: State) {

    var stateValue: State

    fun observe(stateObserver: StateObserver<State>)

    class Factory {
        fun <State : Any> create(initialState: State): StateObservable<State>
    }

    class StateObserver<State : Any> {
        fun onStateChanged(state: State)
    }
}
