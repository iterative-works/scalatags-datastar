// PURPOSE: Tapir endpoints for the gallery chrome — the home index and the per-example pages.
// PURPOSE: `/examples/{id}` carries the demo slug; an unknown slug is a 404, like any missing page.
package works.iterative.scalatags.datastar.scenarios

import sttp.tapir.*
import sttp.model.StatusCode

/** The gallery's page routes.
  *
  * [[home]] is the site root (constrained to `/`, so it does not shadow the example or action
  * routes when every endpoint is mounted together). [[example]] addresses one demo by its slug and
  * answers an unknown slug with `404`, the natural status for a page that does not exist.
  */
object GalleryEndpoints:

    /** The landing page, at the site root. */
    val home: PublicEndpoint[Unit, Unit, String, Any] =
        endpoint.get.in("").out(htmlBodyUtf8)

    /** A single example page, addressed by its demo slug; an unknown slug is a `404`. */
    val example: PublicEndpoint[String, Unit, String, Any] =
        endpoint.get
            .in("examples" / path[String]("id"))
            .errorOut(statusCode(StatusCode.NotFound))
            .out(htmlBodyUtf8)

end GalleryEndpoints
