// PURPOSE: The counter example's signal store — one case class for the whole round trip.
// PURPOSE: The same type seeds the initial `data-signals` value and decodes the store server-side.
package works.iterative.scalatags.datastar.scenarios

import works.iterative.scalatags.datastar.Signals
import zio.json.{JsonDecoder, JsonEncoder}

/** The counter page's signal store.
  *
  * `derives Signals` makes this the single source of truth for the page's `data-signals` object;
  * `derives JsonEncoder, JsonDecoder` lets the same type cross the SSE wire in both directions —
  * decoded from the request via `readSignals`, re-encoded into a `patch-signals` event. The
  * companion mixes in [[Signals.Handles]] so templates reference fields as typed handles
  * (`Counter.count`).
  */
final case class Counter(count: Int = 0, step: Int = 1) derives Signals, JsonEncoder, JsonDecoder:
    /** The store after one click: `count` advances by `step`. */
    def incremented: Counter = copy(count = count + step)
end Counter

object Counter extends Signals.Handles[Counter]:
    val count = signal("count")
    val step = signal("step")
end Counter
