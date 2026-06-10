// PURPOSE: Derives a Datastar signal store's initial `data-signals` object literal from a case class.
// PURPOSE: The case class is the single source of truth for the store's shape and initial values.
package works.iterative.scalatags.datastar

import scala.deriving.Mirror
import scala.compiletime.{constValue, constValueTuple, erasedValue, summonInline}
import scala.compiletime.ops.any.==

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

    /** Marks a case class's companion as the home for its typed signal handles.
      *
      * Mixing `Signals.Handles[A]` into the companion lets it declare handles with [[signal]] —
      * `val count = signal("count")` — each checked against `A`'s fields at compile time and typed
      * as `Signal[FieldType]`. Call sites then read a stable member, e.g. `Counter.count`.
      */
    trait Handles[A]:
        /** The field `name` of `A`, as a typed signal handle. Does not compile if `A` has no such
          * field; the handle's value type is that field's type.
          */
        transparent inline def signal[Name <: String & Singleton](name: Name): Signal[?] =
            fieldSignal[A, Name](name)(using summonInline[Mirror.ProductOf[A]])
    end Handles

    /** A field of product type `A`, by name, as a typed signal handle (`Signal[FieldType]`),
      * verified against `A`'s fields at compile time. Backs [[Handles.signal]].
      */
    transparent inline def fieldSignal[A, Name <: String & Singleton](name: Name)(using
        m: Mirror.ProductOf[A]
    ): Signal[?] =
        lookup[Name, m.MirroredElemLabels, m.MirroredElemTypes](name)

    /** Walks the field labels and types in step to find `Name`'s type. Public only because an
      * inline method refers to it, so it must be accessible wherever that method expands.
      */
    transparent inline def lookup[
        Name <: String & Singleton,
        Labels <: Tuple,
        Types <: Tuple
    ](name: Name): Signal[?] =
        inline erasedValue[Labels] match
            case _: EmptyTuple =>
                compiletime.error("Signals: no field named \"" + constValue[Name] + "\"")
            case _: (label *: labelsTail) =>
                inline erasedValue[Types] match
                    case _: (tpe *: typesTail) =>
                        inline if constValue[label == Name] then Signal[tpe](name)
                        else lookup[Name, labelsTail, typesTail](name)
                    case _: EmptyTuple =>
                        compiletime.error("Signals: field/type arity mismatch")
end Signals
