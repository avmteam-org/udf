package org.avmteam.udf.extensions

import androidx.lifecycle.LifecycleOwner
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

actual class EventDispatcher<Event : Any> {

    private val singleLiveEvent = SingleLiveEvent<Event>()

    actual suspend fun dispatch(event: Event) {
        withContext(Dispatchers.Main) {
            singleLiveEvent.value = (event)
        }
    }

    fun observe(lifecycleOwner: LifecycleOwner, callback: (Event) -> Unit) {
        singleLiveEvent.observe(
            owner = lifecycleOwner,
            observer = callback
        )
    }
}
