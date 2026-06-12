// PURPOSE: Pins the derived initial `data-signals` object literal for a case-class signal model.
// PURPOSE: Covers scalar fields, string quoting, nested models, and binding a model to dataSignals.
package works.iterative.scalatags.datastar

import utest.*
import scalatags.Text.all.*
import Datastar.*

final case class Counter(count: Int = 0, step: Int = 1) derives Signals
object Counter extends Signals.Handles[Counter]:
    val count = signal("count")
    val step = signal("step")

final case class Inner(baz: Int = 2) derives Signals
final case class Outer(inner: Inner = Inner(), n: Int = 1) derives Signals
final case class Search(query: String = "", page: Int = 1) derives Signals

object SignalsTest extends TestSuite:

    val tests = Tests:

        test("derives the initial object literal from a case class"):
            assert(Signals.encode(Counter()) == "{count: 0, step: 1}")
            assert(Signals.encode(Counter(5, 2)) == "{count: 5, step: 2}")

        test("string fields are single-quoted"):
            assert(Signals.encode(Search()) == "{query: '', page: 1}")
            assert(Signals.encode(Search("milk", 3)) == "{query: 'milk', page: 3}")

        test("nested case classes nest the object literal"):
            assert(Signals.encode(Outer()) == "{inner: {baz: 2}, n: 1}")

        test("dataSignals binds a model's encoded initial literal"):
            assert(
              div(dataSignals := Signals.encode(Counter())).render
                  == """<div data-signals="{count: 0, step: 1}"></div>"""
            )

        test("dataSignals binds a typed model directly, no encode call"):
            assert(
              div(dataSignals := Counter()).render
                  == """<div data-signals="{count: 0, step: 1}"></div>"""
            )
            assert(
              div(dataSignals := Search("milk", 3)).render
                  == """<div data-signals="{query: 'milk', page: 3}"></div>"""
            )

        test("companion handles render as signal references"):
            assert(Counter.count.render == "$count")
            assert(Counter.step.render == "$step")

        test("a handle carries the field's type for the Expr DSL"):
            // `> lit(5)` needs `Numeric`, so this only compiles if `count` is a `Signal[Int]`.
            assert((Counter.count > lit(5)).render == "$count > 5")

        test("a handle composes into a typed attribute"):
            assert(div(dataText := Counter.count).render == """<div data-text="$count"></div>""")

        test("a name that is not a field of the model does not compile"):
            assert(!compiletime.testing.typeChecks("""Counter.signal("nope")"""))
            assert(compiletime.testing.typeChecks("""Counter.signal("count")"""))
