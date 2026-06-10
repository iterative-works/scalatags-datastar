// PURPOSE: Pins the rendered JS-expression strings produced by the typed Datastar Expr DSL.
// PURPOSE: Covers literals, signal refs, every operator, and precedence/associativity parenthesising.
package works.iterative.scalatags.datastar

import utest.*
import Expr.*

object ExprTest extends TestSuite:

    val tests = Tests:

        test("a signal renders with a dollar prefix, keeping any nested path"):
            assert(Signal[Int]("count").render == "$count")
            assert(Signal[Int]("form.baz").render == "$form.baz")

        test("literals render JS-style: numbers bare, strings single-quoted, booleans bare"):
            assert(lit(5).render == "5")
            assert(lit(5.0).render == "5.0")
            assert(lit(true).render == "true")
            assert(lit("hello").render == "'hello'")
            assert(lit("it's").render == """'it\'s'""")

        test("comparison operators render with surrounding spaces"):
            val count = Signal[Int]("count")
            assert((count > lit(5)).render == "$count > 5")
            assert((count <= lit(0)).render == "$count <= 0")

        test("equality uses JS === and !=="):
            val name = Signal[String]("name")
            assert((name === lit("a")).render == "$name === 'a'")
            assert((name !== lit("a")).render == "$name !== 'a'")

        test("boolean operators and negation"):
            val busy = Signal[Boolean]("busy")
            val count = Signal[Int]("count")
            assert((!busy).render == "!$busy")
            assert(((count > lit(5)) && !busy).render == "$count > 5 && !$busy")
            assert(((count > lit(5)) || busy).render == "$count > 5 || $busy")
            assert(((count > lit(5)) && (count < lit(10))).render == "$count > 5 && $count < 10")

        test("negation parenthesises a compound operand"):
            val a = Signal[Boolean]("a")
            val b = Signal[Boolean]("b")
            assert((!(a && b)).render == "!($a && $b)")

        test("arithmetic precedence omits unnecessary parentheses"):
            assert((lit(1) + lit(2) * lit(3)).render == "1 + 2 * 3")

        test("arithmetic precedence adds parentheses where needed"):
            assert(((lit(1) + lit(2)) * lit(3)).render == "(1 + 2) * 3")

        test("left-associativity keeps a left chain flat"):
            assert((lit(1) - lit(2) - lit(3)).render == "1 - 2 - 3")

        test("a right-nested subtraction is parenthesised"):
            assert((lit(1) - (lit(2) - lit(3))).render == "1 - (2 - 3)")

        test("string concatenation with +"):
            val msg = Signal[String]("msg")
            assert((msg + lit(" items")).render == "$msg + ' items'")
