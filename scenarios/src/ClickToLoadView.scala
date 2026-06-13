// PURPOSE: The click-to-load widget — a table whose Load-more button appends the next page.
// PURPOSE: Shared `rows`/`loadMore` fragments render the initial page and every appended patch.
package works.iterative.scalatags.datastar.scenarios

import scalatags.Text.all.*
import works.iterative.scalatags.datastar.Datastar.*
import works.iterative.scalatags.datastar.tapir.*

/** The click-to-load example's live fragment.
  *
  * The table's body carries the `agents` id; the Load-more button fires a reverse-routed `@get`,
  * sending the offset in its signal store. The server appends the next page's rows into `#agents`,
  * patches the offset signal forward, and re-renders the button (or an end message). [[rows]] and
  * [[loadMore]] render both the initial page and every patch.
  */
object ClickToLoadView:

    /** The load action, reverse-routed: `@get('/click-to-load/more')`. */
    private val moreAction: String = ClickToLoadEndpoints.moreRoute.action

    // snippet: click-to-load-view
    /** The table rows for a page of agents — appended into the `agents` tbody by id. */
    def rows(agents: Seq[Agent]): Frag =
        frag(agents.map(agent => tr(td(agent.name), td(agent.email))))

    /** The Load-more control, keyed `load-more` so each patch replaces it: a button while rows
      * remain beyond `offset`, otherwise an end message.
      */
    def loadMore(offset: Int): Frag =
        div(id := "load-more")(
            if Agents.hasMore(offset) then button(dataOn("click") := moreAction)("Load more")
            else span(cls := "done")("That's everyone.")
        )

    val demo: Frag =
        div(dataSignals := ClickToLoad())(
            table(
                thead(tr(th("Name"), th("Email"))),
                tbody(id := "agents")(rows(Agents.page(0)))
            ),
            loadMore(Agents.pageSize)
        )
    // snippet-end

end ClickToLoadView
