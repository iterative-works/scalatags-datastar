// PURPOSE: The fixed catalogue the pagination examples page through — a list of agents.
// PURPOSE: Shared by click-to-load and infinite-scroll; the offset rides each page's signal store.
package works.iterative.scalatags.datastar.scenarios

/** One row in the paginated catalogue. */
final case class Agent(id: Int, name: String, email: String)

/** A fixed list of agents the pagination examples reveal a page at a time. The offset (how many
  * rows are already shown) lives in the page's signal store, so the server is stateless: each
  * request carries the offset and returns the next page.
  */
object Agents:

    /** How many rows a page reveals. */
    val pageSize: Int = 10

    /** Every agent, in order. */
    val all: Seq[Agent] =
        (1 to 60).map(index => Agent(index, s"Agent $index", s"agent$index@example.com"))

    /** The page of rows starting at `offset` (clamped to the catalogue bounds). */
    def page(offset: Int): Seq[Agent] = all.slice(offset.max(0), offset.max(0) + pageSize)

    /** Whether any rows remain beyond `offset`. */
    def hasMore(offset: Int): Boolean = offset < all.size

end Agents
