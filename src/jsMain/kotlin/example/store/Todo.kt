package example.store

import reactivity.createStore

data class ItemTodo(
    val name: String,
    val done: Boolean = false
)

data class TodoState(val todos: List<ItemTodo> = emptyList())
sealed class TodoAction {
    data class Remove(val idx: Int) : TodoAction()
    data class Add(val name: String) : TodoAction()
    data class ChangeStatue(val idx: Int, val status: Boolean) : TodoAction()
    data object Clear : TodoAction()
}

val todo = createStore(
    TodoState()
) {
    on(TodoAction.Add::class) { action ->
        commit { copy(todos = todos + ItemTodo(action.name)) }
    }
    on(TodoAction.Remove::class) { action ->
        commit { copy(todos = todos.filterIndexed { index, _ -> index != action.idx }) }
    }
    on(TodoAction.ChangeStatue::class) { action ->
        commit { copy(todos = todos.mapIndexed { index, item -> if (index == action.idx) item.copy(done = action.status) else item }) }
    }
    on(TodoAction.Clear::class) {
        commit { copy(todos = emptyList()) }
    }
}