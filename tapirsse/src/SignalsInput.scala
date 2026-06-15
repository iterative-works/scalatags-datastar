// PURPOSE: Tapir inputs that carry Datastar's signal store, decoded into the typed model.
// PURPOSE: Decoding lives in the codec, so a payload that does not fit the store is a 400, not a match.
package works.iterative.scalatags.datastar.tapir.sse

import sttp.tapir.DecodeResult
import sttp.tapir.EndpointIO
import sttp.tapir.EndpointInput
import sttp.tapir.query as datastarQuery
import sttp.tapir.stringJsonBody
import works.iterative.scalatags.datastar.sse.readSignals
import zio.json.*

/** Tapir inputs that carry Datastar's round-tripped signal store, decoded into the typed model `A`.
  *
  * Datastar appends the whole signal store to every backend request — as the JSON request body for
  * a `@post` action, or the `datastar` query parameter for a `@get` one. These push that decoding
  * (via `readSignals`) into Tapir's codec layer: a payload that does not fit `A` becomes a
  * [[DecodeResult.Error]] Tapir answers with `400`, so a handler only ever receives a valid store.
  * They are the inbound counterpart to a reverse-routed action — the action sends the store, this
  * reads it back — and the store still never appears in the route's reverse-routed input, keeping
  * the two transport channels separate.
  */
object SignalsInput:

    private def decode[A](json: String)(using JsonDecoder[A]): DecodeResult[A] =
        readSignals[A](json) match
            case Right(store) => DecodeResult.Value(store)
            case Left(error)  => DecodeResult.Error(json, SignalsDecodeException(error))

    /** The signal store as a JSON request body — how a `@post` action transports it. */
    def body[A](using JsonDecoder[A], JsonEncoder[A]): EndpointIO[A] =
        stringJsonBody.mapDecode(decode[A](_))(_.toJson)

    /** The signal store in the `datastar` query parameter — how a `@get` action transports it. */
    def query[A](using JsonDecoder[A], JsonEncoder[A]): EndpointInput[A] =
        datastarQuery[String]("datastar").mapDecode(decode[A](_))(_.toJson)

end SignalsInput

/** Why the round-tripped signal store did not fit the model — the cause Tapir reports with its
  * `400`.
  */
final case class SignalsDecodeException(error: String)
    extends RuntimeException(s"Could not read signals: $error")
