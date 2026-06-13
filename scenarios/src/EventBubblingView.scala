// PURPOSE: The event-bubbling widget — one handler on the list reads which child was clicked.
// PURPOSE: Pure client-side: a single data-on:click uses evt.target rather than one handler per item.
package works.iterative.scalatags.datastar.scenarios

import scalatags.Text.all.*
import works.iterative.scalatags.datastar.Datastar.*

/** The event-bubbling example's live fragment.
  *
  * Rather than a handler on every item, one `data-on:click` on the list reads `evt.target` (the
  * actual element clicked, via event bubbling) and records it in a signal. No server is involved.
  */
object EventBubblingView:

    // snippet: event-bubbling-view
    val demo: Frag =
        div(dataSignals := "{picked: '(none)'}")(
            p("Click an item — a single handler on the list reads the target:"),
            ul(dataOn("click") := "$picked = evt.target.closest('li')?.textContent ?? $picked")(
                li("Apple"),
                li("Banana"),
                li("Cherry"),
                li("Date")
            ),
            p("You picked: ", strong(dataText := "$picked"))
        )
    // snippet-end

end EventBubblingView
