// PURPOSE: The active-search example's domain — the query signal store and the contact catalogue.
// PURPOSE: One Search-shaped case class seeds data-signals and decodes the store the browser sends.
package works.iterative.scalatags.datastar.scenarios

import works.iterative.scalatags.datastar.Signals
import zio.json.JsonDecoder
import zio.json.JsonEncoder

/** The active-search page's signal store — the bound query text. As with the live-search example,
  * one case class is the whole round trip: `derives Signals` seeds `data-signals`, and the JSON
  * codecs let `readSignals` decode it from the `datastar` query parameter a `@get` action sends.
  */
// snippet: active-search-store
final case class ActiveSearch(search: String = "") derives Signals, JsonEncoder, JsonDecoder
object ActiveSearch extends Signals.Handles[ActiveSearch]:
    val search = signal("search")
// snippet-end

/** One contact in the catalogue the example filters. */
final case class Contact(firstName: String, lastName: String, email: String)

/** The fixed contact catalogue — also the result before any query is typed. */
object Contacts:

    val all: Seq[Contact] = Seq(
        Contact("Bryana", "Bernier", "bryana.bernier@example.com"),
        Contact("Jensen", "Kassulke", "jensen.kassulke@example.com"),
        Contact("Lavon", "Hermann", "lavon.hermann@example.com"),
        Contact("Terrell", "Boyle", "terrell.boyle@example.com"),
        Contact("Dorthy", "Hahn", "dorthy.hahn@example.com"),
        Contact("Ahmad", "Pfeffer", "ahmad.pfeffer@example.com"),
        Contact("Camron", "Schinner", "camron.schinner@example.com"),
        Contact("Maximus", "Mraz", "maximus.mraz@example.com"),
        Contact("Eldon", "Lockman", "eldon.lockman@example.com"),
        Contact("Justus", "Connelly", "justus.connelly@example.com"),
        Contact("Bridget", "Mertz", "bridget.mertz@example.com"),
        Contact("Madelynn", "Pacocha", "madelynn.pacocha@example.com")
    )

    /** The contacts whose name or email contains `query`, case-insensitively. A blank query matches
      * all.
      */
    def matching(query: String): Seq[Contact] =
        val needle = query.trim.toLowerCase
        if needle.isEmpty then all
        else
            all.filter: contact =>
                s"${contact.firstName} ${contact.lastName} ${contact.email}".toLowerCase
                    .contains(needle)
        end if
    end matching

end Contacts
