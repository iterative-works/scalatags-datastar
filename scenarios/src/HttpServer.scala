// PURPOSE: Shared transport for the scenarios — interprets Tapir endpoints onto http4s/Blaze.
// PURPOSE: One place builds the routes and the scoped server; each example just supplies endpoints.
package works.iterative.scalatags.datastar.scenarios

import org.http4s.HttpRoutes
import org.http4s.blaze.server.BlazeServerBuilder
import org.http4s.implicits.*
import sttp.capabilities.zio.ZioStreams
import sttp.tapir.server.http4s.ztapir.ZHttp4sServerInterpreter
import sttp.tapir.ztapir.*
import zio.*
import zio.interop.catz.*

/** Wires a set of Tapir server endpoints to the house http4s/Blaze stack under ZIO.
  *
  * The examples differ only in their endpoints; the Tapir→http4s interpretation and the Blaze
  * lifecycle are identical, so they live here once. Streaming (SSE) endpoints need the `ZioStreams`
  * capability, so that is the capability every example's endpoints are typed against.
  */
object HttpServer:

    /** Interprets `endpoints` into http4s routes. */
    def routes(endpoints: List[ZServerEndpoint[Any, ZioStreams]]): HttpRoutes[[A] =>> RIO[Any, A]] =
        ZHttp4sServerInterpreter[Any]().from(endpoints).toRoutes

    /** Binds a Blaze server serving `endpoints` to `host`/`port` (use `0` for an ephemeral port),
      * scoped so it shuts down when the scope closes.
      */
    def serve(
        endpoints: List[ZServerEndpoint[Any, ZioStreams]],
        port: Int,
        host: String = "localhost"
    ): RIO[Scope, org.http4s.server.Server] =
        for
            executor <- ZIO.executor
            server <- BlazeServerBuilder[[A] =>> RIO[Any, A]]
                .withExecutionContext(executor.asExecutionContext)
                .bindHttp(port, host)
                .withHttpApp(routes(endpoints).orNotFound)
                .resource
                .toScopedZIO
        yield server

end HttpServer
