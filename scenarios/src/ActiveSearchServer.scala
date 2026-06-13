// PURPOSE: Server logic for the active-search example — the SSE handler on the house stack.
// PURPOSE: Decodes the `datastar` query param, filters contacts, patches the same contacts Frag.
package works.iterative.scalatags.datastar.scenarios

import sttp.tapir.ztapir.*
import sttp.capabilities.zio.ZioStreams
import org.http4s.HttpRoutes
import zio.*
import works.iterative.scalatags.datastar.tapir.sse.*

/** The active-search example's action handler.
  *
  * Receives the `datastar` query parameter already decoded into the typed `ActiveSearch` store,
  * filters the catalogue, and answers with a `patch-elements` event built from the very
  * `ActiveSearchView.contacts` fragment the page first rendered — one template, both directions.
  */
object ActiveSearchServer:

    // snippet: active-search-server
    private val searchLogic: ZServerEndpoint[Any, ZioStreams] =
        ActiveSearchEndpoints.search.zServerLogic: query =>
            val event = ServerSentEvents.patchElements(
                ActiveSearchView.contacts(Contacts.matching(query.search))
            )
            ZIO.succeed(datastarStream(event))
    // snippet-end

    val serverEndpoints: List[ZServerEndpoint[Any, ZioStreams]] =
        List(searchLogic)

    val routes: HttpRoutes[[A] =>> RIO[Any, A]] =
        HttpServer.routes(serverEndpoints)

    /** Binds a Blaze server serving just active search to `host`/`port`, scoped. */
    def serve(port: Int, host: String = "localhost"): RIO[Scope, org.http4s.server.Server] =
        HttpServer.serve(serverEndpoints, port, host)

end ActiveSearchServer
