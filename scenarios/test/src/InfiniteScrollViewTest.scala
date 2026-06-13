// PURPOSE: Unit tests for the infinite-scroll example — the seeded offset and the sentinel states.
// PURPOSE: Pins the data-on-intersect.once trigger and its inert end marker once exhausted.
package works.iterative.scalatags.datastar.scenarios

import utest.*

object InfiniteScrollViewTest extends TestSuite:

    val tests = Tests:

        test("the widget seeds the offset to one page and shows the first page"):
            val html = InfiniteScrollView.demo.render
            assert(html.contains("""data-signals="{offset: 10}""""))
            assert(html.contains("""id="agents""""))
            assert(html.contains("Agent 1"))
            assert(!html.contains("Agent 11"))

        test("the sentinel arms data-on-intersect.once while rows remain"):
            val html = InfiniteScrollView.sentinel(10).render
            assert(html.contains("""id="sentinel""""))
            assert(html.contains("""data-on-intersect__once="@get('/infinite-scroll/more')""""))

        test("the sentinel becomes an inert end marker once exhausted"):
            val html = InfiniteScrollView.sentinel(Agents.all.size).render
            assert(html.contains("That's everyone."))
            assert(!html.contains("data-on-intersect"))

    end tests

end InfiniteScrollViewTest
