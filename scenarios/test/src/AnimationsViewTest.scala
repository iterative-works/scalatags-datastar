// PURPOSE: Unit tests for the animations example — the four technique widgets and their fragments.
// PURPOSE: The timed feeds and toggles are left to the routes test; here the pure rendered pieces.
package works.iterative.scalatags.datastar.scenarios

import utest.*

object AnimationsViewTest extends TestSuite:

    val tests = Tests:

        test("color throb opens its feed on init, keyed by a stable id"):
            val html = AnimationsView.colorThrobDemo.render
            assert(html.contains("""id="color-throb""""))
            assert(html.contains("""data-init="@get('/animations/color-throb')""""))

        test("a throb swatch carries the stable id and its colour"):
            val html = AnimationsView.colorThrob("#dc2626").render
            assert(html.contains("""id="color-throb""""))
            assert(html.contains("#dc2626"))

        test("view transitions seeds the swapped signal and a swap-it action"):
            val html = AnimationsView.vtDemo.render
            assert(html.contains("data-signals"))
            assert(html.contains("swapped"))
            assert(html.contains("""data-on:click="@get('/animations/view-transition')""""))
            assert(html.contains("""id="vt-panel""""))

        test("the view-transition panel orders its items by the swapped flag"):
            val asIs = AnimationsView.vtPanel(swapped = false).render
            val swapped = AnimationsView.vtPanel(swapped = true).render
            assert(asIs.contains("""id="vt-panel""""))
            assert(asIs != swapped)

        test("fade out on swap guards a delete that the server fades then removes"):
            val html = AnimationsView.fadeOutDemo.render
            assert(html.contains("""id="fade-out-card""""))
            assert(html.contains("""data-on:click="@delete('/animations/fade-out')""""))

        test("a fade-out card flags the fading state only when asked"):
            assert(!AnimationsView.fadeOutCard(fading = false).render.contains("fading"))
            assert(AnimationsView.fadeOutCard(fading = true).render.contains("fading"))

        test("fade in on addition appends into a list keyed for the append patch"):
            val html = AnimationsView.fadeInDemo.render
            assert(html.contains("""id="fade-in-list""""))
            assert(html.contains("""data-on:click="@get('/animations/fade-in')""""))

        test("a faded-in item carries the fade-in class and its label"):
            val html = AnimationsView.fadeInItem("Item 7").render
            assert(html.contains("fade-in-item"))
            assert(html.contains("Item 7"))

        test("the composite demo shows all four techniques"):
            val html = AnimationsView.demo.render
            assert(html.contains("""id="color-throb""""))
            assert(html.contains("""id="vt-panel""""))
            assert(html.contains("""id="fade-out-card""""))
            assert(html.contains("""id="fade-in-list""""))

    end tests

end AnimationsViewTest
