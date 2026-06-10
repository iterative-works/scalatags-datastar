# scalatags-datastar

Scalatags bindings for the [Datastar](https://data-star.dev) hypermedia framework.

The goal is more than plain bindings: **fully statically-typed Datastar templates**,
where signal references, the expression language, and — crucially — the backend
endpoints referenced by `@get`/`@post`/… are all checked by the compiler. Backend
endpoints are [Tapir](https://tapir.softwaremill.com) endpoints, so a template can
only reference a route that actually exists, and its URL is generated from the endpoint.

See [PLAN.md](./PLAN.md) for the design and roadmap.

## Status

Early. Phase 0 (scaffold) complete: core `data-*` attribute builders with a passing
smoke test, cross-compiled for JVM and JS.

```scala
import scalatags.Text.all.*
import works.iterative.scalatags.datastar.Datastar.*

button(dataOn("click") := "@post('/save')")("Save")
// <button data-on:click="@post('/save')">Save</button>

div(dataSignals := "{count: 0}", dataText := "$count")
// <div data-signals="{count: 0}" data-text="$count"></div>
```

## Build

Mill 1.1.2, Scala 3.3.7.

```bash
./mill datastar.jvm.test       # run JVM smoke tests
./mill datastar.js.compile     # cross-compile check
./mill __.reformat             # format
```

## License

MIT.
