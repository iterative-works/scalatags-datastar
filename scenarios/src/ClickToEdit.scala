// PURPOSE: The click-to-edit example's domain — a profile record and its single-cell store.
// PURPOSE: The same case class seeds the signals, decodes the @put body, and holds the server record.
package works.iterative.scalatags.datastar.scenarios

import works.iterative.scalatags.datastar.Signals
import zio.json.JsonDecoder
import zio.json.JsonEncoder

// snippet: click-to-edit-store
/** The editable profile — both the page's signal store and the server's record. */
final case class Profile(firstName: String = "", lastName: String = "", email: String = "")
    derives Signals, JsonEncoder, JsonDecoder
object Profile extends Signals.Handles[Profile]:
    val firstName = signal("firstName")
    val lastName = signal("lastName")
    val email = signal("email")

/** The click-to-edit example's store: a single mutable profile and the original for reset. */
object Profiles:
    val original: Profile = Profile("Joe", "Smith", "joe@example.com")
    val cell: Cell[Profile] = Cell(original)
// snippet-end
