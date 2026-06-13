// PURPOSE: The lazy-tabs widget — a tablist whose buttons fetch their panel from the server.
// PURPOSE: One `tabs` Frag renders the initial widget and every patch; selection rides in the markup.
package works.iterative.scalatags.datastar.scenarios

import scalatags.Text.all.*
import works.iterative.scalatags.datastar.Datastar.*
import works.iterative.scalatags.datastar.tapir.*

/** The lazy-tabs example's live fragment.
  *
  * There is no client-side tab state: clicking a tab fires a reverse-routed
  * `@get('/lazy-tabs/{i}')` whose path index identifies the tab, and the server returns the whole
  * widget with the selected tab marked. [[tabs]] renders that widget for both the initial page and
  * every patch, so the `aria-selected` state is always the server's. This is the typed-action
  * analogue of the example's "HATEOAS" point — the action URL carries the tab index as a typed
  * `Int` path parameter.
  */
object LazyTabsView:

    /** The click action for tab `index`, reverse-routed from the typed path route. */
    private def tabAction(index: Int): String = LazyTabsEndpoints.tabRoute.action(index)

    // snippet: lazy-tabs-view
    /** The whole tab widget for the selected index: the tablist (the chosen tab marked
      * `aria-selected`) and its panel body. Carries the `tabs` id so each patch replaces it.
      */
    def tabs(selected: Int): Frag =
        div(id := "tabs", cls := "tabs")(
            div(attr("role") := "tablist")(
                Tabs.titles.zipWithIndex.map: (title, index) =>
                    button(
                        attr("role") := "tab",
                        attr("aria-selected") := (index == selected).toString,
                        dataOn("click") := tabAction(index)
                    )(title)
            ),
            div(attr("role") := "tabpanel")(Tabs.body(selected))
        )

    /** The initial widget shows the first tab selected. */
    val demo: Frag = tabs(0)
    // snippet-end

end LazyTabsView
