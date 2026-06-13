// PURPOSE: Server logic for the progressive-load example — streams the sections in random order.
// PURPOSE: Shuffles with the ZIO Random service, then paces each section patch over the feed.
package works.iterative.scalatags.datastar.scenarios

import sttp.tapir.ztapir.*
import sttp.capabilities.zio.ZioStreams
import org.http4s.HttpRoutes
import zio.*
import zio.stream.ZStream
import works.iterative.scalatags.datastar.tapir.sse.*

/** The progressive-load example's handler: shuffles the sections, then streams one paced
  * `patch-elements` event per section, each filling its placeholder by id.
  */
object ProgressiveLoadServer:

    /** The delay between streamed sections. */
    private val tick: Duration = 300.millis

    // snippet: progressive-load-server
    private val updatesLogic: ZServerEndpoint[Any, ZioStreams] =
        ProgressiveLoadEndpoints.updates.zServerLogic: _ =>
            Random.shuffle(Sections.all.toList).map: order =>
                val events: ZStream[Any, Throwable, String] =
                    ZStream
                        .fromIterable(order)
                        .map(section =>
                            ServerSentEvents.patchElements(ProgressiveLoadView.section(section))
                        )
                        .mapZIO(event => ZIO.sleep(tick).as(event))
                datastarStream(events)
    // snippet-end

    val serverEndpoints: List[ZServerEndpoint[Any, ZioStreams]] =
        List(updatesLogic)

    val routes: HttpRoutes[[A] =>> RIO[Any, A]] =
        HttpServer.routes(serverEndpoints)

    /** Binds a Blaze server serving just progressive load to `host`/`port`, scoped. */
    def serve(port: Int, host: String = "localhost"): RIO[Scope, org.http4s.server.Server] =
        HttpServer.serve(serverEndpoints, port, host)

end ProgressiveLoadServer
