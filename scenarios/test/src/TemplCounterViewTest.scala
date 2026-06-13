// PURPOSE: Unit test for the templ-counter widget — the lazy-loaded shared count and its actions.
// PURPOSE: The stateful round-trip is covered by the routes test against the shared Cell.
package works.iterative.scalatags.datastar.scenarios

import utest.*

object TemplCounterViewTest extends TestSuite:

    val tests = Tests:

        test("the count renders under its id"):
            assert(TemplCounterView.count(7).render.contains("""id="templ-count""""))
            assert(TemplCounterView.count(7).render.contains(">7<"))

        test("the widget lazy-loads the shared count and wires the increment action"):
            val html = TemplCounterView.demo.render
            assert(html.contains("""data-init="@get('/templ-counter/count')""""))
            assert(html.contains("""data-on:click="@post('/templ-counter/increment')""""))

    end tests

end TemplCounterViewTest
