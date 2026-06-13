// PURPOSE: Server logic for the dbmon example — re-randomises rows each frame and patches the table.
// PURPOSE: A bounded feed paced by the requested fps; mutationRate gates which rows change.
package works.iterative.scalatags.datastar.scenarios

import sttp.tapir.ztapir.*
import sttp.capabilities.zio.ZioStreams
import org.http4s.HttpRoutes
import zio.*
import zio.stream.ZStream
import works.iterative.scalatags.datastar.tapir.sse.*

/** The dbmon example's handler: a bounded feed that, each frame, randomly mutates a fraction of the
  * database rows (set by `mutationRate`) and patches the table, paced by the requested `fps`.
  */
object DbmonServer:

    /** How many frames a feed runs before ending on its own. */
    private val frames: Int = 200

    /** Recomputes the rows, changing each with probability `mutationRate`%, and the patch event. */
    private def frame(config: Dbmon): UIO[String] =
        for
            current <- Databases.cell.get
            mutated <- ZIO.foreach(current): database =>
                for
                    roll <- Random.nextIntBounded(100)
                    queries <- Random.nextIntBetween(1, 20)
                    slowest <- Random.nextDoubleBetween(0.1, 15.0)
                yield
                    if roll < config.mutationRate then
                        database.copy(queries = queries, slowest = slowest)
                    else database
            _ <- Databases.cell.set(mutated)
        yield ServerSentEvents.patchElements(
            DbmonView.rows(mutated),
            selector = Some("#databases"),
            mode = ElementPatchMode.Inner
        )

    // snippet: dbmon-server
    private val updatesLogic: ZServerEndpoint[Any, ZioStreams] =
        DbmonEndpoints.updates.zServerLogic: config =>
            val delay = Duration.fromMillis(1000L / math.max(1, config.fps))
            val events: ZStream[Any, Throwable, String] =
                ZStream.fromIterable(1 to frames).mapZIO(_ => frame(config).delay(delay))
            ZIO.succeed(datastarStream(events))
    // snippet-end

    val serverEndpoints: List[ZServerEndpoint[Any, ZioStreams]] =
        List(updatesLogic)

    val routes: HttpRoutes[[A] =>> RIO[Any, A]] =
        HttpServer.routes(serverEndpoints)

    /** Binds a Blaze server serving just dbmon to `host`/`port`, scoped. */
    def serve(port: Int, host: String = "localhost"): RIO[Scope, org.http4s.server.Server] =
        HttpServer.serve(serverEndpoints, port, host)

end DbmonServer
