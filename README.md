# scalatags-datastar

Scalatags bindings for the [Datastar](https://data-star.dev) hypermedia framework.

The goal is more than plain bindings: **fully statically-typed Datastar templates**,
where signal references, the expression language, and — crucially — the backend
endpoints referenced by `@get`/`@post`/… are all checked by the compiler. Backend
endpoints are [Tapir](https://tapir.softwaremill.com) endpoints, so a template can
only reference a route that actually exists, and its URL is generated from the endpoint.

See [PLAN.md](./PLAN.md) for the design and roadmap.

## Status

Early. The full **standard** `data-*` attribute surface with a typed, fluent modifier builder
(Phase 1), and the **Tapir endpoint bridge** (Phase 3 core): backend actions reverse-routed from
typed endpoints, so `@get`/`@post`/… can only reference routes that exist. Both cross-compiled for
JVM and JS. Signal references and the expression DSL are still plain strings — they land in Phase 2.
(Datastar Pro attributes are not yet bound.)

```scala
import scalatags.Text.all.*
import works.iterative.scalatags.datastar.Datastar.*
import scala.concurrent.duration.*

button(dataOn("click") := "@post('/save')")("Save")
// <button data-on:click="@post('/save')">Save</button>

div(dataSignals := "{count: 0}", dataText := "$count")
// <div data-signals="{count: 0}" data-text="$count"></div>

// Modifiers are typed and chainable; each attribute exposes only the ones Datastar accepts:
input(dataOn("input").debounce(300.millis).once := "@get('/search')")
// <input data-on:input__debounce.300ms__once="@get('/search')">

div(dataOnIntersect.once.threshold(0.5) := "@get('/more')")
// <div data-on-intersect__once__threshold.0.5="@get('/more')"></div>
```

### Typed backend actions (Tapir bridge)

Backend actions are reverse-routed from Tapir endpoints, so a template can only reference a
route that exists, and the verb and URL are both derived from the endpoint — they cannot drift.

```scala
import sttp.tapir.*
import works.iterative.scalatags.datastar.tapir.EndpointAction.action

val toggleTodo = endpoint.post.in("todos" / path[Long]("id") / "toggle")

// action derives the verb (POST) and reverse-routes the URL, both from the endpoint.
action(toggleTodo).map(toggle => button(dataOn("click") := toggle(7L))("Toggle"))
// Some(<button data-on:click="@post('/todos/7/toggle')">Toggle</button>)
```

`action` returns `Option[I => String]` — `None` only when the endpoint fixes no Datastar-supported
method (e.g. a bare endpoint with no verb, or a HEAD route). An application resolves its actions once
at startup, where a missing verb fails initialization rather than throwing mid-render. The
reverse-routed URL is escaped into the action's string literal, so values can't break out of the
expression.

## Build

Mill 1.1.2, Scala 3.3.7.

```bash
./mill datastar.jvm.test       # core binding tests
./mill tapir.jvm.test          # endpoint bridge tests
./mill __.compile              # cross-compile check (JVM + JS)
./mill __.reformat             # format
```

## License

MIT.
