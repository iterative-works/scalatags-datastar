// PURPOSE: Server logic for the inline-validation example — validates the store, patches the errors.
// PURPOSE: Validate patches every field's error element; submit either re-shows them or succeeds.
package works.iterative.scalatags.datastar.scenarios

import sttp.tapir.ztapir.*
import sttp.capabilities.zio.ZioStreams
import org.http4s.HttpRoutes
import zio.*
import works.iterative.scalatags.datastar.tapir.sse.*

/** The inline-validation example's handlers.
  *
  * [[validateLogic]] patches every field's error element from the pure rules — empty content when
  * the field is valid, so a fixed error clears. [[submitLogic]] does the same on an invalid form,
  * but on a valid one replaces the whole form with a success message.
  */
object InlineValidationServer:

    /** One `patch-elements` event per field error element, replacing it by id. */
    private def errorEvents(form: SignupForm): Seq[String] =
        SignupValidation.messages(form).map: (errorId, message) =>
            ServerSentEvents.patchElements(InlineValidationView.errorElement(errorId, message))

    // snippet: inline-validation-server
    private val validateLogic: ZServerEndpoint[Any, ZioStreams] =
        InlineValidationEndpoints.validate.zServerLogic: form =>
            ZIO.succeed(datastarStream(errorEvents(form)*))

    private val submitLogic: ZServerEndpoint[Any, ZioStreams] =
        InlineValidationEndpoints.submit.zServerLogic: form =>
            if SignupValidation.isValid(form) then
                ZIO.succeed(
                    datastarStream(ServerSentEvents.patchElements(InlineValidationView.success))
                )
            else ZIO.succeed(datastarStream(errorEvents(form)*))
    // snippet-end

    val serverEndpoints: List[ZServerEndpoint[Any, ZioStreams]] =
        List(validateLogic, submitLogic)

    val routes: HttpRoutes[[A] =>> RIO[Any, A]] =
        HttpServer.routes(serverEndpoints)

    /** Binds a Blaze server serving just inline-validation to `host`/`port`, scoped. */
    def serve(port: Int, host: String = "localhost"): RIO[Scope, org.http4s.server.Server] =
        HttpServer.serve(serverEndpoints, port, host)

end InlineValidationServer
