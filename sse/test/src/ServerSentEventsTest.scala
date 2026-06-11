// PURPOSE: Focused tests for the SSE codec paths the golden conformance suite does not exercise.
// PURPOSE: Covers the typed patchSignals/readSignals entry points and the namespace option.
package works.iterative.scalatags.datastar.sse

import utest.*
import zio.json.*
import scalatags.Text.all.*
import scala.concurrent.duration.DurationInt

/** A small signal model used to exercise the typed JSON entry points. */
final case class Box(a: Int, b: String) derives JsonEncoder, JsonDecoder

object ServerSentEventsTest extends TestSuite:

    val tests = Tests {

        test("patchElements renders a bare event for a plain fragment") {
            val sse = ServerSentEvents.patchElements(div("hi"))
            assert(sse == "event: datastar-patch-elements\ndata: elements <div>hi</div>\n\n")
        }

        test("patchElements emits a namespace data line when set") {
            val sse = ServerSentEvents.patchElements(raw("<g/>"), namespace = Some("svg"))
            assert(sse == "event: datastar-patch-elements\ndata: namespace svg\ndata: elements <g/>\n\n")
        }

        test("a non-default retry duration emits a retry line in milliseconds") {
            val sse = ServerSentEvents.patchElements(div(), retryDuration = 2.seconds)
            assert(sse == "event: datastar-patch-elements\nretry: 2000\ndata: elements <div></div>\n\n")
        }

        test("patchSignals serializes a typed model to compact JSON") {
            val sse = ServerSentEvents.patchSignals(Box(1, "x"))
            assert(sse == "event: datastar-patch-signals\ndata: signals {\"a\":1,\"b\":\"x\"}\n\n")
        }

        test("readSignals decodes a round-tripped store into the typed model") {
            assert(readSignals[Box]("""{"a":1,"b":"x"}""") == Right(Box(1, "x")))
        }

        test("readSignals reports a decode error rather than throwing") {
            assert(readSignals[Box]("not json").isLeft)
        }
    }

end ServerSentEventsTest
