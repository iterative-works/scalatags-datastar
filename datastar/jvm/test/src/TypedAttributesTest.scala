// PURPOSE: Pins that Datastar attributes accept typed `Expr`/`Signal` values and render their string.
// PURPOSE: Confirms the expression DSL plugs into existing attributes without losing the string form.
package works.iterative.scalatags.datastar

import utest.*
import scalatags.Text.all.*
import Datastar.*

object TypedAttributesTest extends TestSuite:

    def render(m: Modifier): String = div(m).render

    val count = Signal[Int]("count")
    val busy = Signal[Boolean]("busy")

    val tests = Tests:

        test("a bare signal binds as a typed expression"):
            assert(render(dataText := count) == """<div data-text="$count"></div>""")

        test("data-show renders a boolean expression, HTML-escaped (the browser decodes it)"):
            // `>` and `&&` are escaped to `&gt;`/`&amp;&amp;` as required for a valid HTML
            // attribute value; the browser decodes them back to `$count > 5 && !$busy` before
            // Datastar reads the attribute, so the expression it evaluates is unchanged.
            val visible = (count > lit(5)) && !busy
            assert(
              render(dataShow := visible)
                  == """<div data-show="$count &gt; 5 &amp;&amp; !$busy"></div>"""
            )

        test("an arithmetic expression binds inline"):
            assert(render(dataText := count + lit(1)) == """<div data-text="$count + 1"></div>""")

        test("data-computed takes an expression, plain and keyed"):
            val a = Signal[Int]("a")
            val b = Signal[Int]("b")
            assert(render(dataComputed := a + b) == """<div data-computed="$a + $b"></div>""")
            assert(
              render(dataComputed("total") := a + b)
                  == """<div data-computed:total="$a + $b"></div>"""
            )

        test("data-effect takes an expression"):
            val a = Signal[Int]("a")
            assert(render(dataEffect := a + lit(1)) == """<div data-effect="$a + 1"></div>""")

        test("the string escape hatch still works alongside typed expressions"):
            assert(render(dataText := "$count") == """<div data-text="$count"></div>""")

        test("data-bind binds a signal handle by its bare name, not a $ reference"):
            // Datastar's data-bind takes the signal *name* (`data-bind="step"`), unlike every other
            // attribute that takes an expression — so a handle must render its name, not `$step`.
            assert(render(dataBind := Counter.step) == """<div data-bind="step"></div>""")
            assert(input(dataBind := Counter.step).render == """<input data-bind="step" />""")
