// PURPOSE: A typed reference to a named Datastar signal, usable anywhere an Expr is.
// PURPOSE: Renders as `$name`, where the name already carries any nested path (e.g. `form.baz`).
package works.iterative.scalatags.datastar

/** A typed handle to a Datastar signal.
  *
  * A signal is a named reactive value in the browser store. As an [[Expr]] it renders to its
  * reference form `$name`; the `name` already includes any nested path, so a signal at `form.baz`
  * renders `$form.baz`. The phantom type `A` is the signal's value type, letting the expression DSL
  * and the typed attributes keep references honest.
  */
final class Signal[A](val name: String) extends Expr[A]:
    private[datastar] def prec = Expr.Atom
    private[datastar] def renderRaw = s"$$$name"
end Signal

object Signal:
    def apply[A](name: String): Signal[A] = new Signal[A](name)
