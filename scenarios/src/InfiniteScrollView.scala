// PURPOSE: The infinite-scroll widget — a table with a sentinel that loads the next page on sight.
// PURPOSE: data-on-intersect.once re-arms with each patched sentinel, fetching pages as you scroll.
package works.iterative.scalatags.datastar.scenarios

import scalatags.Text.all.*
import works.iterative.scalatags.datastar.Datastar.*
import works.iterative.scalatags.datastar.tapir.*

/** The infinite-scroll example's live fragment.
  *
  * Below the table sits a sentinel carrying `data-on-intersect.once`, so when it scrolls into view
  * the reverse-routed `@get` fires once and the server appends the next page. The server re-renders
  * the sentinel each time — a fresh `.once` re-arms it for the page after — until the catalogue is
  * exhausted, when it patches an inert end marker instead.
  */
object InfiniteScrollView:

    /** The load action, reverse-routed: `@get('/infinite-scroll/more')`. */
    private val moreAction: String = InfiniteScrollEndpoints.moreRoute.action

    // snippet: infinite-scroll-view
    /** The table rows for a page of agents — appended into the `agents` tbody by id. */
    def rows(agents: Seq[Agent]): Frag =
        frag(agents.map(agent => tr(td(agent.name), td(agent.email))))

    /** The sentinel, keyed `sentinel` so each patch replaces it. While rows remain it carries
      * `data-on-intersect.once` to fetch the next page as it scrolls into view; at the end it is an
      * inert marker, so the feed stops.
      */
    def sentinel(offset: Int): Frag =
        if Agents.hasMore(offset) then
            div(id := "sentinel", cls := "sentinel", dataOnIntersect.once := moreAction)(
                "Loading more…"
            )
        else div(id := "sentinel", cls := "sentinel")("That's everyone.")

    val demo: Frag =
        div(dataSignals := InfiniteScroll())(
            div(cls := "scroller")(
                table(
                    thead(tr(th("Name"), th("Email"))),
                    tbody(id := "agents")(rows(Agents.page(0)))
                ),
                sentinel(Agents.pageSize)
            )
        )
    // snippet-end

end InfiniteScrollView
