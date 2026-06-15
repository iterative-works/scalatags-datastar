// PURPOSE: Unit tests for the progressive-load example — the gated button and the section fragments.
// PURPOSE: Pins the composed click expression, the disabled binding, and the four section ids.
package works.iterative.scalatags.datastar.scenarios

import utest.*

object ProgressiveLoadViewTest extends TestSuite:

    val tests = Tests:

        test("the widget seeds the load-disabled signal store"):
            assert(
                ProgressiveLoadView.demo.render.contains("""data-signals="{loadDisabled: false}"""")
            )

        test("the button sets the signal and fires the feed, and binds its own disabled state"):
            val html = ProgressiveLoadView.demo.render
            assert(html.contains(
                """data-on:click="$loadDisabled = true; @get('/progressive-load/updates')""""
            ))
            assert(html.contains("""data-attr:disabled="$loadDisabled""""))

        test("a loaded section is keyed by its id"):
            val html = ProgressiveLoadView.section(Sections.all.head).render
            assert(html.contains("""id="header""""))
            assert(html.contains("Header"))

        test("the catalogue has the four expected section ids"):
            assert(Sections.all.map(_.id) == Seq("header", "article", "comments", "footer"))

    end tests

end ProgressiveLoadViewTest
