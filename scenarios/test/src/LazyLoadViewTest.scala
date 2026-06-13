// PURPOSE: Unit tests for the lazy-load example's pure pieces — the placeholder and the fragment.
// PURPOSE: Pins that data-init fires the reverse-routed action and the loaded fragment shares the id.
package works.iterative.scalatags.datastar.scenarios

import utest.*

object LazyLoadViewTest extends TestSuite:

    val tests = Tests:

        test("the placeholder fires the reverse-routed load action on init"):
            val html = LazyLoadView.demo.render
            assert(html.contains("""id="graph""""))
            assert(html.contains("""data-init="@get('/lazy-load/graph')""""))
            assert(html.contains("Loading"))

        test("the loaded fragment keeps the graph id and carries no data-init"):
            val html = LazyLoadView.graph.render
            assert(html.contains("""id="graph""""))
            assert(html.contains("Quarterly revenue"))
            assert(!html.contains("data-init"))

    end tests

end LazyLoadViewTest
