// PURPOSE: A typed Datastar expression that renders to the framework's JS expression string.
// PURPOSE: Operators build a tree rendered with JS precedence, so output needs no manual parentheses.
package works.iterative.scalatags.datastar

/** A typed Datastar expression.
  *
  * Datastar drives reactivity from expressions written as JavaScript strings, e.g.
  * `data-show="$count > 5 && !$busy"`. An `Expr[A]` is the typed counterpart: the phantom type `A`
  * tracks the value the expression evaluates to, so an attribute can demand the right shape (e.g.
  * `data-show` wants an `Expr[Boolean]`) and the compiler rejects nonsense.
  *
  * Operators (in [[Expr$]]) build a small tree; [[render]] prints it using JavaScript operator
  * precedence and associativity, inserting only the parentheses needed to preserve meaning. So `(a
  * + b) * c` keeps its parentheses while `a + b * c` does not.
  */
trait Expr[+A]:

    /** This node's JavaScript operator precedence; atoms (signals, literals) bind tightest. */
    private[datastar] def prec: Int

    /** This node's string form *without* any enclosing parentheses. */
    private[datastar] def renderRaw: String

    /** Renders this node, wrapping it in parentheses when it binds looser than its context needs.
      */
    private[datastar] final def renderAt(minPrec: Int): String =
        if prec < minPrec then s"($renderRaw)" else renderRaw

    /** The Datastar expression string, e.g. `$count > 5 && !$busy`. */
    final def render: String = renderAt(0)

end Expr

/** A literal value embedded in an expression, rendered by its [[ExprLiteral]]. */
private final class Lit[A](value: A, literal: ExprLiteral[A]) extends Expr[A]:
    private[datastar] def prec = Expr.Atom
    private[datastar] def renderRaw = literal.render(value)

/** A binary operation `left op right`. Left-associative: the right operand is parenthesised one
  * precedence level sooner than the left, so `a - b - c` stays flat but `a - (b - c)` does not.
  */
private final class Binary[A](
    op: String,
    private[datastar] val prec: Int,
    left: Expr[?],
    right: Expr[?]
) extends Expr[A]:
    private[datastar] def renderRaw = s"${left.renderAt(prec)} $op ${right.renderAt(prec + 1)}"
end Binary

/** A prefix operation `op operand`, e.g. `!$busy`. */
private final class Unary[A](op: String, private[datastar] val prec: Int, operand: Expr[?])
    extends Expr[A]:
    private[datastar] def renderRaw = s"$op${operand.renderAt(prec)}"

/** Expression constructors and operators.
  *
  * Precedence levels mirror JavaScript's: looser operators get lower numbers, so `||` < `&&` <
  * equality < relational < additive < multiplicative < unary < atom. The absolute values are
  * arbitrary; only their ordering matters to the parenthesising in [[Expr.renderAt]].
  */
object Expr:

    /** Atoms (signals, literals) never need parentheses, so they bind tighter than any operator. */
    private[datastar] val Atom = 100

    /** Lifts a Scala value into an expression, e.g. `lit(5)`, `lit("hello")`, `lit(true)`. */
    def lit[A](value: A)(using literal: ExprLiteral[A]): Expr[A] = new Lit(value, literal)

    extension (self: Expr[Boolean])
        def &&(that: Expr[Boolean]): Expr[Boolean] = new Binary("&&", 4, self, that)
        def ||(that: Expr[Boolean]): Expr[Boolean] = new Binary("||", 3, self, that)
        def unary_! : Expr[Boolean] = new Unary("!", 14, self)
    end extension

    extension [A](self: Expr[A])
        def ===(that: Expr[A]): Expr[Boolean] = new Binary("===", 8, self, that)
        def !==(that: Expr[A]): Expr[Boolean] = new Binary("!==", 8, self, that)

    extension [A](self: Expr[A])(using Numeric[A])
        def >(that: Expr[A]): Expr[Boolean] = new Binary(">", 9, self, that)
        def <(that: Expr[A]): Expr[Boolean] = new Binary("<", 9, self, that)
        def >=(that: Expr[A]): Expr[Boolean] = new Binary(">=", 9, self, that)
        def <=(that: Expr[A]): Expr[Boolean] = new Binary("<=", 9, self, that)
        def +(that: Expr[A]): Expr[A] = new Binary("+", 11, self, that)
        def -(that: Expr[A]): Expr[A] = new Binary("-", 11, self, that)
        def *(that: Expr[A]): Expr[A] = new Binary("*", 12, self, that)
        def /(that: Expr[A]): Expr[A] = new Binary("/", 12, self, that)
        def %(that: Expr[A]): Expr[A] = new Binary("%", 12, self, that)
    end extension

    extension (self: Expr[String])
        def +(that: Expr[String]): Expr[String] = new Binary("+", 11, self, that)

end Expr

/** Renders a Scala value as a Datastar/JS literal: numbers bare, booleans bare, strings
  * single-quoted with `\` and `'` escaped (the literal sits inside a single-quoted JS string).
  */
trait ExprLiteral[A]:
    def render(value: A): String

object ExprLiteral:
    given ExprLiteral[Int] = _.toString
    given ExprLiteral[Long] = _.toString
    given ExprLiteral[Double] = _.toString
    given ExprLiteral[Boolean] = _.toString
    given ExprLiteral[String] = value =>
        "'" + value.replace("\\", "\\\\").replace("'", "\\'") + "'"
end ExprLiteral
