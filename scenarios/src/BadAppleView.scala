// PURPOSE: The bad-apple widget — a <pre> the server fills with a stream of ASCII frames.
// PURPOSE: data-init opens the feed; each frame is a patch-elements replacing the <pre> by id.
package works.iterative.scalatags.datastar.scenarios

import scalatags.Text.all.*
import works.iterative.scalatags.datastar.Datastar.*
import works.iterative.scalatags.datastar.tapir.*

/** The bad-apple example's live fragment.
  *
  * data-init opens a reverse-routed feed; the server streams a sequence of `patch-elements` events,
  * each replacing the `<pre>` with the next animation frame — the same one-way, server-driven feed
  * the original uses to play a video, scaled down to a tiny ASCII loop.
  */
object BadAppleView:

    private val playAction: String = BadAppleEndpoints.playRoute.action

    // snippet: bad-apple-view
    /** One animation frame, keyed `frame` so each streamed patch replaces it. */
    def frame(art: String): Frag = pre(id := "frame", cls := "ascii")(art)

    val demo: Frag =
        div(cls := "bad-apple")(
            p("A server-streamed ASCII animation — one patch per frame:"),
            pre(id := "frame", cls := "ascii", dataInit := playAction)("Loading…")
        )
    // snippet-end

end BadAppleView
