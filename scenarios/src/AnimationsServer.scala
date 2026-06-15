// PURPOSE: Server logic for the animations example — the throb feed, the toggle, the two fades.
// PURPOSE: Color throb and fade-out are paced ZStream feeds; view transitions toggles and re-patches.
package works.iterative.scalatags.datastar.scenarios

import org.http4s.HttpRoutes
import scalatags.Text.all.frag
import sttp.capabilities.zio.ZioStreams
import sttp.tapir.ztapir.*
import works.iterative.scalatags.datastar.tapir.sse.*
import zio.*
import zio.stream.ZStream

/** The animations example's handlers, one per technique.
  *
  *   - [[colorThrobLogic]] streams a colour per tick, each replacing `#color-throb`.
  *   - [[viewTransitionLogic]] reads the client's state, returns the other panel order with a view
  *     transition, and patches `swapped` forward so successive clicks alternate.
  *   - [[fadeOutLogic]] patches the `fading` class, waits for the CSS transition, then removes the
  *     card.
  *   - [[fadeInLogic]] appends an item that fades itself in via a CSS keyframe.
  */
object AnimationsServer:

    /** Between throb colours, and between fading the card and removing it. */
    private val tick: Duration = 700.millis

    // snippet: animations-server
    private val colorThrobLogic: ZServerEndpoint[Any, ZioStreams] =
        AnimationsEndpoints.colorThrob.zServerLogic: _ =>
            val cycle =
                Animations.throbColors ++ Animations.throbColors :+ Animations.throbColors.head
            val events = ZStream
                .fromIterable(cycle)
                .map(color => ServerSentEvents.patchElements(AnimationsView.colorThrob(color)))
                .mapZIO(event => ZIO.sleep(tick).as(event))
            ZIO.succeed(datastarStream(events))

    private val viewTransitionLogic: ZServerEndpoint[Any, ZioStreams] =
        AnimationsEndpoints.viewTransition.zServerLogic: vt =>
            val next = !vt.swapped
            ZIO.succeed(datastarStream(
                ServerSentEvents.patchElements(
                    AnimationsView.vtPanel(next),
                    useViewTransition = true
                ),
                ServerSentEvents.patchSignals(ViewTransition(next))
            ))

    private val fadeOutLogic: ZServerEndpoint[Any, ZioStreams] =
        AnimationsEndpoints.fadeOut.zServerLogic: _ =>
            val fade = ServerSentEvents.patchElements(AnimationsView.fadeOutCard(fading = true))
            val remove = ServerSentEvents.patchElements(
                frag(),
                selector = Some("#fade-out-card"),
                mode = ElementPatchMode.Remove
            )
            val events = ZStream.succeed(fade) ++
                ZStream.succeed(remove).mapZIO(event => ZIO.sleep(tick).as(event))
            ZIO.succeed(datastarStream(events))

    private val fadeInLogic: ZServerEndpoint[Any, ZioStreams] =
        AnimationsEndpoints.fadeIn.zServerLogic: _ =>
            Random.nextIntBetween(1, 1000).map: n =>
                datastarStream(
                    ServerSentEvents.patchElements(
                        AnimationsView.fadeInItem(s"Faded in #$n"),
                        selector = Some("#fade-in-list"),
                        mode = ElementPatchMode.Append
                    )
                )
    // snippet-end

    val serverEndpoints: List[ZServerEndpoint[Any, ZioStreams]] =
        List(colorThrobLogic, viewTransitionLogic, fadeOutLogic, fadeInLogic)

    val routes: HttpRoutes[[A] =>> RIO[Any, A]] =
        HttpServer.routes(serverEndpoints)

    /** Binds a Blaze server serving just the animations example to `host`/`port`, scoped. */
    def serve(port: Int, host: String = "localhost"): RIO[Scope, org.http4s.server.Server] =
        HttpServer.serve(serverEndpoints, port, host)

end AnimationsServer
