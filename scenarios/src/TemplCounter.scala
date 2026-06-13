// PURPOSE: The templ-counter example's domain — a single counter shared by every visitor.
// PURPOSE: Server state in a Cell[Int]; per-user counters need a session and are out of DSL scope.
package works.iterative.scalatags.datastar.scenarios

// snippet: templ-counter-store
/** The templ-counter example's store: one global click count every visitor shares. */
object GlobalCounter:
    val cell: Cell[Int] = Cell(0)
// snippet-end
