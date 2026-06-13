// PURPOSE: The infinite-scroll example's signal store — the offset of the next page to reveal.
// PURPOSE: As with click-to-load, the offset rides the store so the server stays stateless.
package works.iterative.scalatags.datastar.scenarios

import works.iterative.scalatags.datastar.Signals
import zio.json.{JsonDecoder, JsonEncoder}

/** The infinite-scroll page's signal store: the offset of the next page, seeded to one page since
  * the initial render already shows the first.
  */
// snippet: infinite-scroll-store
final case class InfiniteScroll(offset: Int = Agents.pageSize)
    derives Signals, JsonEncoder, JsonDecoder
object InfiniteScroll extends Signals.Handles[InfiniteScroll]:
    val offset = signal("offset")
// snippet-end
