package org.avmteam.udf

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.observe

actual class StateObservable<State : Any> actual constructor(initialState: State) {

    private val liveData = MutableLiveData(initialState)

    actual var stateValue: State
        get() = liveData.value!!
        set(value) {
            liveData.value = value
        }

    actual class Factory {
        actual fun <State : Any> create(initialState: State) = StateObservable(initialState)
    }

    actual fun observe(stateObserver: StateObserver<State>) {
        liveData.observe(
            owner = stateObserver.lifecycleOwner,
            onChanged = stateObserver::onStateChanged
        )
    }

    @Deprecated(
        message = "use StateObserver instead",
        replaceWith = ReplaceWith("observe(StateObserver(lifecycleOwner))")
    )
    fun observe(lifecycleOwner: LifecycleOwner, callback: (State) -> Unit) {
        liveData.observe(
            owner = lifecycleOwner,
            onChanged = callback
        )
    }

    actual class StateObserver<State : Any>(
        internal val lifecycleOwner: LifecycleOwner,
        private val callback: (State) -> Unit
    ) {
        actual fun onStateChanged(state: State) {
            callback(state)
        }
    }
}
