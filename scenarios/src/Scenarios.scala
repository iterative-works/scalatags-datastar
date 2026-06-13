// PURPOSE: The composed scenarios app — every example's endpoints mounted on one server.
// PURPOSE: One place defines the union, so the entrypoint and its tests route through the same set.
package works.iterative.scalatags.datastar.scenarios

import sttp.tapir.ztapir.*
import sttp.capabilities.zio.ZioStreams
import org.http4s.HttpRoutes
import zio.*

/** All scenario examples, mounted together.
  *
  * The gallery contributes the page routes (home and each example); every example contributes its
  * own SSE action endpoints. This object is the single composition the runnable [[Server]] serves
  * and the routing tests exercise, so they can never drift.
  */
object Scenarios:

    val endpoints: List[ZServerEndpoint[Any, ZioStreams]] =
        GalleryServer.serverEndpoints ++
            CounterServer.serverEndpoints ++
            SearchServer.serverEndpoints ++
            ActiveSearchServer.serverEndpoints ++
            LazyLoadServer.serverEndpoints ++
            LazyTabsServer.serverEndpoints ++
            TitleUpdateServer.serverEndpoints ++
            ProgressBarServer.serverEndpoints ++
            ProgressiveLoadServer.serverEndpoints ++
            ClickToLoadServer.serverEndpoints ++
            InfiniteScrollServer.serverEndpoints ++
            InlineValidationServer.serverEndpoints ++
            FormDataServer.serverEndpoints ++
            DeleteRowServer.serverEndpoints ++
            EditRowServer.serverEndpoints ++
            BulkUpdateServer.serverEndpoints ++
            TodoMvcServer.serverEndpoints ++
            ClickToEditServer.serverEndpoints ++
            SvgMorphingServer.serverEndpoints

    val routes: HttpRoutes[[A] =>> RIO[Any, A]] =
        HttpServer.routes(endpoints)

end Scenarios
