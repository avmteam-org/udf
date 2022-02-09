package org.avmteam.udf

interface Store<Action : Any> {
    suspend fun process(action: Action)
}
