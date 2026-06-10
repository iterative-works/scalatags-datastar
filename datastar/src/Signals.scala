// PURPOSE: Derives a Datastar signal store's initial `data-signals` object literal from a case class.
// PURPOSE: The case class is the single source of truth for the store's shape and initial values.
package works.iterative.scalatags.datastar

import scala.deriving.Mirror
import scala.compiletime.{constValueTuple, erasedValue, summonInline}

/** The initial-value model for a Datastar signal store.
  *
  * A `Signals[A]` knows how to render an instance of `A` as the object literal Datastar's
  * `data-signals` attribute expects, e.g. `{count: 0, step: 1}`. Deriving it from a case class
  * (`case class Counter(count: Int = 0, step: Int = 1) derives Signals`) makes that case class the
  * single source of truth for the store: the same type that seeds the initial values is the one the
  * server will decode the round-tripped store back into.
  *
  * It extends [[ExprLiteral]] so a nested model renders as a nested object literal, e.g. a field of
  * type `Inner` contributes `inner: {baz: 2}`.
  */
trait Signals[A] extends ExprLiteral[A]

object Signals:

    /** Renders a model instance as Datastar's initial `data-signals` object literal. */
    def encode[A](value: A)(using s: Signals[A]): String = s.render(value)

    /** The case class's field names, in declaration order. */
    private inline def labelsOf[T <: Tuple]: List[String] =
        constValueTuple[T].productIterator.map(_.toString).toList

    /** One literal renderer per field type, in declaration order. A nested model resolves to its
      * own derived `Signals` (which is an `ExprLiteral`), so object literals nest.
      */
    private inline def literalsOf[T <: Tuple]: List[ExprLiteral[Any]] =
        inline erasedValue[T] match
            case _: EmptyTuple => Nil
            case _: (head *: tail) =>
                summonInline[ExprLiteral[head]].asInstanceOf[ExprLiteral[Any]] :: literalsOf[tail]

    inline given derived[A](using m: Mirror.ProductOf[A]): Signals[A] =
        val labels = labelsOf[m.MirroredElemLabels]
        val literals = literalsOf[m.MirroredElemTypes]
        new Signals[A]:
            def render(value: A): String =
                val values = value.asInstanceOf[Product].productIterator.toList
                labels
                    .lazyZip(values)
                    .lazyZip(literals)
                    .map((label, fieldValue, literal) => s"$label: ${literal.render(fieldValue)}")
                    .mkString("{", ", ", "}")
            end render
        end new
    end derived
end Signals
