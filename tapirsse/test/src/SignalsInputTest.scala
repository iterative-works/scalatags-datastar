// PURPOSE: Unit test for the Datastar signal-store Tapir inputs — decoding happens in the codec.
// PURPOSE: A payload that fits the store decodes to its value; one that does not is a decode error.
package works.iterative.scalatags.datastar.tapir.sse

import sttp.tapir.Codec
import sttp.tapir.DecodeResult
import sttp.tapir.EndpointIO
import sttp.tapir.EndpointInput
import utest.*
import zio.json.JsonDecoder
import zio.json.JsonEncoder

/** A throwaway signal store: enough shape to exercise decoding — a typed field that rejects a
  * mismatched JSON value.
  */
final case class Probe(n: Int = 0) derives JsonEncoder, JsonDecoder

object SignalsInputTest extends TestSuite:

    // Reach the codec inside each input and decode a raw payload directly — the same decoding a
    // server runs on a request, without booting one. The casts are safe by construction: `body` is
    // always a String JSON body and `query` the single `datastar` parameter.
    private val bodyCodec: Codec[String, Probe, ?] =
        SignalsInput.body[Probe].asInstanceOf[EndpointIO.Body[String, Probe]].codec
    private val queryCodec: Codec[List[String], Probe, ?] =
        SignalsInput.query[Probe].asInstanceOf[EndpointInput.Query[Probe]].codec

    private def isError(result: DecodeResult[?]): Boolean = result match
        case _: DecodeResult.Error => true
        case _                     => false

    val tests = Tests:

        test("body decodes a JSON payload that fits the store into its value"):
            assert(bodyCodec.decode("""{"n":5}""") == DecodeResult.Value(Probe(5)))

        test("body decoding a payload that does not fit the store is a decode error"):
            assert(isError(bodyCodec.decode("""{"n":"nope"}""")))

        test("query decodes the datastar parameter that fits the store into its value"):
            assert(queryCodec.decode(List("""{"n":7}""")) == DecodeResult.Value(Probe(7)))

        test("query decoding a parameter that does not fit the store is a decode error"):
            assert(isError(queryCodec.decode(List("not json"))))

    end tests

end SignalsInputTest
