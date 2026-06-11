// PURPOSE: Server logic for the gallery — renders the home index and each example with its sources.
// PURPOSE: The one file-reading edge: loads each demo's snippet regions from the classpath to show.
package works.iterative.scalatags.datastar.scenarios

import sttp.tapir.ztapir.*
import sttp.capabilities.zio.ZioStreams
import zio.*

/** The gallery's page handlers.
  *
  * [[Gallery]] renders purely; the only effect lives here — reading each demo's marked source
  * regions from the classpath via [[Sources]] and handing the loaded text to the view. A demo whose
  * snippet region is missing is a wiring bug, so the load defect is fatal rather than silently
  * swallowed; an unknown slug, by contrast, is an expected `404`.
  */
object GalleryServer:

    private val homeLogic: ZServerEndpoint[Any, Any] =
        GalleryEndpoints.home.zServerLogic(_ => ZIO.succeed(Gallery.home))

    private val exampleLogic: ZServerEndpoint[Any, Any] =
        GalleryEndpoints.example.zServerLogic: id =>
            Demos.byId(id) match
                case None => ZIO.fail(())
                case Some(demo) =>
                    ZIO
                        .foreach(demo.snippets)(ref =>
                            Sources.snippet(ref.resource, ref.region).map(ref -> _)
                        )
                        .map(snippets => Gallery.demoPage(demo, snippets))
                        .orDie

    val serverEndpoints: List[ZServerEndpoint[Any, ZioStreams]] =
        List(homeLogic, exampleLogic)

end GalleryServer
