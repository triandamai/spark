package internal.builders

import internal.*
import internal.types.HttpMethod
import org.w3c.dom.events.Event

@ViewDsl
class FormBuilder(element: VElement, parentBuilder: ElementBuilder?) :
    BaseElementBuilder<FormBuilder>(element, parentBuilder) {
    
    fun action(value: String): FormBuilder {
        element.attributes["action"] = value
        return this
    }

    fun bindAction(cb:(Event)->Unit): FormBuilder {
        element.listeners["submit"] = { event ->
            event.preventDefault()
            cb(event)
        }
        return this
    }

    /**
     * Set the HTTP method using type-safe HttpMethod.
     * Example: method(HttpMethod.Post)
     */
    fun method(value: HttpMethod): FormBuilder {
        element.attributes["method"] = value.value
        return this
    }

    /**
     * Set the HTTP method using a string (for backward compatibility).
     * Example: method("POST")
     */
    fun method(value: String): FormBuilder {
        element.attributes["method"] = value
        return this
    }
}
