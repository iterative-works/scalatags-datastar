// PURPOSE: Server logic for the templ-counter example — reads and advances the shared counter.
// PURPOSE: Both handlers patch the count span; the Cell makes the increment atomic across visitors.
package works.iterative.scalatags.datastar.scenarios

import sttp.tapir.ztapir.*
import sttp.capabilities.zio.ZioStreams
import org.http4s.HttpRoutes
import zio.*
import works.iterative.scalatags.datastar.tapir.sse.*

/** The templ-counter example's handlers: [[countLogic]] patches the current shared count;
  * [[incrementLogic]] advances it atomically and patches the new value.
  */
object TemplCounterServer:

    private def patchCount(value: Int): zio.stream.Stream[Throwable, Byte] =
        datastarStream(ServerSentEvents.patchElements(TemplCounterView.count(value)))

    // snippet: templ-counter-server
    private val countLogic: ZServerEndpoint[Any, ZioStreams] =
        TemplCounterEndpoints.count.zServerLogic: _ =>
            GlobalCounter.cell.get.map(patchCount)

    private val incrementLogic: ZServerEndpoint[Any, ZioStreams] =
        TemplCounterEndpoints.increment.zServerLogic: _ =>
            GlobalCounter.cell.update(_ + 1) *> GlobalCounter.cell.get.map(patchCount)
    // snippet-end

    val serverEndpoints: List[ZServerEndpoint[Any, ZioStreams]] =
        List(countLogic, incrementLogic)

    val routes: HttpRoutes[[A] =>> RIO[Any, A]] =
        HttpServer.routes(serverEndpoints)

    /** Binds a Blaze server serving just templ-counter to `host`/`port`, scoped. */
    def serve(port: Int, host: String = "localhost"): RIO[Scope, org.http4s.server.Server] =
        HttpServer.serve(serverEndpoints, port, host)

end TemplCounterServer
