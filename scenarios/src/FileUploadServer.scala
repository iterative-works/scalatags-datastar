// PURPOSE: Server logic for the file-upload example — reports the count and size it received.
// PURPOSE: Reads the decoded base64 array; size is approximated from the encoded length.
package works.iterative.scalatags.datastar.scenarios

import sttp.tapir.ztapir.*
import sttp.capabilities.zio.ZioStreams
import org.http4s.HttpRoutes
import zio.*
import works.iterative.scalatags.datastar.tapir.sse.*

/** The file-upload example's handler: takes the decoded base64 files and patches a report of the
  * count and approximate total size.
  */
object FileUploadServer:

    /** The decoded byte size of a base64 string, dropping any `data:…;base64,` prefix. */
    private def approxBytes(base64: String): Int =
        val payload = base64.substring(base64.indexOf(",") + 1)
        (payload.length * 3) / 4

    // snippet: file-upload-server
    private val submitLogic: ZServerEndpoint[Any, ZioStreams] =
        FileUploadEndpoints.submit.zServerLogic: upload =>
            val count = upload.upload.size
            val totalBytes = upload.upload.map(approxBytes).sum
            val message =
                if count == 0 then "No files selected."
                else s"Received $count file(s), about $totalBytes bytes."
            ZIO.succeed(
                datastarStream(ServerSentEvents.patchElements(FileUploadView.result(message)))
            )
    // snippet-end

    val serverEndpoints: List[ZServerEndpoint[Any, ZioStreams]] =
        List(submitLogic)

    val routes: HttpRoutes[[A] =>> RIO[Any, A]] =
        HttpServer.routes(serverEndpoints)

    /** Binds a Blaze server serving just file-upload to `host`/`port`, scoped. */
    def serve(port: Int, host: String = "localhost"): RIO[Scope, org.http4s.server.Server] =
        HttpServer.serve(serverEndpoints, port, host)

end FileUploadServer
