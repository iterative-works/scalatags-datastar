// PURPOSE: Integration test for the composed app — the gallery pages and every example's action.
// PURPOSE: Guards against one endpoint shadowing another, and proves source is read through a route.
package works.iterative.scalatags.datastar.scenarios

import utest.*
import zio.*
import zio.interop.catz.*
import org.http4s.*
import org.http4s.implicits.*

object ScenariosRoutesTest extends TestSuite:

    private type F[A] = RIO[Any, A]

    private val runtime = Runtime.default

    private def run[A](z: F[A]): A =
        Unsafe.unsafe(implicit u => runtime.unsafe.run(z).getOrThrowFiberFailure())

    private val app = Scenarios.routes.orNotFound

    private def call(request: Request[F]): (Response[F], String) =
        run(app.run(request).flatMap(resp => resp.as[String].map(resp -> _)))

    /** The response without draining its body — for streaming feeds whose body emits over time. */
    private def respond(request: Request[F]): Response[F] =
        run(app.run(request))

    private def isEventStream(response: Response[F]): Boolean =
        response.contentType.exists(_.mediaType == MediaType.`text/event-stream`)

    val tests = Tests:

        test("GET / serves the gallery home listing every demo"):
            val (response, body) = call(Request[F](Method.GET, uri"/"))
            assert(response.status.code == 200)
            assert(body.contains("""href="/examples/counter""""))
            assert(body.contains("""href="/examples/search""""))

        test("GET /examples/counter serves the counter demo beside its source"):
            val (response, body) = call(Request[F](Method.GET, uri"/examples/counter"))
            assert(response.status.code == 200)
            // the live widget...
            assert(body.contains("""data-on:click="@post('/increment')""""))
            // ...and its source, read from the classpath and marked for the highlighter
            assert(body.contains("""class="language-scala""""))
            assert(body.contains("final case class Counter"))

        test("GET /examples/search serves the live-search demo, not the counter"):
            val (response, body) = call(Request[F](Method.GET, uri"/examples/search"))
            assert(response.status.code == 200)
            assert(body.contains("""data-bind="query""""))
            assert(!body.contains("""@post('/increment')"""))

        test("GET /examples/{unknown} is a 404"):
            val (response, _) = call(Request[F](Method.GET, uri"/examples/nope"))
            assert(response.status.code == 404)

        test("POST /increment serves the counter action"):
            val request = Request[F](Method.POST, uri"/increment")
                .withEntity("""{"count":0,"step":1}""")
            val (response, _) = call(request)
            assert(response.status.code == 200)
            assert(response.contentType.exists(_.mediaType == MediaType.`text/event-stream`))

        test("GET /search/results serves the search action"):
            val request = Request[F](
                Method.GET,
                uri"/search/results".withQueryParam("datastar", """{"query":"go"}""")
            )
            val (response, _) = call(request)
            assert(response.status.code == 200)
            assert(response.contentType.exists(_.mediaType == MediaType.`text/event-stream`))

        test("POST /increment with a body that does not fit the store is a 400"):
            val request = Request[F](Method.POST, uri"/increment")
                .withEntity("""{"count":"nope"}""")
            val (response, _) = call(request)
            assert(response.status.code == 400)

        test("GET /search/results with a datastar param that does not fit the store is a 400"):
            val request = Request[F](
                Method.GET,
                uri"/search/results".withQueryParam("datastar", "not json")
            )
            val (response, _) = call(request)
            assert(response.status.code == 400)

        test("the home page lists the Batch 0 demos too"):
            val (_, body) = call(Request[F](Method.GET, uri"/"))
            assert(body.contains("""href="/examples/active-search""""))
            assert(body.contains("""href="/examples/lazy-tabs""""))
            assert(body.contains("""href="/examples/progress-bar""""))

        test("GET /active-search/search filters the catalogue and patches the contacts"):
            val request = Request[F](
                Method.GET,
                uri"/active-search/search".withQueryParam("datastar", """{"search":"bern"}""")
            )
            val (response, body) = call(request)
            assert(response.status.code == 200)
            assert(isEventStream(response))
            assert(body.contains("Bernier"))
            assert(body.contains("""id="contact-list""""))

        test("GET /lazy-load/graph streams the loaded fragment"):
            val (response, body) = call(Request[F](Method.GET, uri"/lazy-load/graph"))
            assert(response.status.code == 200)
            assert(isEventStream(response))
            assert(body.contains("Quarterly revenue"))

        test("GET /lazy-tabs/3 returns the widget with tab 3 selected"):
            val (response, body) = call(Request[F](Method.GET, uri"/lazy-tabs/3"))
            assert(response.status.code == 200)
            assert(isEventStream(response))
            assert(body.contains("content of tab 3"))
            assert(body.contains("""aria-selected="true""""))

        test("POST /title-update patches the document title by selector"):
            val (response, body) = call(Request[F](Method.POST, uri"/title-update"))
            assert(response.status.code == 200)
            assert(isEventStream(response))
            assert(body.contains("data: selector title"))
            assert(body.contains("<title>"))

        test("GET /progress-bar/updates opens an event-stream feed"):
            val response = respond(Request[F](Method.GET, uri"/progress-bar/updates"))
            assert(response.status.code == 200)
            assert(isEventStream(response))

        test("GET /progressive-load/updates opens an event-stream feed"):
            val request = Request[F](
                Method.GET,
                uri"/progressive-load/updates".withQueryParam("datastar", """{"loadDisabled":true}""")
            )
            val response = respond(request)
            assert(response.status.code == 200)
            assert(isEventStream(response))

        test("GET /progressive-load/updates with a datastar param that does not fit is a 400"):
            val request = Request[F](
                Method.GET,
                uri"/progressive-load/updates".withQueryParam("datastar", "not json")
            )
            val response = respond(request)
            assert(response.status.code == 400)

        test("GET /click-to-load/more appends the next page and advances the offset"):
            val request = Request[F](
                Method.GET,
                uri"/click-to-load/more".withQueryParam("datastar", """{"offset":10}""")
            )
            val (response, body) = call(request)
            assert(response.status.code == 200)
            assert(isEventStream(response))
            assert(body.contains("Agent 11"))
            assert(body.contains("data: mode append"))
            assert(body.contains("""data: signals {"offset":20}"""))

        test("GET /infinite-scroll/more appends a page and re-arms the sentinel"):
            val request = Request[F](
                Method.GET,
                uri"/infinite-scroll/more".withQueryParam("datastar", """{"offset":10}""")
            )
            val (response, body) = call(request)
            assert(response.status.code == 200)
            assert(isEventStream(response))
            assert(body.contains("Agent 11"))
            assert(body.contains("data-on-intersect__once"))
            assert(body.contains("""data: signals {"offset":20}"""))

        test("POST /inline-validation/validate patches each field's error from the store"):
            val request = Request[F](Method.POST, uri"/inline-validation/validate")
                .withEntity("""{"email":"nope","firstName":"","lastName":""}""")
            val (response, body) = call(request)
            assert(response.status.code == 200)
            assert(isEventStream(response))
            assert(body.contains("valid email"))
            assert(body.contains("required"))

        test("POST /inline-validation/submit succeeds for a valid form"):
            val request = Request[F](Method.POST, uri"/inline-validation/submit")
                .withEntity("""{"email":"fresh@example.com","firstName":"Jo","lastName":"Lee"}""")
            val (response, body) = call(request)
            assert(response.status.code == 200)
            assert(body.contains("Thanks for signing up"))

        test("POST /inline-validation/submit re-shows errors for an invalid form"):
            val request = Request[F](Method.POST, uri"/inline-validation/submit")
                .withEntity("""{"email":"","firstName":"","lastName":""}""")
            val (_, body) = call(request)
            assert(body.contains("required"))
            assert(!body.contains("Thanks for signing up"))

        test("POST /form-data/submit reads the form-encoded fields and echoes them"):
            val request = Request[F](Method.POST, uri"/form-data/submit")
                .withEntity(UrlForm("name" -> "Pizza", "toppings" -> "cheese", "toppings" -> "onion"))
            val (response, body) = call(request)
            assert(response.status.code == 200)
            assert(isEventStream(response))
            assert(body.contains("name = Pizza"))
            assert(body.contains("toppings = cheese"))
            assert(body.contains("toppings = onion"))

        test("GET /delete-row/rows renders the current members into the table body"):
            run(Members.repo.reset())
            val (response, body) = call(Request[F](Method.GET, uri"/delete-row/rows"))
            assert(response.status.code == 200)
            assert(isEventStream(response))
            assert(body.contains("data: mode inner"))
            assert(body.contains("Joe Smith"))

        test("DELETE /delete-row/{id} removes the member from the store"):
            run(Members.repo.reset())
            val (deleted, deleteBody) = call(Request[F](Method.DELETE, uri"/delete-row/2"))
            assert(deleted.status.code == 200)
            assert(deleteBody.contains("data: mode remove"))
            assert(deleteBody.contains("selector #member-2"))
            val (_, rowsBody) = call(Request[F](Method.GET, uri"/delete-row/rows"))
            assert(!rowsBody.contains("Angie MacDowell"))
            run(Members.repo.reset())

        test("GET /edit-row/{id}/edit returns the inline edit form for that row"):
            val (response, body) = call(Request[F](Method.GET, uri"/edit-row/1/edit"))
            assert(response.status.code == 200)
            assert(body.contains("""id="person-1""""))
            assert(body.contains("""name="name""""))
            assert(body.contains("@put('/edit-row/1'"))

        test("PUT /edit-row/{id} saves the edited fields into the store"):
            run(People.repo.reset())
            val request = Request[F](Method.PUT, uri"/edit-row/2")
                .withEntity(UrlForm("name" -> "Angela M.", "email" -> "angela@example.com"))
            val (response, body) = call(request)
            assert(response.status.code == 200)
            assert(body.contains("Angela M."))
            assert(run(People.repo.get(2L)).exists(_.name == "Angela M."))
            run(People.repo.reset())

        test("PUT /bulk-update/activate activates exactly the selected accounts"):
            run(Accounts.repo.reset())
            val request = Request[F](Method.PUT, uri"/bulk-update/activate")
                .withEntity("""{"selections":[true,false,true,false,false]}""")
            val (response, body) = call(request)
            assert(response.status.code == 200)
            assert(body.contains("data: mode inner"))
            assert(run(Accounts.repo.all).filter(_.active).map(_.id).sorted == Vector(1L, 3L))
            run(Accounts.repo.reset())

        test("GET /todomvc/list renders the seeded todos and the count"):
            run(Todos.repo.reset())
            val request = Request[F](
                Method.GET,
                uri"/todomvc/list".withQueryParam("datastar", """{"input":"","mode":"all"}""")
            )
            val (response, body) = call(request)
            assert(response.status.code == 200)
            assert(isEventStream(response))
            assert(body.contains("data: mode inner"))
            assert(body.contains("Taste Datastar"))
            assert(body.contains("items left"))

        test("POST /todomvc/add adds a todo to the store and clears the input"):
            run(Todos.repo.reset())
            val request = Request[F](Method.POST, uri"/todomvc/add")
                .withEntity("""{"input":"Write the docs","mode":"all"}""")
            val (response, body) = call(request)
            assert(response.status.code == 200)
            assert(body.contains("Write the docs"))
            assert(body.contains("""data: signals {"input":""}"""))
            assert(run(Todos.repo.all).exists(_.text == "Write the docs"))
            run(Todos.repo.reset())

        test("PUT /todomvc/{id}/toggle flips a todo's completion in the store"):
            run(Todos.repo.reset())
            val request = Request[F](Method.PUT, uri"/todomvc/2/toggle")
                .withEntity("""{"input":"","mode":"all"}""")
            val (response, _) = call(request)
            assert(response.status.code == 200)
            assert(run(Todos.repo.get(2L)).exists(_.completed))
            run(Todos.repo.reset())

        test("PUT /todomvc/clear-completed removes the completed todos"):
            run(Todos.repo.reset())
            val request = Request[F](Method.PUT, uri"/todomvc/clear-completed")
                .withEntity("""{"input":"","mode":"all"}""")
            val (response, _) = call(request)
            assert(response.status.code == 200)
            assert(run(Todos.repo.all).forall(!_.completed))
            run(Todos.repo.reset())

        test("GET /click-to-edit/view renders the current record"):
            run(Profiles.cell.reset())
            val (response, body) = call(Request[F](Method.GET, uri"/click-to-edit/view"))
            assert(response.status.code == 200)
            assert(isEventStream(response))
            assert(body.contains("Joe Smith"))

        test("PUT /click-to-edit/save persists the posted profile"):
            run(Profiles.cell.reset())
            val request = Request[F](Method.PUT, uri"/click-to-edit/save")
                .withEntity("""{"firstName":"Jane","lastName":"Roe","email":"jane@example.com"}""")
            val (response, body) = call(request)
            assert(response.status.code == 200)
            assert(body.contains("Jane Roe"))
            assert(run(Profiles.cell.get).firstName == "Jane")
            run(Profiles.cell.reset())

        test("PATCH /click-to-edit/reset restores the original record"):
            run(Profiles.cell.set(Profile("Jane", "Roe", "jane@example.com")))
            val (response, body) = call(Request[F](Method.PATCH, uri"/click-to-edit/reset"))
            assert(response.status.code == 200)
            assert(body.contains("Joe Smith"))
            assert(run(Profiles.cell.get).firstName == "Joe")

        test("GET /svg-morphing/morph patches a circle in the svg namespace"):
            val (response, body) = call(Request[F](Method.GET, uri"/svg-morphing/morph"))
            assert(response.status.code == 200)
            assert(isEventStream(response))
            assert(body.contains("data: namespace svg"))
            assert(body.contains("<circle"))

    end tests

end ScenariosRoutesTest
