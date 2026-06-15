// PURPOSE: Proves the signal store round-trips: one case class seeds the initial value and decodes
// PURPOSE: the value Datastar sends back — the symmetry the typed Signals model exists to provide.
package works.iterative.scalatags.datastar.sse

import utest.*
import works.iterative.scalatags.datastar.Signals
import zio.json.*

/** A signal store that is the single source of truth in both directions: `derives Signals` seeds
  * the initial `data-signals` value (core), and `derives JsonDecoder` decodes the store Datastar
  * round-trips to the server.
  */
final case class Counter(count: Int = 0, step: Int = 1) derives Signals, JsonEncoder, JsonDecoder

object SymmetryTest extends TestSuite:

    val tests = Tests {

        test("one case class seeds the initial store and decodes the round trip") {
            // The core encoder renders the initial value for the data-signals attribute.
            assert(Signals.encode(Counter()) == "{count: 0, step: 1}")
            // Datastar sends the store back as standard JSON; readSignals decodes it into the model.
            assert(readSignals[Counter]("""{"count":5,"step":2}""") == Right(Counter(5, 2)))
        }

        test("patchSignals streams the model back as a patch-signals event") {
            val sse = ServerSentEvents.patchSignals(Counter(7, 3))
            assert(
                sse == "event: datastar-patch-signals\ndata: signals {\"count\":7,\"step\":3}\n\n"
            )
        }
    }

end SymmetryTest
