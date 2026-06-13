// PURPOSE: Unit tests for the click-to-load example — the seeded offset, the table, the catalogue.
// PURPOSE: Pins the offset-in-store pagination and the Load-more control's two rendered states.
package works.iterative.scalatags.datastar.scenarios

import utest.*

object ClickToLoadViewTest extends TestSuite:

    val tests = Tests:

        test("the widget seeds the offset to one page and shows the first page"):
            val html = ClickToLoadView.demo.render
            assert(html.contains("""data-signals="{offset: 10}""""))
            assert(html.contains("""id="agents""""))
            assert(html.contains("Agent 1"))
            assert(html.contains("Agent 10"))
            assert(!html.contains("Agent 11"))

        test("the Load-more button reverse-routes the page action while rows remain"):
            val html = ClickToLoadView.loadMore(10).render
            assert(html.contains("""id="load-more""""))
            assert(html.contains("""data-on:click="@get('/click-to-load/more')""""))

        test("the control becomes an end message once the catalogue is exhausted"):
            val html = ClickToLoadView.loadMore(Agents.all.size).render
            assert(html.contains("That's everyone."))
            assert(!html.contains("data-on:click"))

        test("Agents.page slices the catalogue and hasMore tracks the end"):
            assert(Agents.page(0).map(_.name) == (1 to 10).map(i => s"Agent $i"))
            assert(Agents.page(10).head.name == "Agent 11")
            assert(Agents.page(60).isEmpty)
            assert(Agents.hasMore(0))
            assert(!Agents.hasMore(60))

    end tests

end ClickToLoadViewTest
