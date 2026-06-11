// PURPOSE: Unit tests for the counter example's pure pieces — the domain step and the page render.
// PURPOSE: Pins that the template emits the typed Datastar attributes the round trip depends on.
package works.iterative.scalatags.datastar.scenarios

import utest.*

object CounterViewTest extends TestSuite:

    val tests = Tests:

        test("incremented advances count by step"):
            assert(Counter(count = 5, step = 1).incremented == Counter(6, 1))
            assert(Counter(count = 0, step = 3).incremented == Counter(3, 3))

        test("page seeds the signal store from the case class"):
            assert(CounterView.page.contains("""data-signals="{count: 0, step: 1}""""))

        test("page binds the live value to the count signal"):
            assert(CounterView.page.contains("""data-text="$count""""))

        test("page wires the button to the reverse-routed increment action"):
            assert(CounterView.page.contains("""data-on:click="@post('/increment')""""))

        test("page loads the Datastar client matching the codec's wire format"):
            assert(CounterView.page.contains("starfederation/datastar@v1.0.2"))
            // v1.0.2 renamed merge-* to patch-*; the demo must not regress to the old client.
            assert(!CounterView.page.contains("merge-signals"))

        test("page is a complete HTML document"):
            assert(CounterView.page.startsWith("<!DOCTYPE html>"))

    end tests

end CounterViewTest
