package org.avmteam.udf

typealias Reducer<State, SideAction> = (currentState: State, newAction: SideAction) -> State
