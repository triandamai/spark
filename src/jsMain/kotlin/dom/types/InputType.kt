package dom.types

/**
 * Type-safe representation of HTML input types.
 * Use this instead of raw strings to get compile-time safety and IDE autocomplete.
 * 
 * Example:
 * ```
 * input {
 *     type(InputType.Email)
 *     placeholder("Enter your email")
 * }
 * ```
 */
sealed class InputType(val value: String) {
    /** Text input - single line text field */
    object Text : InputType("text")
    
    /** Password input - obscured text field */
    object Password : InputType("password")
    
    /** Email input - validates email format */
    object Email : InputType("email")
    
    /** Number input - numeric values only */
    object Number : InputType("number")
    
    /** Telephone input - for phone numbers */
    object Tel : InputType("tel")
    
    /** URL input - validates URL format */
    object Url : InputType("url")
    
    /** Search input - for search fields */
    object Search : InputType("search")
    
    /** Date input - date picker */
    object Date : InputType("date")
    
    /** Time input - time picker */
    object Time : InputType("time")
    
    /** DateTime input - date and time picker */
    object DateTime : InputType("datetime-local")
    
    /** Month input - month and year picker */
    object Month : InputType("month")
    
    /** Week input - week picker */
    object Week : InputType("week")
    
    /** Color input - color picker */
    object Color : InputType("color")
    
    /** File input - file upload */
    object File : InputType("file")
    
    /** Hidden input - not visible to user */
    object Hidden : InputType("hidden")
    
    /** Range input - slider control */
    object Range : InputType("range")
    
    /** Checkbox input - boolean toggle */
    object Checkbox : InputType("checkbox")
    
    /** Radio input - single selection from group */
    object Radio : InputType("radio")
    
    /** Submit button - submits form */
    object Submit : InputType("submit")
    
    /** Reset button - resets form */
    object Reset : InputType("reset")
    
    /** Button - generic button */
    object Button : InputType("button")
}
