// PURPOSE: Server logic for the lazy-load example — streams the loaded fragment once, on init.
// PURPOSE: A single patch-elements event whose fragment replaces the placeholder by its id.
package works.iterative.scalatags.datastar.scenarios

import sttp.tapir.ztapir.*
import sttp.capabilities.zio.ZioStreams
import org.http4s.HttpRoutes
import zio.*
import works.iterative.scalatags.datastar.tapir.sse.*

/** The lazy-load example's handler: answers the on-init action with one `patch-elements` event
  * carrying the loaded `LazyLoadView.graph` fragment, which replaces the placeholder by its id.
  */
object LazyLoadServer:

    // snippet: lazy-load-server
    private val graphLogic: ZServerEndpoint[Any, ZioStreams] =
        LazyLoadEndpoints.graph.zServerLogic: _ =>
            ZIO.succeed(datastarStream(ServerSentEvents.patchElements(LazyLoadView.graph)))
    // snippet-end

    val serverEndpoints: List[ZServerEndpoint[Any, ZioStreams]] =
        List(graphLogic)

    val routes: HttpRoutes[[A] =>> RIO[Any, A]] =
        HttpServer.routes(serverEndpoints)

    /** Binds a Blaze server serving just lazy load to `host`/`port`, scoped. */
    def serve(port: Int, host: String = "localhost"): RIO[Scope, org.http4s.server.Server] =
        HttpServer.serve(serverEndpoints, port, host)

end LazyLoadServer
