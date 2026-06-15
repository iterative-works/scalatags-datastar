// PURPOSE: The bulk-update example's domain — an account row and the selection signal store.
// PURPOSE: A Repository[Long, Account] holds the table; the selections array rides the @put body.
package works.iterative.scalatags.datastar.scenarios

import zio.json.JsonDecoder
import zio.json.JsonEncoder

// snippet: bulk-update-store
/** An account in the bulk-update table. */
final case class Account(id: Long, name: String, email: String, active: Boolean)

/** The bulk-update example's store: a repository of accounts, all inactive to start so an activate
  * is visible. A bulk action mutates the selected accounts in this shared store.
  */
object Accounts:

    val seed: Seq[Account] = Seq(
        Account(1, "Joe Smith", "joe@example.com", active = false),
        Account(2, "Angie MacDowell", "angie@example.com", active = false),
        Account(3, "Fuqua Tarkenton", "fuqua@example.com", active = false),
        Account(4, "Kim Yee", "kim@example.com", active = false),
        Account(5, "Sid Carter", "sid@example.com", active = false)
    )

    val repo: Repository[Long, Account] = Repository(seed, _.id)

end Accounts

/** The page's signal store: one boolean per row, in render order — the checkbox selections. It
  * rides the `@put` body (no `Signals` derivation, since an array is seeded with a raw
  * `data-signals` literal rather than the typed model); `readSignals` decodes it for the handler.
  */
final case class BulkSelection(selections: List[Boolean] = Nil) derives JsonEncoder, JsonDecoder
// snippet-end
