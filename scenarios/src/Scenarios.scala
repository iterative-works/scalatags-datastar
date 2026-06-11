// PURPOSE: The composed scenarios app — every example's endpoints mounted on one server.
// PURPOSE: One place defines the union, so the entrypoint and its tests route through the same set.
package works.iterative.scalatags.datastar.scenarios

import sttp.tapir.ztapir.*
import sttp.capabilities.zio.ZioStreams
import org.http4s.HttpRoutes
import zio.*

/** All scenario examples, mounted together.
  *
  * Each example contributes its own server endpoints; this object is the single composition the
  * runnable [[Server]] serves and the routing tests exercise, so they can never drift.
  */
object Scenarios:

    val endpoints: List[ZServerEndpoint[Any, ZioStreams]] =
        CounterServer.serverEndpoints ++ SearchServer.serverEndpoints

    val routes: HttpRoutes[[A] =>> RIO[Any, A]] =
        HttpServer.routes(endpoints)

end Scenarios
