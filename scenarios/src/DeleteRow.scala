// PURPOSE: The delete-row example's domain — a member row type and the shared mutable repository.
// PURPOSE: One Repository[Long, Member] holds the table the example deletes rows from over SSE.
package works.iterative.scalatags.datastar.scenarios

// snippet: delete-row-store
/** A member in the delete-row table. */
final case class Member(id: Long, name: String, email: String, active: Boolean)

/** The delete-row example's store: a repository of members seeded with a small roster. Deleting a
  * row mutates this shared store, so the change survives until the process restarts (or a reset).
  */
object Members:

    val seed: Seq[Member] = Seq(
        Member(1, "Joe Smith", "joe@example.com", active = true),
        Member(2, "Angie MacDowell", "angie@example.com", active = true),
        Member(3, "Fuqua Tarkenton", "fuqua@example.com", active = false),
        Member(4, "Kim Yee", "kim@example.com", active = true),
        Member(5, "Sid Carter", "sid@example.com", active = false)
    )

    val repo: Repository[Long, Member] = Repository(seed, _.id)

end Members
// snippet-end
