// PURPOSE: Server logic for the file-upload example — reports the count and size it received.
// PURPOSE: Reads the decoded base64 array; size is approximated from the encoded length.
package works.iterative.scalatags.datastar.scenarios

import org.http4s.HttpRoutes
import sttp.capabilities.zio.ZioStreams
import sttp.tapir.ztapir.*
import works.iterative.scalatags.datastar.tapir.sse.*
import zio.*

/** The file-upload example's handler: takes the decoded base64 files and patches a report of the
  * count and approximate total size.
  */
object FileUploadServer:

    /** The decoded byte size of a base64 payload, ignoring padding. */
    private def approxBytes(base64: String): Int = (base64.length * 3) / 4

    // snippet: file-upload-server
    private val submitLogic: ZServerEndpoint[Any, ZioStreams] =
        FileUploadEndpoints.submit.zServerLogic: files =>
            val count = files.upload.size
            val totalBytes = files.upload.map(file => approxBytes(file.contents)).sum
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
