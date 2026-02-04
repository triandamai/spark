package dom.types

/**
 * Type-safe representation of DOM events.
 * Use this instead of raw strings to get compile-time safety and IDE autocomplete.
 * 
 * Example:
 * ```
 * button {
 *     on(DomEvent.Click) { event ->
 *         println("Button clicked!")
 *     }
 * }
 * ```
 */
sealed class DomEvent(val value: String) {
    // Mouse Events
    /** Fired when an element is clicked */
    object Click : DomEvent("click")
    
    /** Fired when an element is double-clicked */
    object DblClick : DomEvent("dblclick")
    
    /** Fired when a mouse button is pressed down */
    object MouseDown : DomEvent("mousedown")
    
    /** Fired when a mouse button is released */
    object MouseUp : DomEvent("mouseup")
    
    /** Fired when the mouse pointer moves over an element */
    object MouseMove : DomEvent("mousemove")
    
    /** Fired when the mouse pointer enters an element */
    object MouseEnter : DomEvent("mouseenter")
    
    /** Fired when the mouse pointer leaves an element */
    object MouseLeave : DomEvent("mouseleave")
    
    /** Fired when the mouse pointer moves onto an element */
    object MouseOver : DomEvent("mouseover")
    
    /** Fired when the mouse pointer moves out of an element */
    object MouseOut : DomEvent("mouseout")
    
    /** Fired when the context menu is triggered (right-click) */
    object ContextMenu : DomEvent("contextmenu")
    
    // Keyboard Events
    /** Fired when a key is pressed down */
    object KeyDown : DomEvent("keydown")
    
    /** Fired when a key is released */
    object KeyUp : DomEvent("keyup")
    
    /** Fired when a key is pressed (deprecated but still supported) */
    object KeyPress : DomEvent("keypress")
    
    // Form Events
    /** Fired when a form is submitted */
    object Submit : DomEvent("submit")
    
    /** Fired when the value of an input element changes */
    object Change : DomEvent("change")
    
    /** Fired when an input element receives input */
    object Input : DomEvent("input")
    
    /** Fired when an element receives focus */
    object Focus : DomEvent("focus")
    
    /** Fired when an element loses focus */
    object Blur : DomEvent("blur")
    
    /** Fired when a form is reset */
    object Reset : DomEvent("reset")
    
    /** Fired when text is selected */
    object Select : DomEvent("select")
    
    // Touch Events
    /** Fired when a touch point is placed on the touch surface */
    object TouchStart : DomEvent("touchstart")
    
    /** Fired when a touch point is moved along the touch surface */
    object TouchMove : DomEvent("touchmove")
    
    /** Fired when a touch point is removed from the touch surface */
    object TouchEnd : DomEvent("touchend")
    
    /** Fired when a touch event is interrupted */
    object TouchCancel : DomEvent("touchcancel")
    
    // Drag Events
    /** Fired when an element starts being dragged */
    object DragStart : DomEvent("dragstart")
    
    /** Fired when an element is being dragged */
    object Drag : DomEvent("drag")
    
    /** Fired when a drag operation ends */
    object DragEnd : DomEvent("dragend")
    
    /** Fired when a dragged element enters a valid drop target */
    object DragEnter : DomEvent("dragenter")
    
    /** Fired when a dragged element is over a valid drop target */
    object DragOver : DomEvent("dragover")
    
    /** Fired when a dragged element leaves a valid drop target */
    object DragLeave : DomEvent("dragleave")
    
    /** Fired when an element is dropped on a valid drop target */
    object Drop : DomEvent("drop")
    
    // Media Events
    /** Fired when media playback starts */
    object Play : DomEvent("play")
    
    /** Fired when media playback is paused */
    object Pause : DomEvent("pause")
    
    /** Fired when media playback ends */
    object Ended : DomEvent("ended")
    
    /** Fired when media volume changes */
    object VolumeChange : DomEvent("volumechange")
    
    // Other Events
    /** Fired when a resource and its dependent resources have finished loading */
    object Load : DomEvent("load")
    
    /** Fired when a resource failed to load */
    object Error : DomEvent("error")
    
    /** Fired when an element's scrollbar is being scrolled */
    object Scroll : DomEvent("scroll")
    
    /** Fired when the window is resized */
    object Resize : DomEvent("resize")
    
    /** Fired when the page is about to be unloaded */
    object BeforeUnload : DomEvent("beforeunload")
    
    /** Fired when the page has been unloaded */
    object Unload : DomEvent("unload")
    
    /** Fired when the hash portion of the URL changes */
    object HashChange : DomEvent("hashchange")
    
    /** Fired when the browser's history changes */
    object PopState : DomEvent("popstate")
}
