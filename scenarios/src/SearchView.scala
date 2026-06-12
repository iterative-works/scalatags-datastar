// PURPOSE: The live-search widget — a debounced bound input and a reusable results fragment.
// PURPOSE: The same `results` Frag renders the initial list and every SSE patch, in one template.
package works.iterative.scalatags.datastar.scenarios

import scalatags.Text.all.*
import scala.concurrent.duration.*
import works.iterative.scalatags.datastar.Datastar.*
import works.iterative.scalatags.datastar.tapir.*

/** The live-search example's live fragment.
  *
  * The input two-way binds to the `query` signal (`data-bind="query"`) and, on a debounced
  * keystroke, fires the reverse-routed search action (`@get('/search/results')`). The crucial piece
  * is [[results]]: one fragment renders the list both for the initial page and for the SSE
  * `patch-elements` event the server streams back, so the two can never diverge.
  */
object SearchView:

    /** The search action, reverse-routed from the search route: `@get('/search/results')`. */
    private val searchAction: String = SearchEndpoints.searchRoute.action

    // snippet: search-view
    /** The result list. Carries the `results` id so a default `patch-elements` event replaces it by
      * id; an empty match set keeps the element (and id) and shows a message instead.
      */
    def results(matches: Seq[String]): Frag =
        val items: Seq[Frag] =
            if matches.isEmpty then Seq(li("No matches."))
            else matches.map(name => li(name))
        ul(id := "results")(items)
    end results

    /** The interactive search: an input two-way bound to the `query` signal that fires a debounced,
      * reverse-routed `@get` action, over the catalogue rendered by [[results]].
      */
    val demo: Frag =
        div(dataSignals := Search())(
            input(
                `type` := "search",
                placeholder := "Filter languages…",
                dataBind := Search.query,
                dataOn("input").debounce(300.millis) := searchAction
            ),
            results(Languages.all)
        )
    // snippet-end

end SearchView
