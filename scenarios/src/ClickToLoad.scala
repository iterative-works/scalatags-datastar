// PURPOSE: The click-to-load example's signal store — the offset of the next page to reveal.
// PURPOSE: The offset rides the store, so the server stays stateless: each click carries it along.
package works.iterative.scalatags.datastar.scenarios

import works.iterative.scalatags.datastar.Signals
import zio.json.JsonDecoder
import zio.json.JsonEncoder

/** The click-to-load page's signal store: how many rows are already shown, i.e. the offset of the
  * next page. Seeded to one page, since the initial render already shows the first page.
  */
// snippet: click-to-load-store
final case class ClickToLoad(offset: Int = Agents.pageSize)
    derives Signals, JsonEncoder, JsonDecoder
object ClickToLoad extends Signals.Handles[ClickToLoad]:
    val offset = signal("offset")
// snippet-end
