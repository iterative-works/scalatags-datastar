// PURPOSE: The counter widget — typed Datastar signals, a typed expression, and a reverse-routed action.
// PURPOSE: One Scalatags fragment; clicking the button drives the whole signal round trip over SSE.
package works.iterative.scalatags.datastar.scenarios

import scalatags.Text.all.*
import works.iterative.scalatags.datastar.Datastar.*
import works.iterative.scalatags.datastar.Signals

/** The counter example's live fragment.
  *
  * Everything the page needs from the library shows up here: the initial store from the `Counter`
  * case class (`data-signals`), a typed signal reference for the live value (`data-text="$count"`),
  * and a button whose action is reverse-routed from a Tapir endpoint (`@post('/increment')`), so it
  * can only name a route that exists. The gallery embeds this fragment beside its source.
  */
object CounterView:

    // snippet: counter-view
    /** The click action, reverse-routed from the increment route: `@post('/increment')`. */
    private val incrementAction: String =
        works.iterative.scalatags.datastar.tapir.EndpointAction
            .action(CounterEndpoints.incrementRoute)(())

    /** The interactive counter: the seeded signal store, the live count, and the increment button.
      */
    val demo: Frag =
        div(dataSignals := Signals.encode(Counter()))(
            p("Count: ", span(id := "count", dataText := Counter.count)),
            button(dataOn("click") := incrementAction)("Increment")
        )
    // snippet-end

end CounterView
