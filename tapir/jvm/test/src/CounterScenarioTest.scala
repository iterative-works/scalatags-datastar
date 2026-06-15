// PURPOSE: End-to-end proof that the typed signal model, Expr DSL, handles and a Tapir action compose.
// PURPOSE: Renders the canonical counter view and pins its exact HTML across every Phase 2/3 feature.
package works.iterative.scalatags.datastar.tapir

import scalatags.Text.all.*
import sttp.tapir.*
import utest.*
import works.iterative.scalatags.datastar.Datastar.*

/** The signal store for the counter, the single source of truth for its shape and initial values.
  */
final case class Counter(count: Int = 0, step: Int = 1) derives Signals
object Counter extends Signals.Handles[Counter]:
    val count = signal("count")
    val step = signal("step")

object CounterScenarioTest extends TestSuite:

    val tests = Tests:

        test("the counter view composes signals, expressions, handles and a typed action"):
            val increment = endpoint.post.in("inc")
            val inc = increment.action

            val view = div(
                dataSignals := Signals.encode(Counter()),
                input(`type` := "number", dataBind := Counter.step),
                button(dataOn("click") := inc)("+"),
                span(dataShow := Counter.count > lit(0), dataText := Counter.count)
            )

            val expected =
                """<div data-signals="{count: 0, step: 1}">""" +
                    """<input type="number" data-bind="step" />""" +
                    """<button data-on:click="@post('/inc')">+</button>""" +
                    """<span data-show="$count &gt; 0" data-text="$count"></span>""" +
                    """</div>"""

            assert(view.render == expected)
end CounterScenarioTest
