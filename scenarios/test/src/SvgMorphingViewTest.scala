// PURPOSE: Unit tests for the svg-morphing example — the circle SVG and the morph button.
// PURPOSE: Pins the keyed svg element, its colour/radius attributes, and the reverse-routed action.
package works.iterative.scalatags.datastar.scenarios

import utest.*

object SvgMorphingViewTest extends TestSuite:

    val tests = Tests:

        test("circle renders an svg with the given colour and radius, keyed by id"):
            val html = SvgMorphingView.circle("#ff0000", 25).render
            assert(html.contains("""id="circle""""))
            assert(html.contains("<circle"))
            assert(html.contains("""fill="#ff0000""""))
            assert(html.contains("""r="25""""))

        test("the widget shows the morph button wired to the reverse-routed action"):
            val html = SvgMorphingView.demo.render
            assert(html.contains("<svg"))
            assert(html.contains("""data-on:click="@get('/svg-morphing/morph')""""))

    end tests

end SvgMorphingViewTest
