package internal

object ComponentContext {
    private val stack = mutableListOf<Component>()
    val current: Component? get() = stack.lastOrNull()

    fun <T> withComponent(component: Component, block: () -> T): T {
        stack.add(component)
        try {
            return block()
        } finally {
            if (stack.isNotEmpty()) {
                stack.removeAt(stack.lastIndex)
            }
        }
    }
}
