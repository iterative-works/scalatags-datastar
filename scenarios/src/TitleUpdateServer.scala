// PURPOSE: Server logic for the title-update example — patches the <title> with the server time.
// PURPOSE: Reads the clock from the ZIO environment and targets the title element by CSS selector.
package works.iterative.scalatags.datastar.scenarios

import org.http4s.HttpRoutes
import scalatags.Text.all.raw
import sttp.capabilities.zio.ZioStreams
import sttp.tapir.ztapir.*
import works.iterative.scalatags.datastar.tapir.sse.*
import zio.*

import java.time.OffsetDateTime

/** The title-update example's handler: reads the current time from the clock and answers with a
  * `patch-elements` event whose `selector` is `title`, replacing the document title.
  */
object TitleUpdateServer:

    /** The server time as `HH:MM:SS`. */
    private def clockText(now: OffsetDateTime): String =
        f"${now.getHour}%02d:${now.getMinute}%02d:${now.getSecond}%02d"

    // snippet: title-update-server
    private val updateLogic: ZServerEndpoint[Any, ZioStreams] =
        TitleUpdateEndpoints.update.zServerLogic: _ =>
            Clock.currentDateTime.map: now =>
                val event = ServerSentEvents.patchElements(
                    raw(s"<title>${clockText(now)}</title>"),
                    selector = Some("title")
                )
                datastarStream(event)
    // snippet-end

    val serverEndpoints: List[ZServerEndpoint[Any, ZioStreams]] =
        List(updateLogic)

    val routes: HttpRoutes[[A] =>> RIO[Any, A]] =
        HttpServer.routes(serverEndpoints)

    /** Binds a Blaze server serving just title update to `host`/`port`, scoped. */
    def serve(port: Int, host: String = "localhost"): RIO[Scope, org.http4s.server.Server] =
        HttpServer.serve(serverEndpoints, port, host)

end TitleUpdateServer
