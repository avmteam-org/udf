package org.avmteam.udf

import kotlin.reflect.KClass

interface SideEffect<Action : Any, SideAction : Any> {

    fun actionId(): KClass<Action> = throw IllegalStateException("actionId must be specified!")

    suspend fun execute(action: Action, reducerCallback: suspend (SideAction) -> Unit)
}
