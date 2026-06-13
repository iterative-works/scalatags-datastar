// PURPOSE: The progressive-load widget — a one-shot Load button and the section placeholders.
// PURPOSE: Each section's placeholder shares its id, so the streamed patches fill them in place.
package works.iterative.scalatags.datastar.scenarios

import scalatags.Text.all.*
import works.iterative.scalatags.datastar.Datastar.*
import works.iterative.scalatags.datastar.tapir.*

/** The progressive-load example's live fragment.
  *
  * The Load button sets `$loadDisabled` and fires the reverse-routed feed in one click expression,
  * and binds its own `disabled` attribute to that signal so it cannot fire twice. The server then
  * streams the four sections back in random order, each `patch-elements` event filling the matching
  * placeholder by id. [[section]] renders a loaded section for both — though here the placeholders
  * start empty, so only the patches carry content.
  */
object ProgressiveLoadView:

    /** The updates action, reverse-routed: `@get('/progressive-load/updates')`. */
    private val updatesAction: String = ProgressiveLoadEndpoints.updatesRoute.action

    // snippet: progressive-load-view
    /** A loaded section, keyed by its id so the streamed patch replaces its placeholder. */
    def section(loaded: Section): Frag =
        div(id := loaded.id, cls := "section")(h3(loaded.title), p(loaded.body))

    val demo: Frag =
        div(dataSignals := ProgressiveLoad())(
            button(
                dataOn("click") := s"$$loadDisabled = true; $updatesAction",
                dataAttr("disabled") := ProgressiveLoad.loadDisabled
            )("Load"),
            div(cls := "sections")(
                Sections.all.map(s =>
                    div(id := s.id, cls := "section")(em(s"${s.title} — not loaded"))
                )
            )
        )
    // snippet-end

end ProgressiveLoadView
