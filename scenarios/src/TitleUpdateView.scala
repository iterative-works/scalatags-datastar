// PURPOSE: The title-update widget — a button whose action patches the document <title> over SSE.
// PURPOSE: Demonstrates patching a head element by CSS selector, not just elements in the body.
package works.iterative.scalatags.datastar.scenarios

import scalatags.Text.all.*
import works.iterative.scalatags.datastar.Datastar.*
import works.iterative.scalatags.datastar.tapir.*

/** The title-update example's live fragment.
  *
  * Clicking the button fires a reverse-routed `@post('/title-update')`; the server answers with a
  * `patch-elements` event whose `selector` targets the `<title>` element, so the browser tab title
  * changes. It shows that the SSE codec patches *any* element addressed by selector, including ones
  * in the document head.
  */
object TitleUpdateView:

    /** The update action, reverse-routed from the update route: `@post('/title-update')`. */
    private val updateAction: String = TitleUpdateEndpoints.updateRoute.action

    // snippet: title-update-view
    val demo: Frag =
        div(
            p(
                "Click to set the document title to the server's current time — watch the browser tab."
            ),
            button(dataOn("click") := updateAction)("Update title")
        )
    // snippet-end

end TitleUpdateView
