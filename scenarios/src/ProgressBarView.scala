// PURPOSE: The progress-bar widget — a placeholder the server fills with a stream of bar states.
// PURPOSE: One-way SSE: data-init opens the feed; each streamed patch replaces the bar by its id.
package works.iterative.scalatags.datastar.scenarios

import scalatags.Text.all.*
import works.iterative.scalatags.datastar.Datastar.*
import works.iterative.scalatags.datastar.tapir.*

/** The progress-bar example's live fragment.
  *
  * `data-init` opens a reverse-routed `@get('/progress-bar/updates')`; the server streams a
  * sequence of `patch-elements` events, each replacing the bar with a higher percentage, then a
  * "try again" button. It exercises a one-way, over-time SSE feed (the `ZStream` form of
  * `datastarStream`), where the server, not the client, drives the updates.
  */
object ProgressBarView:

    /** The updates action, reverse-routed: `@get('/progress-bar/updates')`. */
    private val updatesAction: String = ProgressBarEndpoints.updatesRoute.action

    private def barInner(percent: Int): Frag =
        frag(
            div(cls := "track")(div(cls := "fill", style := s"width:$percent%")),
            span(cls := "label")(s"$percent%")
        )

    // snippet: progress-bar-view
    /** The bar at a given percentage; carries the `progress-bar` id so each streamed patch replaces
      * it outer-by-id.
      */
    def bar(percent: Int): Frag =
        div(id := "progress-bar", cls := "progress")(barInner(percent))

    /** The terminal state: a button whose click re-opens the feed to run it again. */
    val completed: Frag =
        div(id := "progress-bar", cls := "progress")(
            button(dataOn("click") := updatesAction)("Completed! Try again?")
        )

    /** The placeholder: `data-init` opens the updates feed once, on mount. */
    val demo: Frag =
        div(id := "progress-bar", cls := "progress", dataInit := updatesAction)(barInner(0))
    // snippet-end

end ProgressBarView
