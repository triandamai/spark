package dom.types

/**
 * Type-safe representation of HTTP methods.
 * Use this instead of raw strings to get compile-time safety and IDE autocomplete.
 * 
 * Example:
 * ```
 * form {
 *     action("/api/submit")
 *     method(HttpMethod.Post)
 * }
 * ```
 */
sealed class HttpMethod(val value: String) {
    /** GET - Retrieve data from server */
    object Get : HttpMethod("GET")
    
    /** POST - Submit data to server */
    object Post : HttpMethod("POST")
    
    /** PUT - Update/replace resource on server */
    object Put : HttpMethod("PUT")
    
    /** DELETE - Delete resource from server */
    object Delete : HttpMethod("DELETE")
    
    /** PATCH - Partially update resource on server */
    object Patch : HttpMethod("PATCH")
}
