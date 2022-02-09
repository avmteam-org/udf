package org.avmteam.udf.extensions

expect class EventDispatcher<Event : Any> {

    suspend fun dispatch(event: Event)
}
