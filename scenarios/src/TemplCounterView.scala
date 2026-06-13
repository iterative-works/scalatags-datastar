// PURPOSE: The templ-counter widget — a shared count that lazy-loads and an increment button.
// PURPOSE: The count is server state, so two browser tabs see the same value advance.
package works.iterative.scalatags.datastar.scenarios

import scalatags.Text.all.*
import works.iterative.scalatags.datastar.Datastar.*
import works.iterative.scalatags.datastar.tapir.*

/** The templ-counter example's live fragment.
  *
  * The count is held on the server, so it is shared across visitors: the span lazy-loads the
  * current value on init, and the button's reverse-routed `@post` advances the shared counter and
  * patches the new value back. Open the page in two tabs to see both reflect each click.
  */
object TemplCounterView:

    private val countAction: String = TemplCounterEndpoints.countRoute.action
    private val incrementAction: String = TemplCounterEndpoints.incrementRoute.action

    // snippet: templ-counter-view
    /** The shared count, keyed `templ-count` so each patch replaces it. */
    def count(value: Int): Frag = span(id := "templ-count")(value.toString)

    val demo: Frag =
        div(cls := "templ-counter")(
            p("A counter shared by every visitor — open this page in two tabs."),
            p("Total clicks: ", span(id := "templ-count", dataInit := countAction)("…")),
            button(dataOn("click") := incrementAction)("Increment")
        )
    // snippet-end

end TemplCounterView
