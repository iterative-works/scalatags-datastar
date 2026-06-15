// PURPOSE: Server logic for the progress-bar example — streams bar states from 0 to 100 over time.
// PURPOSE: Uses the over-time datastarStream feed: one paced patch-elements event per step.
package works.iterative.scalatags.datastar.scenarios

import org.http4s.HttpRoutes
import sttp.capabilities.zio.ZioStreams
import sttp.tapir.ztapir.*
import works.iterative.scalatags.datastar.tapir.sse.*
import zio.*
import zio.stream.ZStream

/** The progress-bar example's handler: streams a `patch-elements` event for each percentage step,
  * paced by a short delay, then one final event swapping in the "try again" button.
  */
object ProgressBarServer:

    /** The percentages streamed, 0..100 in tens. */
    val steps: Seq[Int] = 0 to 100 by 10

    /** The delay between streamed updates. */
    private val tick: Duration = 200.millis

    // snippet: progress-bar-server
    private val updatesLogic: ZServerEndpoint[Any, ZioStreams] =
        ProgressBarEndpoints.updates.zServerLogic: _ =>
            val events: ZStream[Any, Throwable, String] =
                ZStream
                    .fromIterable(steps)
                    .map(percent => ServerSentEvents.patchElements(ProgressBarView.bar(percent)))
                    .concat(ZStream.succeed(
                        ServerSentEvents.patchElements(ProgressBarView.completed)
                    ))
                    .mapZIO(event => ZIO.sleep(tick).as(event))
            ZIO.succeed(datastarStream(events))
    // snippet-end

    val serverEndpoints: List[ZServerEndpoint[Any, ZioStreams]] =
        List(updatesLogic)

    val routes: HttpRoutes[[A] =>> RIO[Any, A]] =
        HttpServer.routes(serverEndpoints)

    /** Binds a Blaze server serving just the progress bar to `host`/`port`, scoped. */
    def serve(port: Int, host: String = "localhost"): RIO[Scope, org.http4s.server.Server] =
        HttpServer.serve(serverEndpoints, port, host)

end ProgressBarServer
