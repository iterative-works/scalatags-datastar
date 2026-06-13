// PURPOSE: Server logic for the svg-morphing example — streams a circle with a random colour/radius.
// PURPOSE: Uses the ZIO Random service; patches the SVG with the svg namespace.
package works.iterative.scalatags.datastar.scenarios

import sttp.tapir.ztapir.*
import sttp.capabilities.zio.ZioStreams
import org.http4s.HttpRoutes
import zio.*
import works.iterative.scalatags.datastar.tapir.sse.*

/** The svg-morphing example's handler: picks a random colour and radius and patches a fresh circle
  * in the SVG namespace.
  */
object SvgMorphingServer:

    private val colors: Seq[String] =
        Seq("#2563eb", "#dc2626", "#16a34a", "#9333ea", "#ea580c", "#0891b2")

    // snippet: svg-morphing-server
    private val morphLogic: ZServerEndpoint[Any, ZioStreams] =
        SvgMorphingEndpoints.morph.zServerLogic: _ =>
            for
                colorIndex <- Random.nextIntBounded(colors.size)
                radius <- Random.nextIntBetween(20, 50)
            yield datastarStream(
                ServerSentEvents.patchElements(
                    SvgMorphingView.circle(colors(colorIndex), radius),
                    namespace = Some("svg")
                )
            )
    // snippet-end

    val serverEndpoints: List[ZServerEndpoint[Any, ZioStreams]] =
        List(morphLogic)

    val routes: HttpRoutes[[A] =>> RIO[Any, A]] =
        HttpServer.routes(serverEndpoints)

    /** Binds a Blaze server serving just svg-morphing to `host`/`port`, scoped. */
    def serve(port: Int, host: String = "localhost"): RIO[Scope, org.http4s.server.Server] =
        HttpServer.serve(serverEndpoints, port, host)

end SvgMorphingServer
