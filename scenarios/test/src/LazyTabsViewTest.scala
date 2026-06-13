// PURPOSE: Unit tests for the lazy-tabs example — the widget marks exactly the selected tab active.
// PURPOSE: Pins the typed Int path-parameter action and the server-managed aria-selected state.
package works.iterative.scalatags.datastar.scenarios

import utest.*

object LazyTabsViewTest extends TestSuite:

    private def selectedCount(html: String): Int =
        html.split("""aria-selected="true"""", -1).length - 1

    val tests = Tests:

        test("the widget reverse-routes each tab to its typed Int path action"):
            val html = LazyTabsView.tabs(0).render
            assert(html.contains("""data-on:click="@get('/lazy-tabs/0')""""))
            assert(html.contains("""data-on:click="@get('/lazy-tabs/3')""""))

        test("exactly the selected tab is marked aria-selected"):
            assert(selectedCount(LazyTabsView.tabs(0).render) == 1)
            assert(selectedCount(LazyTabsView.tabs(5).render) == 1)
            // the chosen tab carries the true; the rest are false
            val html = LazyTabsView.tabs(5).render
            assert(html.split("""aria-selected="false"""", -1).length - 1 == Tabs.titles.size - 1)

        test("the panel shows the selected tab's body"):
            assert(LazyTabsView.tabs(3).render.contains("content of tab 3"))

        test("the initial widget shows the first tab"):
            assert(selectedCount(LazyTabsView.demo.render) == 1)
            assert(LazyTabsView.demo.render.contains("content of tab 0"))

    end tests

end LazyTabsViewTest
