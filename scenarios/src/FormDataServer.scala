// PURPOSE: Server logic for the form-data example — echoes the form-encoded fields it received.
// PURPOSE: Proves the form channel: the handler reads decoded form fields, not the signal store.
package works.iterative.scalatags.datastar.scenarios

import sttp.tapir.ztapir.*
import sttp.capabilities.zio.ZioStreams
import org.http4s.HttpRoutes
import zio.*
import works.iterative.scalatags.datastar.tapir.sse.*

/** The form-data example's handler: takes the decoded form fields and patches an echo of them, so
  * the page shows exactly what the form submitted over the form-encoded channel.
  */
object FormDataServer:

    // snippet: form-data-server
    private val submitLogic: ZServerEndpoint[Any, ZioStreams] =
        FormDataEndpoints.submit.zServerLogic: fields =>
            ZIO.succeed(
                datastarStream(ServerSentEvents.patchElements(FormDataView.formResult(fields)))
            )
    // snippet-end

    val serverEndpoints: List[ZServerEndpoint[Any, ZioStreams]] =
        List(submitLogic)

    val routes: HttpRoutes[[A] =>> RIO[Any, A]] =
        HttpServer.routes(serverEndpoints)

    /** Binds a Blaze server serving just form-data to `host`/`port`, scoped. */
    def serve(port: Int, host: String = "localhost"): RIO[Scope, org.http4s.server.Server] =
        HttpServer.serve(serverEndpoints, port, host)

end FormDataServer
