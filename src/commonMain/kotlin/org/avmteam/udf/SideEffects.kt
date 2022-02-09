package org.avmteam.udf

import kotlin.reflect.KClass

class SideEffects<Action : Any, SideAction : Any> private constructor() {

    private val sideEffectsMap = hashMapOf<KClass<out Action>, Any>()

    suspend fun execute(action: Action, reducerCallback: suspend (SideAction) -> Unit) {
        val value = sideEffectsMap[action::class] as SideEffect<Action, SideAction>
        value.execute(action, reducerCallback)
    }

    class Builder<Action : Any, SideAction : Any> {

        private val sideEffects = SideEffects<Action, SideAction>()

        fun appendSideEffect(
            sideEffect: SideEffect<out Action, out SideAction>
        ): Builder<Action, SideAction> {
            sideEffects.sideEffectsMap[sideEffect.actionId()] = sideEffect
            return this
        }

        fun build(): SideEffects<Action, SideAction> = sideEffects
    }
}
