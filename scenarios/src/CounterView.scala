// PURPOSE: The counter page template — typed Datastar signals, expression and reverse-routed action.
// PURPOSE: One Scalatags template; clicking the button drives the whole signal round trip over SSE.
package works.iterative.scalatags.datastar.scenarios

import scalatags.Text.all.*
import works.iterative.scalatags.datastar.Datastar.*
import works.iterative.scalatags.datastar.Signals

/** Renders the counter page.
  *
  * Everything the page needs from the library shows up here: the initial store from the `Counter`
  * case class (`data-signals`), a typed signal reference for the live value (`data-text="$count"`),
  * and a button whose action is reverse-routed from a Tapir endpoint (`@post('/increment')`), so it
  * can only name a route that exists.
  */
object CounterView:

    /** The Datastar client, pinned to the release whose wire format the SSE codec targets
      * (`datastar-patch-signals`); npm's "latest" tag still ships the older `merge-signals` names.
      */
    private val datastarScript =
        "https://cdn.jsdelivr.net/gh/starfederation/datastar@v1.0.2/bundles/datastar.js"

    /** The click action, reverse-routed from the increment route: `@post('/increment')`. */
    private val incrementAction: String =
        works.iterative.scalatags.datastar.tapir.EndpointAction
            .action(CounterEndpoints.incrementRoute)(())

    /** The full page as an HTML string. */
    def page: String =
        "<!DOCTYPE html>" + html(lang := "en")(
            head(
                meta(attr("charset") := "utf-8"),
                meta(
                    name := "viewport",
                    attr("content") := "width=device-width, initial-scale=1"
                ),
                tag("title")("Datastar counter"),
                script(src := datastarScript, `type` := "module")
            ),
            body(
                div(dataSignals := Signals.encode(Counter()))(
                    h1("Counter"),
                    p(
                        "Count: ",
                        span(id := "count", dataText := Counter.count)
                    ),
                    button(dataOn("click") := incrementAction)("Increment")
                )
            )
        ).render

end CounterView
