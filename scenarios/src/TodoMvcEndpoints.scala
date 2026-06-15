// PURPOSE: Tapir endpoints for the TodoMVC example — list, add, toggle, delete, toggle-all, clear.
// PURPOSE: The view-state store rides the body (POST/PUT) or the datastar query (GET/DELETE).
package works.iterative.scalatags.datastar.scenarios

import sttp.capabilities.zio.ZioStreams
import sttp.tapir.*
import works.iterative.scalatags.datastar.tapir.sse.*
import zio.stream.Stream

/** The TodoMVC example's routes. Each mutating action reverse-routes a distinct verb (add `@post`,
  * toggle/toggle-all/clear `@put`, delete `@delete`); the per-item routes carry the id as a typed
  * `Long` path parameter. The client store (input + mode) rides the request — the body for the
  * body-methods, the `datastar` query parameter for the bodyless GET/DELETE.
  */
object TodoMvcEndpoints:

    // snippet: todomvc-endpoints
    val listRoute: PublicEndpoint[Unit, Unit, Unit, Any] =
        endpoint.get.in("todomvc" / "list")

    val addRoute: PublicEndpoint[Unit, Unit, Unit, Any] =
        endpoint.post.in("todomvc" / "add")

    val toggleRoute: PublicEndpoint[Long, Unit, Unit, Any] =
        endpoint.put.in("todomvc" / path[Long]("id") / "toggle")

    val deleteRoute: PublicEndpoint[Long, Unit, Unit, Any] =
        endpoint.delete.in("todomvc" / path[Long]("id"))

    val toggleAllRoute: PublicEndpoint[Unit, Unit, Unit, Any] =
        endpoint.put.in("todomvc" / "toggle-all")

    val clearRoute: PublicEndpoint[Unit, Unit, Unit, Any] =
        endpoint.put.in("todomvc" / "clear-completed")

    val list: PublicEndpoint[TodoMvc, Unit, Stream[Throwable, Byte], ZioStreams] =
        listRoute.in(SignalsInput.query[TodoMvc]).out(datastarEvents)

    val add: PublicEndpoint[TodoMvc, Unit, Stream[Throwable, Byte], ZioStreams] =
        addRoute.in(SignalsInput.body[TodoMvc]).out(datastarEvents)

    val toggle: PublicEndpoint[(Long, TodoMvc), Unit, Stream[Throwable, Byte], ZioStreams] =
        toggleRoute.in(SignalsInput.body[TodoMvc]).out(datastarEvents)

    val delete: PublicEndpoint[(Long, TodoMvc), Unit, Stream[Throwable, Byte], ZioStreams] =
        deleteRoute.in(SignalsInput.query[TodoMvc]).out(datastarEvents)

    val toggleAll: PublicEndpoint[TodoMvc, Unit, Stream[Throwable, Byte], ZioStreams] =
        toggleAllRoute.in(SignalsInput.body[TodoMvc]).out(datastarEvents)

    val clear: PublicEndpoint[TodoMvc, Unit, Stream[Throwable, Byte], ZioStreams] =
        clearRoute.in(SignalsInput.body[TodoMvc]).out(datastarEvents)
    // snippet-end

end TodoMvcEndpoints
