// PURPOSE: The animations example — four CSS / View-Transitions techniques, each a small live widget.
// PURPOSE: Stable ids let swaps animate; one fragment per technique renders both initial and patched.
package works.iterative.scalatags.datastar.scenarios

import scalatags.Text.all.*
import works.iterative.scalatags.datastar.Datastar.*
import works.iterative.scalatags.datastar.tapir.*

/** The animations example's live fragment: four techniques data-star.dev demonstrates with only CSS
  * and HTML, each driven by a reverse-routed action and a stable element id so the swap animates.
  *
  *   - **Color throb** keeps `#color-throb` across a feed of colour swaps; a CSS transition tweens.
  *   - **View transitions** swap `#vt-panel` with `useViewTransition`, the browser cross-fading it.
  *   - **Fade out on swap** adds a `.fading` class (CSS opacity) before the server removes the
  *     card.
  *   - **Fade in on addition** appends an item whose `.fade-in-item` class runs a CSS keyframe.
  */
object AnimationsView:

    private val throbAction: String = AnimationsEndpoints.colorThrobRoute.action
    private val viewTransitionAction: String = AnimationsEndpoints.viewTransitionRoute.action
    private val fadeOutAction: String = AnimationsEndpoints.fadeOutRoute.action
    private val fadeInAction: String = AnimationsEndpoints.fadeInRoute.action

    // snippet: animations-view
    /** A throb swatch, kept under the `color-throb` id so each streamed colour transitions in. */
    def colorThrob(color: String): Frag =
        div(id := "color-throb", cls := "throb", style := s"background:$color")("Color throb")

    /** The placeholder: `data-init` opens the throb feed once, on mount. */
    val colorThrobDemo: Frag =
        div(
            id := "color-throb",
            cls := "throb",
            style := s"background:${Animations.throbColors.head}",
            dataInit := throbAction
        )("Color throb")

    /** The view-transition panel, kept under `vt-panel`; `swapped` decides the order, so a swap
      * with `useViewTransition` animates the items moving.
      */
    def vtPanel(swapped: Boolean): Frag =
        val items = if swapped then Animations.panelItems.reverse else Animations.panelItems
        ul(id := "vt-panel", cls := "vt-panel")(items.map(item => li(item)))
    end vtPanel

    /** Swap It! sends the `swapped` signal; the server returns the other order with a view
      * transition and flips the flag, so successive clicks alternate.
      */
    val vtDemo: Frag =
        div(dataSignals := ViewTransition())(
            div(cls := "actions")(button(dataOn("click") := viewTransitionAction)("Swap it!")),
            vtPanel(swapped = false)
        )

    /** The fade-out card, kept under `fade-out-card`; the `fading` class transitions its opacity to
      * zero before the server removes it.
      */
    def fadeOutCard(fading: Boolean): Frag =
        div(id := "fade-out-card", cls := s"fade-card${if fading then " fading" else ""}")(
            p("Fade out then remove me."),
            button(dataOn("click") := fadeOutAction)("Fade out")
        )

    val fadeOutDemo: Frag =
        div(id := "fade-out-demo")(fadeOutCard(fading = false))

    /** A faded-in item; the `fade-in-item` class runs a CSS keyframe on insertion. */
    def fadeInItem(label: String): Frag =
        div(cls := "fade-in-item")(label)

    /** Fade me in appends a fresh item into the `fade-in-list`, each fading itself in. */
    val fadeInDemo: Frag =
        div(id := "fade-in-demo")(
            div(cls := "actions")(button(dataOn("click") := fadeInAction)("Fade me in")),
            div(id := "fade-in-list", cls := "fade-in-list")()
        )

    private def technique(title: String, description: String, widget: Frag): Frag =
        div(cls := "technique")(h4(title), p(description), widget)

    val demo: Frag =
        div(cls := "animations")(
            technique(
                "Color throb",
                "Keep an element's id stable across a swap and a CSS transition tweens between " +
                    "versions.",
                colorThrobDemo
            ),
            technique(
                "View transitions",
                "A swap with the View Transitions API cross-fades the old and new content.",
                vtDemo
            ),
            technique(
                "Fade out on swap",
                "The card fades via a CSS class, then the server removes it.",
                fadeOutDemo
            ),
            technique(
                "Fade in on addition",
                "An appended item runs a CSS keyframe to fade itself in.",
                fadeInDemo
            )
        )
    // snippet-end

end AnimationsView
