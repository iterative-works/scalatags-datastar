// PURPOSE: Unit test for the title-update example's widget — the button reverse-routes its action.
// PURPOSE: The SSE handler's title patch is covered by the routes test (it reads the clock).
package works.iterative.scalatags.datastar.scenarios

import utest.*

object TitleUpdateViewTest extends TestSuite:

    val tests = Tests:

        test("the button fires the reverse-routed update action"):
            val html = TitleUpdateView.demo.render
            assert(html.contains("""data-on:click="@post('/title-update')""""))
            assert(html.contains("Update title"))

    end tests

end TitleUpdateViewTest
