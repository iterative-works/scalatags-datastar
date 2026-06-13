// PURPOSE: Tapir endpoint for the file-upload example — the POST that carries the base64 files.
// PURPOSE: The files ride the signal body, decoded into FileUpload by SignalsInput.body.
package works.iterative.scalatags.datastar.scenarios

import sttp.tapir.*
import sttp.capabilities.zio.ZioStreams
import zio.stream.Stream
import works.iterative.scalatags.datastar.tapir.sse.*

/** The file-upload example's route: [[submitRoute]] is what the Upload button reverse-routes, and
  * [[submit]] is its server realisation decoding the base64 files from the signal body.
  */
object FileUploadEndpoints:

    // snippet: file-upload-endpoints
    /** The upload route; reverse-routes to `@post('/file-upload/submit')`. */
    val submitRoute: PublicEndpoint[Unit, Unit, Unit, Any] =
        endpoint.post.in("file-upload" / "submit")

    val submit: PublicEndpoint[FileUpload, Unit, Stream[Throwable, Byte], ZioStreams] =
        submitRoute.in(SignalsInput.body[FileUpload]).out(datastarEvents)
    // snippet-end

end FileUploadEndpoints
