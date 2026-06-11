// PURPOSE: Unit tests for the counter example's pure pieces — the domain step and the widget render.
// PURPOSE: Pins that the fragment emits the typed Datastar attributes the round trip depends on.
package works.iterative.scalatags.datastar.scenarios

import utest.*

object CounterViewTest extends TestSuite:

    val tests = Tests:

        test("incremented advances count by step"):
            assert(Counter(count = 5, step = 1).incremented == Counter(6, 1))
            assert(Counter(count = 0, step = 3).incremented == Counter(3, 3))

        test("the widget seeds the signal store from the case class"):
            assert(CounterView.demo.render.contains("""data-signals="{count: 0, step: 1}""""))

        test("the widget binds the live value to the count signal"):
            assert(CounterView.demo.render.contains("""data-text="$count""""))

        test("the widget wires the button to the reverse-routed increment action"):
            assert(CounterView.demo.render.contains("""data-on:click="@post('/increment')""""))

    end tests

end CounterViewTest
