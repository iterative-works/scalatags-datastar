// PURPOSE: The svg-morphing widget — an inline SVG circle and a button that randomises it.
// PURPOSE: The circle is patched with the svg namespace, so the new element is created correctly.
package works.iterative.scalatags.datastar.scenarios

import scalatags.Text.all.*
import works.iterative.scalatags.datastar.Datastar.*
import works.iterative.scalatags.datastar.tapir.*

/** The svg-morphing example's live fragment.
  *
  * Clicking Morph fires a reverse-routed `@get`; the server streams a `patch-elements` event whose
  * fragment is a fresh `<svg id="circle">` with a random colour and radius, patched with `namespace
  * \= svg` so the browser creates it in the SVG namespace. [[circle]] renders both the initial
  * circle and every morph.
  */
object SvgMorphingView:

    /** The morph action, reverse-routed: `@get('/svg-morphing/morph')`. */
    private val morphAction: String = SvgMorphingEndpoints.morphRoute.action

    // snippet: svg-morphing-view
    /** The circle SVG, keyed `circle` so each morph replaces it by id. */
    def circle(color: String, radius: Int): Frag =
        tag("svg")(
            id := "circle",
            attr("viewBox") := "0 0 100 100",
            attr("width") := "140",
            attr("height") := "140"
        )(
            tag("circle")(
                attr("cx") := "50",
                attr("cy") := "50",
                attr("r") := radius.toString,
                attr("fill") := color
            )
        )

    val demo: Frag =
        div(cls := "svg-morphing")(
            circle("#2563eb", 30),
            div(cls := "actions")(button(dataOn("click") := morphAction)("Morph"))
        )
    // snippet-end

end SvgMorphingView
