// PURPOSE: The edit-row example's domain — a person row type and the shared mutable repository.
// PURPOSE: One Repository[Long, Person] holds the table the example edits in place over SSE.
package works.iterative.scalatags.datastar.scenarios

// snippet: edit-row-store
/** A person in the edit-row table. */
final case class Person(id: Long, name: String, email: String)

/** The edit-row example's store: a repository of people. Saving an edited row mutates this shared
  * store, so the change survives until the process restarts (or a reset).
  */
object People:

    val seed: Seq[Person] = Seq(
        Person(1, "Joe Smith", "joe@example.com"),
        Person(2, "Angie MacDowell", "angie@example.com"),
        Person(3, "Fuqua Tarkenton", "fuqua@example.com")
    )

    val repo: Repository[Long, Person] = Repository(seed, _.id)

end People
// snippet-end
