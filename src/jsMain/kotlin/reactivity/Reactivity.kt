package reactivity

import dom.BuildContext
import kotlin.reflect.KClass
import kotlin.reflect.KProperty

class State<T>(initialValue: T) {
    private var _value: T = initialValue
    private val observers = mutableListOf<() -> Unit>()

    var value: T
        get() {
            return _value
        }
        set(newValue) {
            if (_value != newValue) {
                _value = newValue
                notifyObservers()
            }
        }

    fun subscribe(observer: () -> Unit) {
        observers.add(observer)
    }

    fun notifyObservers() {
        observers.forEach {
            it()
        }
    }

    fun setWithoutNotifying(newValue: T) {
        _value = newValue
    }

    operator fun invoke(): T = value
    operator fun invoke(value:T) {
        this.value = value
    }
}


// Minimal reactivity helpers as seen in Main.kt imports
fun <T> State<List<T>>.add(item: T) {
    this.value = this.value + item
}

fun <T> State<List<T>>.size(): Int {
   return this.value.size
}

fun <T> State<List<T>>.set(idx: Int,item: T) {
    val value = this.value.toMutableList()
    value.set(idx,item)
    this.value = value
}


fun <T> State<List<T>>.remove(item: T) {
    this.value = this.value - item
}

fun <T> State<List<T>>.removeAt(idx: Int) {
    val list = this.value.toMutableList()
    list.removeAt(idx)
    this.value = list
}

fun <T> State<List<T>>.removeLast() {
    if (this.value.isNotEmpty()) {
        this.value = this.value.dropLast(1)
    }
}

fun <T> State<List<T>>.addAll(items: Collection<T>) {
    this.value = this.value + items
}

fun <T> State<List<T>>.get(index: Int): T {
    return this.value[index]
}

fun <T> State<List<T>>.getOrNull(index: Int): T? {
    return this.value.getOrNull(index)
}

// MutableList extensions
fun <T> State<MutableList<T>>.add(item: T) {
    this.value.add(item)
    notifyObservers()
}

// MutableList extensions
fun <T> State<MutableList<T>>.set(idx: Int, item: T) {
    this.value.set(idx,item)
    notifyObservers()
}

fun <T> State<MutableList<T>>.remove(item: T) {
    if (this.value.remove(item)) {
        notifyObservers()
    }
}

fun <T> State<MutableList<T>>.removeLast() {
    if (this.value.isNotEmpty()) {
        this.value.removeAt(this.value.size - 1)
        notifyObservers()
    }
}

fun <T> State<MutableList<T>>.addAll(items: Collection<T>) {
    if (this.value.addAll(items)) {
        notifyObservers()
    }
}

fun <T> State<MutableList<T>>.get(index: Int): T {
    return this.value[index]
}

fun <T> State<MutableList<T>>.getOrNull(index: Int): T? {
    return this.value.getOrNull(index)
}


fun <T> State<MutableList<T>>.removeAt(idx: Int) {
    this.value.removeAt(idx)
    notifyObservers()
}

fun <T> State<MutableList<T>>.size(): Int {
    return this.value.size
}


class Store<S, A : Any>(
    initialState: S,
    private val block: StoreBuilder<S, A>.() -> Unit
) {
    private val _state = State(initialState)
    val state: S get() = _state.value

    private val handlers = mutableMapOf<KClass<out A>, (A) -> Unit>()

    init {
        val builder = StoreBuilder(this)
        builder.block()
    }

    internal fun registerHandler(clazz: KClass<out A>, handler: (A) -> Unit) {
        handlers[clazz] = handler
    }

    fun commit(reducer: S.() -> S) {
        val newState = _state.value.reducer()
        _state.value = newState
    }

    fun dispatch(action: A) {
        handlers[action::class]?.invoke(action)
    }

    fun subscribe(observer: () -> Unit) {
        _state.subscribe(observer)
    }
}

class StoreBuilder<S, A : Any>(private val store: Store<S, A>) {
    fun <T : A> on(clazz: KClass<T>, handler: Store<S,A>.(T) -> Unit) {
        store.registerHandler(clazz) { action -> handler(store, action as T) }
    }
}

fun <S, A : Any> createStore(initialState: S, block: StoreBuilder<S, A>.() -> Unit): Store<S, A> {
    return Store(initialState, block)
}