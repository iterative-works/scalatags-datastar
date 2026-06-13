// PURPOSE: An in-memory, Ref-backed collection of domain rows — the store the row examples share.
// PURPOSE: Reads and updates are atomic effects; the seed is created once when the repo is built.
package works.iterative.scalatags.datastar.scenarios

import zio.*

/** A mutable in-memory collection of rows of type `T` keyed by `Id`, backed by a ZIO `Ref`.
  *
  * The row-mutating examples (delete, edit, bulk-update, todos) all reduce to "render the rows →
  * change one or all → render them again", so they share this small store rather than each
  * hand-roll a `Ref`. Every operation is an atomic `UIO`, so handlers stay on the `Any`
  * environment. The seed collection is captured once when the repository is constructed; [[reset]]
  * restores it.
  *
  * It is a deliberately simple, process-local store for a dogfood app — not a persistence
  * abstraction. The `Ref` is created eagerly at construction so the rows exist before the first
  * request, keeping the handlers free of setup effects.
  */
final class Repository[Id, T](seed: Seq[T], identify: T => Id):

    private val store: Ref[Vector[T]] = Repository.unsafeRef(seed.toVector)

    /** Every row, in order. */
    def all: UIO[Vector[T]] = store.get

    /** The row with this id, if present. */
    def get(id: Id): UIO[Option[T]] = store.get.map(_.find(row => identify(row) == id))

    /** Appends a row. */
    def add(row: T): UIO[Unit] = store.update(_ :+ row)

    /** Removes the row with this id, if present. */
    def delete(id: Id): UIO[Unit] = store.update(_.filterNot(row => identify(row) == id))

    /** Replaces the row with this id by `f` applied to it, leaving the rest untouched. */
    def update(id: Id)(f: T => T): UIO[Unit] =
        store.update(_.map(row => if identify(row) == id then f(row) else row))

    /** Applies `f` to every row. */
    def updateAll(f: T => T): UIO[Unit] = store.update(_.map(f))

    /** Restores the seed collection. */
    def reset(): UIO[Unit] = store.set(seed.toVector)

end Repository

object Repository:

    /** Builds a repository seeded with `seed`, keyed by `identify`. */
    def apply[Id, T](seed: Seq[T], identify: T => Id): Repository[Id, T] =
        new Repository(seed, identify)

    private val runtime = Runtime.default

    /** Allocates a `Ref` eagerly — the store must exist before the first request handles it. */
    private[scenarios] def unsafeRef[A](initial: A): Ref[A] =
        Unsafe.unsafe(implicit u => runtime.unsafe.run(Ref.make(initial)).getOrThrowFiberFailure())

end Repository

/** A single mutable value behind a ZIO `Ref` — the single-record sibling of [[Repository]], for the
  * examples that edit one record (a profile, a circle, a dashboard) rather than a collection. The
  * value is created eagerly at construction; [[reset]] restores the initial one.
  */
final class Cell[A](initial: A):

    private val ref: Ref[A] = Repository.unsafeRef(initial)

    /** The current value. */
    def get: UIO[A] = ref.get

    /** Replaces the value. */
    def set(value: A): UIO[Unit] = ref.set(value)

    /** Transforms the value. */
    def update(f: A => A): UIO[Unit] = ref.update(f)

    /** Restores the initial value. */
    def reset(): UIO[Unit] = ref.set(initial)

end Cell
