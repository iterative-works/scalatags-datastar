// PURPOSE: The dbmon widget — rate controls and a table the server updates rapidly over SSE.
// PURPOSE: data-init opens the feed; changing a control re-opens it at the new rate (signals ride along).
package works.iterative.scalatags.datastar.scenarios

import scalatags.Text.all.*
import works.iterative.scalatags.datastar.Datastar.*
import works.iterative.scalatags.datastar.tapir.*

/** The dbmon example's live fragment.
  *
  * A database-monitor benchmark: the table body opens a server feed on init and the server keeps
  * patching it with fresh, randomised statistics. Two bound number inputs control the frame rate
  * and the fraction of rows that change each frame; because they are signals, a `data-on:change`
  * re-fires the updates action and the feed re-opens at the new rate. (Per-cell focus/blur editing
  * from the original is omitted.)
  */
object DbmonView:

    private val updatesAction: String = DbmonEndpoints.updatesRoute.action

    // snippet: dbmon-view
    /** One cluster row. */
    def row(database: Database): Frag =
        tr(
            td(database.name),
            td(database.queries.toString),
            td(f"${database.slowest}%.2f ms")
        )

    def rows(databases: Seq[Database]): Frag = frag(databases.map(row))

    val demo: Frag =
        div(dataSignals := Dbmon())(
            div(cls := "controls")(
                label("Mutation rate %"),
                input(
                    `type` := "number",
                    dataBind := Dbmon.mutationRate,
                    dataOn("change") := updatesAction
                ),
                label("FPS"),
                input(`type` := "number", dataBind := Dbmon.fps, dataOn("change") := updatesAction)
            ),
            table(
                thead(tr(th("Database"), th("Queries"), th("Slowest"))),
                tbody(id := "databases", dataInit := updatesAction)
            )
        )
    // snippet-end

end DbmonView
