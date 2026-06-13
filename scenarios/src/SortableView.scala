// PURPOSE: The sortable widget — a draggable list (SortableJS) whose order updates a signal.
// PURPOSE: Client-only: data-init wires the third-party lib, data-on reads the reordered DOM.
package works.iterative.scalatags.datastar.scenarios

import scalatags.Text.all.*
import works.iterative.scalatags.datastar.Datastar.*

/** The sortable example's live fragment.
  *
  * data-init runs a JavaScript expression on mount, so it can wire a third-party library: here it
  * makes the list draggable with SortableJS and has the library dispatch a `reordered` event on
  * drop. `data-on:reordered` then reads the new order out of the DOM into a signal. The integration
  * point is plain JS; what the library renders is the typed triggers around it. No server is
  * involved.
  */
object SortableView:

    /** SortableJS, pinned like the gallery's other app-only CDN assets. */
    val sortableScript = "https://cdn.jsdelivr.net/npm/sortablejs@1.15.6/Sortable.min.js"

    private val initSortable: String =
        "new Sortable(el, {animation: 150, onEnd: () => " +
            "el.dispatchEvent(new CustomEvent('reordered', {bubbles: true}))})"

    private val readOrder: String =
        "$order = [...el.children].map(item => item.dataset.id).join(', ')"

    private def item(id: String, label0: String): Frag =
        li(cls := "sortable-item", attr("data-id") := id)(label0)

    // snippet: sortable-view
    val demo: Frag =
        div(dataSignals := "{order: 'one, two, three, four'}")(
            script(src := sortableScript),
            p("Drag to reorder — the order signal follows:"),
            ul(id := "sortable", dataInit := initSortable, dataOn("reordered") := readOrder)(
                item("one", "One"),
                item("two", "Two"),
                item("three", "Three"),
                item("four", "Four")
            ),
            p("Order: ", strong(dataText := "$order"))
        )
    // snippet-end

end SortableView
