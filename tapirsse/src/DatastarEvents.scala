// PURPOSE: Tapir output and response stream for the Datastar SSE events a handler streams back.
// PURPOSE: `.out(datastarEvents)` declares the text/event-stream; `datastarStream` carries its bytes.
package works.iterative.scalatags.datastar.tapir.sse

import sttp.capabilities.zio.ZioStreams
import sttp.tapir.CodecFormat
import sttp.tapir.StreamBodyIO
import sttp.tapir.streamTextBody
import zio.Chunk
import zio.stream.Stream
import zio.stream.ZStream

import java.nio.charset.StandardCharsets.UTF_8

/** The `text/event-stream` output a Datastar SSE endpoint declares, as a `ZioStreams` byte stream.
  *
  * A handler answers a Datastar action by streaming SSE events, so its endpoint outputs a raw byte
  * stream tagged `text/event-stream`. `.out(datastarEvents)` is that declaration;
  * [[datastarStream]] builds the matching response body from the strings the SSE codec renders.
  */
val datastarEvents: StreamBodyIO[Stream[Throwable, Byte], Stream[Throwable, Byte], ZioStreams] =
    streamTextBody(ZioStreams)(CodecFormat.TextEventStream())

/** The response body for a [[datastarEvents]] output: the rendered SSE `events` as a byte stream.
  *
  * `ServerSentEvents` renders each event to its exact wire string, and concatenating several gives
  * a multi-event response. This carries those strings as the UTF-8 byte stream the output expects,
  * so a handler returns `ZIO.succeed(datastarStream(event))` rather than plumbing
  * `ZStream`/`Chunk`/bytes by hand.
  */
def datastarStream(events: String*): Stream[Throwable, Byte] =
    ZStream.fromChunk(Chunk.fromArray(events.mkString.getBytes(UTF_8)))

/** The response body for a [[datastarEvents]] output from a stream of events produced *over time*:
  * each rendered SSE event string becomes its UTF-8 bytes as the stream emits it, preserving the
  * pacing. This is the feed form of [[datastarStream]] — a handler that streams progress updates, a
  * clock, or any open-ended sequence returns `ZIO.succeed(datastarStream(events))` where `events`
  * emits on its own schedule, rather than collecting every event up front as the varargs form does.
  */
def datastarStream(events: ZStream[Any, Throwable, String]): Stream[Throwable, Byte] =
    events.mapConcatChunk(event => Chunk.fromArray(event.getBytes(UTF_8)))

/** The SSE codec a server handler builds its events with, re-exported so the server side is reached
  * through this one import: [[works.iterative.scalatags.datastar.sse.ServerSentEvents]] renders the
  * events, [[works.iterative.scalatags.datastar.sse.ElementPatchMode]] selects a patch strategy,
  * and [[works.iterative.scalatags.datastar.sse.readSignals]] is the raw store decoder
  * [[SignalsInput]] wraps.
  */
export works.iterative.scalatags.datastar.sse.{ServerSentEvents, ElementPatchMode, readSignals}
