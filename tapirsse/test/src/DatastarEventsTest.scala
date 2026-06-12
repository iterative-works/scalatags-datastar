// PURPOSE: Unit test for the Datastar SSE response stream — rendered events become the body bytes.
// PURPOSE: Concatenated event strings cross the wire as one UTF-8 byte stream, in order.
package works.iterative.scalatags.datastar.tapir.sse

import utest.*
import zio.*
import zio.stream.Stream
import java.nio.charset.StandardCharsets.UTF_8

object DatastarEventsTest extends TestSuite:

    private val runtime = Runtime.default

    private def bytesOf(stream: Stream[Throwable, Byte]): Array[Byte] =
        Unsafe.unsafe(implicit u =>
            runtime.unsafe.run(stream.runCollect).getOrThrowFiberFailure().toArray
        )

    val tests = Tests:

        test("a single event becomes its UTF-8 bytes"):
            val event = "event: datastar-patch-signals\ndata: signals {\"n\":5}\n\n"
            assert(bytesOf(datastarStream(event)).sameElements(event.getBytes(UTF_8)))

        test("several events are concatenated in order"):
            val a = "event: a\n\n"
            val b = "event: b\n\n"
            assert(bytesOf(datastarStream(a, b)).sameElements((a + b).getBytes(UTF_8)))

    end tests

end DatastarEventsTest
