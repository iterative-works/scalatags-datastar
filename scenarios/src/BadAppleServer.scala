// PURPOSE: Server logic for the bad-apple example — streams the ASCII frames on a paced loop.
// PURPOSE: Uses the over-time datastarStream feed; the loop is bounded so it ends on its own.
package works.iterative.scalatags.datastar.scenarios

import org.http4s.HttpRoutes
import sttp.capabilities.zio.ZioStreams
import sttp.tapir.ztapir.*
import works.iterative.scalatags.datastar.tapir.sse.*
import zio.*
import zio.stream.ZStream

/** The bad-apple example's handler: streams the frame loop a few times over, one paced
  * `patch-elements` event per frame, then stops.
  */
object BadAppleServer:

    /** Time between frames. */
    private val frameDelay: Duration = 90.millis

    /** How many times the loop repeats before the feed ends. */
    private val loops: Int = 3

    // snippet: bad-apple-server
    private val playLogic: ZServerEndpoint[Any, ZioStreams] =
        BadAppleEndpoints.play.zServerLogic: _ =>
            val frames = Seq.fill(loops)(BadAppleFrames.all).flatten
            val events: ZStream[Any, Throwable, String] =
                ZStream
                    .fromIterable(frames)
                    .map(art => ServerSentEvents.patchElements(BadAppleView.frame(art)))
                    .mapZIO(event => ZIO.sleep(frameDelay).as(event))
            ZIO.succeed(datastarStream(events))
    // snippet-end

    val serverEndpoints: List[ZServerEndpoint[Any, ZioStreams]] =
        List(playLogic)

    val routes: HttpRoutes[[A] =>> RIO[Any, A]] =
        HttpServer.routes(serverEndpoints)

    /** Binds a Blaze server serving just bad-apple to `host`/`port`, scoped. */
    def serve(port: Int, host: String = "localhost"): RIO[Scope, org.http4s.server.Server] =
        HttpServer.serve(serverEndpoints, port, host)

end BadAppleServer
