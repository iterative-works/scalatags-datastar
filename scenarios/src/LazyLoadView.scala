// PURPOSE: The lazy-load widget — a placeholder that fetches its real content once on init.
// PURPOSE: data-init fires a @get whose streamed fragment replaces the placeholder by its id.
package works.iterative.scalatags.datastar.scenarios

import scalatags.Text.all.*
import works.iterative.scalatags.datastar.Datastar.*
import works.iterative.scalatags.datastar.tapir.*

/** The lazy-load example's live fragment.
  *
  * The simplest round trip in the gallery: a placeholder element carries `data-init`, so as soon as
  * Datastar mounts it the reverse-routed `@get` fires; the server streams back a `patch-elements`
  * event whose fragment carries the same `graph` id, so it replaces the placeholder outer-by-id.
  */
object LazyLoadView:

    /** The load action, reverse-routed from the graph route: `@get('/lazy-load/graph')`. */
    private val graphAction: String = LazyLoadEndpoints.graphRoute.action

    // snippet: lazy-load-view
    /** The loaded content — a small bar chart. Keeps the `graph` id so the patch replaces the
      * placeholder; carries no `data-init`, so it does not re-fire.
      */
    val graph: Frag =
        div(id := "graph", cls := "graph")(
            h3("Quarterly revenue"),
            div(cls := "bars")(
                Seq(40, 72, 55, 90, 64).map(height =>
                    div(cls := "bar", style := s"height:${height}px")
                )
            )
        )

    /** The placeholder: `data-init` fires the load action once, on mount. */
    val demo: Frag =
        div(id := "graph", dataInit := graphAction)("Loading…")
    // snippet-end

end LazyLoadView
