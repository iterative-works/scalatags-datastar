# scalatags-datastar — Design & Roadmap

Typed Scalatags bindings for the [Datastar](https://data-star.dev) hypermedia
framework, with a typed expression DSL and compiler-checked references to Tapir
endpoints.

## Goal

Datastar drives reactivity from `data-*` attributes whose **values are magic strings**.
There are exactly three kinds, and we replace each with a compiler-checked contract:

| Magic string in a template | Example                              | Made typed by                    |
|----------------------------|--------------------------------------|----------------------------------|
| Signal references          | `data-text="$user.name"`             | a `Signal[A]` model              |
| The expression language    | `data-show="$count > 5 && !$busy"`   | an `Expr[A]` operator DSL        |
| **Backend endpoint URLs**  | `data-on:click="@get('/search')"`    | **Tapir `Endpoint` reverse-route** |

The third is the "make sure endpoints actually exist" goal: `@get(endpoint)(inputs)`
takes a Tapir endpoint *value*, so a reference to a missing or wrong-shaped endpoint
does not compile, and the URL is generated from the endpoint so it cannot drift.

**Symmetry.** The server's SSE responses patch with the *same* Scalatags `Frag`
templates and the *same* typed `Signals` model, so the frontend template and the
backend handler share compiler-checked contracts in both directions.

**Key distinction — two channels, not one.** An endpoint's typed input (path/query)
is *separate* from signals. Datastar appends the whole signal store to every request
automatically (GET → `datastar` query param, others → JSON body). So the Tapir input
models only the explicit URL params; the signal store is modelled by `Signals` and
decoded server-side via `readSignals`.

## Decisions (2026-06)

- **Repo:** git, default branch `main`.
- **Targets:** binding layer cross-compiled JVM + JS (nearly free, platform-neutral
  `generic.Attr`); SSE SDK and endpoint bridge JVM-only (server concerns).
- **SSE backend:** build a native codec on Scalatags + Tapir/http4s and validate it
  against the official Datastar SDK conformance test suite — *not* a dependency on
  `zio-http-datastar-sdk` (wrong server stack, trivial wire format).
- **Example app:** tapir + http4s + ZIO, to dogfood the real house stack and actually
  exercise the typed-endpoint guarantee.
- **Baseline:** Mill 1.1.2, Scala 3.3.8 (LTS), scalatags 0.13.1, tapir 1.13.20, ZIO, http4s
  — matching `scalatags-webawesome` and the iw-support libs.

## Module plan

| Module                       | Purpose                                                   | Targets   |
|------------------------------|-----------------------------------------------------------|-----------|
| `scalatags-datastar`         | core attribute + action bindings (scalatags only)         | JVM + JS  |
| `scalatags-datastar-tapir`   | endpoint reverse-routing bridge (typed backend actions)   | JVM + JS  |
| `scalatags-datastar-sse`     | server SSE codec: patch-elements / -signals / readSignals | JVM       |
| `tapirsse`                   | tapir↔SSE server bridge: input codecs + event stream      | JVM       |
| `scenarios`                  | tapir + http4s + ZIO dogfood app                          | JVM       |

Kept separate so the core bindings stay dependency-light (scalatags only), exactly
like `scalatags-webawesome`.

## Datastar reference notes (verified)

- Attribute syntax is **colon notation**: `data-on:click`, `data-signals:foo`,
  `data-bind:foo`. Modifiers attach with `__`: `data-on:click__window__debounce.500ms.leading`.
  Kebab keys auto-convert to camelCase signal names.
- Two SSE event types from the server:
  - `datastar-patch-elements` — `data:` keys: `selector`, `mode`
    (`outer`/`inner`/`replace`/`prepend`/`append`/`before`/`after`/`remove`),
    `useViewTransition`, `viewTransitionSelector`, `namespace`, `elements` (HTML).
  - `datastar-patch-signals` — `data:` keys: `signals` (JSON), `onlyIfMissing`.
- Backend actions in expressions: `@get/@post/@put/@patch/@delete(uri, options={})`;
  non-backend: `@setAll`, `@toggleAll`, `@peek`, etc.

## Phases

- **Phase 0 — Scaffold & decisions.** ✅ DONE (this commit). Mill build, configs, core
  binding skeleton with colon-notation attributes, passing smoke test, cross-compile
  verified.
- **Phase 1 — Attribute bindings.** ✅ DONE. Full **standard** `data-*` surface; no-modifier
  attributes are plain `generic.Attr`, the rest return a typed `DataAttr` subtype exposing only
  their valid modifiers through a fluent builder (`.debounce(500.millis, leading = true)`, `.once`,
  `.window`, `.caseKebab`, `.threshold(0.5)`, …), rendered to Datastar's `__mod.arg` wire form.
  `:=` works directly on the wrapper (no implicit conversion, no feature warnings). Exhaustive
  wire-format tests. **Pro (paid-tier) attributes deferred** — `data-persist`, `data-scroll-into-view`,
  `data-view-transition`, `data-query-string`, `data-on-raf`, `data-on-resize`, `data-animate`,
  `data-match-media`, `data-custom-validity`, `data-replace-url` — several carry unique modifier sets;
  add as a separate opt-in `DatastarPro` when needed.
- **Phase 2 — Typed signals + `Expr[A]` DSL.** ✅ DONE. `Expr[A]` is the typed counterpart to
  Datastar's JS expression strings: operators (`&&`/`||`/`unary_!`, `===`/`!==` since Scala can't
  override `==`, `< <= > >=`, `+ - * / %`, string `+`) build a tree rendered with JS precedence and
  associativity, inserting only the parentheses meaning requires. `Signal[A]` is a named reference
  rendering `$name`. Any attribute binds a typed expression via an `AttrValue` over `Expr` subtypes
  (`dataShow := count > lit(5)`), found through `Expr`'s implicit scope on both backends; the String
  escape hatch is unaffected. The **signal model is a case class** (`derives Signals`, the
  single source of truth chosen over standalone declarations): Mirror-derived, it renders the initial
  `data-signals` object literal (`{count: 0, step: 1}`, nesting for nested models) via a tiny
  hand-rolled encoder — **no JSON dependency in core**. Typed **handles** come from
  `Signals.Handles[A]` mixed into the companion (`val count = signal("count")`): a `transparent
  inline def` looks the name up against the model's fields (no quotes macro), fails to compile on an
  unknown field, and types the handle `Signal[FieldType]`, so call sites read a stable member
  (`Counter.count`). `data-bind` binds a handle by its bare name (`data-bind="step"`). The full
  counter view (signals + expressions + handles + a reverse-routed action) renders end to end.
  Cross-compiles JVM+JS. *Deferred polish:* per-attribute expected-type constraints (e.g. forcing
  `dataShow` to `Expr[Boolean]`), `_.count` selector-macro sugar (only if the string form chafes),
  `dataSignals := model` sugar, typed object-literal maps for `data-class`/`-attr`/`-style`.
- **Phase 3 — Tapir endpoint bridge.** ✅ DONE. `EndpointUrl.urlOf(endpoint): I => String`
  reverse-routes via the sttp client interpreter with no base URI, yielding a relative `/path?query`
  (leading slash guaranteed; query values form-encoded and round-tripping through Tapir's own server
  decoder). The `endpoint.action` extension is total: `endpoint.action(input): String` (an input-free
  endpoint is `endpoint.action`). It derives the Datastar action from the endpoint: the verb from
  `endpoint.method` (the four mutating verbs map directly; a methodless endpoint or a non-action method
  such as HEAD falls back to `@get`, mirroring Tapir's own client interpreter which realizes a
  methodless endpoint as `method.getOrElse(Method.GET)`), the URL by reverse-routing. The embedded URL
  is escaped into the single-quoted literal so an apostrophe in a value cannot break out or inject
  expression syntax (every other literal-breaking char is percent-encoded by the router — pinned by
  test). Wires into `dataOn` end-to-end: `dataOn("click") := toggleTodo.action(7L)` renders
  `data-on:click="@post('/todos/7/toggle')"`. Cross-compiles JVM+JS; the bridge depends on core.
  *Headline differentiator.* The GET fallback is specific to this Tapir-endpoint binding (faithful to
  Tapir); a future non-Tapir action source could choose different verb-resolution logic.
  Action **options** are typed too: `endpoint.action(input, options)` appends Datastar's options object
  after the URL — `@post('/save', {contentType: 'form'})`. `ActionOptions` covers `contentType` (a `ContentType`
  enum → `'json'`/`'form'`) and `headers` (insertion-ordered, keys quoted since header names carry `-`,
  values escaped). Only set fields render, so the default leaves a bare `@verb('/url')` and every
  existing call site is unchanged. *Deferred (add as fields when needed):* Datastar's remaining action
  options — `selector`, `filterSignals`, `openWhenHidden`, the `retry*` family, `payload`,
  `requestCancellation`.
- **Phase 4 — Native SSE SDK.** ✅ DONE. A JVM-only `sse` module (scalatags + zio-json, no other
  deps) renders the two Datastar server events to their exact wire format. `ServerSentEvents`:
  `patchElements(frag, selector, mode, useViewTransition, namespace, …)` renders a Scalatags `Frag`
  and splits it across `data: elements` lines; `patchSignals(model)` serializes a typed model to
  compact JSON via zio-json (with `patchSignalsRaw` for pre-serialized JSON); `executeScript(js, …)`
  appends a `<script>` to `<body>`. Each renders one event ending in the SSE-terminating blank line;
  concatenating gives a multi-event response. Only non-default options emit data lines (a bare
  `patchElements(frag)` is just `event:` + `data: elements …`), and `retry` is emitted only when it
  differs from Datastar's 1000ms default — matching the reference SDKs. `readSignals[A](json)`
  decodes the round-tripped store into the typed model `A` (the same case class that `derives Signals`
  for the initial value also `derives JsonDecoder` here — the symmetry payoff, pinned by a round-trip
  test). **Validated against the official conformance suite:** the upstream golden `input.json`/
  `output.txt` cases are vendored into the test resources and driven through the codec, compared with
  the *same* semantics as the upstream Go runner (events matched; `event`/`id`/`retry` compared by
  value; `data:` lines grouped by leading key so unlike-line order is insignificant) — all 19 GET and
  1 POST cases pass. *Transport deferred to Phase 5:* the codec is stack-neutral (it produces the SSE
  strings); wiring them into tapir `streamBody` / http4s SSE inherently couples to a concrete
  stream/server stack and can only be end-to-end tested with a running server, so it lands with the
  dogfood app where that stack exists.
- **Phase 5 — Dogfood app + docs.** 🚧 IN PROGRESS. A `scenarios` app on the house server stack —
  ZIO + http4s (Blaze) + tapir's `ZHttp4sServerInterpreter` — dogfoods every layer of the library
  together and, finally, wires the deferred SSE transport.
  **First slice DONE — the server-driven counter:** `GET /` renders a Scalatags page whose typed
  bindings come straight from the library (initial store from `Counter derives Signals` →
  `data-signals`, a typed signal reference → `data-text="$count"`, and a button whose
  `@post('/increment')` action is reverse-routed from a Tapir endpoint). A click sends the whole
  signal store as the request body; the handler decodes it with `readSignals[Counter]`, advances it,
  and answers with a `patch-signals` SSE event built by the Phase-4 codec — streamed back as
  `text/event-stream`. The codec stays the single source of the wire bytes; tapir's
  `streamTextBody(ZioStreams)(TextEventStream)` only carries the string it produced. Faithful to the
  **two-channels** design: the signal store is never a typed tapir input. The template action
  reverse-routes a parent route endpoint (`endpoint.post.in("increment")`, empty input); the server
  endpoint is *built from that same route* by adding the raw body and the SSE output, so the URL the
  browser calls and the handler that answers it cannot drift. Validated by unit (page render +
  domain), integration (in-process http4s routes) and end-to-end (real Blaze on an ephemeral port
  driven by a real sttp client, asserting the response bytes equal the codec output) tests.
  Datastar client pinned to **v1.0.2** — the release whose wire format the codec targets
  (`patch-*`); npm's "latest" tag still ships the older `merge-*` names.
  **Second slice DONE — live search:** `GET /search` renders a page whose debounced `data-on:input`
  fires a reverse-routed `@get('/search/results')`; the handler decodes the store, filters a
  catalogue, and streams a `patch-elements` event whose `<ul id="results">` is produced by the very
  `results(matches)` fragment the page first rendered — one template both directions, the symmetry the
  library exists for. This slice exercises the *other* transport channel: a `@get` action serialises
  the signals into a `datastar` **query parameter** (`ot=GET,DELETE` are bodyless in the client), so
  the server endpoint adds `.in(query[String]("datastar"))` and decodes it through the same
  `readSignals` — two channels, GET-shaped. Shared plumbing was factored out: `HttpServer` (the
  Blaze/route boilerplate), `Layout` (the document shell pinning the client version in one place), and
  `Scenarios` (the composed endpoint set the entrypoint and routing tests share). Composing the two
  examples surfaced — and a new `ScenariosRoutesTest` now guards — a routing bug: a pathless
  `endpoint.get` is a catch-all that shadowed `/search`; the counter page is now `endpoint.get.in("")`
  (root only).
  **Third slice DONE — examples gallery:** the two standalone pages became a sidebar-navigated gallery
  modelled on data-star.dev/examples — each demo runs *beside the typed Scala that produces it*. The
  source panels are read at runtime from the files that compiled (the `scenarios` `src` tree rides on
  the classpath via an overridden `resources`), delimited by `// snippet:` regions so an excerpt can
  never drift from the code that runs; `Sources.extract` is pure (region slice + dedent) with
  classpath loading the only effect. A `Demo` registry (id, blurb, live widget, snippet refs) drives
  both the sidebar and the routes — a new example is one entry. Pages live under the gallery
  (`GET /` home, `GET /examples/{id}`, unknown slug → 404); each view now exposes a `demo: Frag`
  widget the chrome embeds, while the action endpoints (`POST /increment`, `GET /search/results`) are
  untouched — reverse-routed, they work wherever the widget is mounted. Source is highlighted
  client-side by a pinned highlight.js (**11.11.1**) + Scala grammar, the second deliberately-pinned
  CDN asset beside the Datastar client. Validated by unit (`SourcesTest` region/dedent, `GalleryViewTest`
  chrome), integration (`ScenariosRoutesTest` proves source is read *through* a route) and end-to-end
  (`GalleryE2ETest`) tests.
  **Fourth slice DONE — the full examples gallery:** the gallery now reimplements **27 of the 28
  core data-star.dev examples** (every one but `match_media`, which needs the deferred Pro
  `data-match-media` attribute; `animations` exercises `useViewTransition` with only core attributes). Built in TDD batches, each example follows the established store /
  view / endpoints / handler shape plus a `Demos` entry and snippet regions, with view-unit and
  in-process routes-integration tests (the shared `ScenariosRoutesTest` verifies store state after
  each mutation, with `reset()` bookends). New library pieces this required: the over-time
  `datastarStream(ZStream[String])` feed overload in `tapirsse` (for `progress-bar`/`progressive-load`/
  `bad-apple`/`dbmon`), and two scenario-local state abstractions — `Repository[Id, T]` over a ZIO
  `Ref` (shared by `delete-row`/`edit-row`/`bulk-update`/`todomvc`) and its single-record sibling
  `Cell[A]` (shared by `click-to-edit`/`templ-counter`/`dbmon`), each created eagerly at construction
  so handlers stay on the `Any` environment and the interpreter is untouched. Stateful list demos
  lazy-load their rows via `data-init` so a reload reflects the live store. Together the examples
  cover every action verb, both request channels plus `formBody`, the `Expr` DSL, and every SSE patch
  mode. *Remaining:* `match_media` (a ~10-line `DatastarPro` `dataMatchMedia` binding, if the gallery
  is to advertise Pro support), llms.txt, contributing.
- **Phase 6 (optional).** Generate SSE constants/enums from `datastar-sdk-config.json`. Component
  codegen is *not* warranted — the attribute set is small and stable (YAGNI).

## Status

Phases 1–4 complete, all tests green (JVM + JS where applicable). Phase 1: full standard attribute
surface + typed modifier builder. Phase 2: the `Expr[A]` DSL (precedence-correct JS-expression
rendering), `Signal[A]`, a case-class `Signals` model deriving the initial `data-signals` JSON
(macro-free, no JSON dependency) and field-checked typed handles via `Signals.Handles[A]`, and typed
attribute binding — the full counter view composes end to end. Phase 3: endpoint reverse-routing
(`urlOf`) and typed Datastar actions (`action`) derived from Tapir endpoints, composing with `dataOn`
end-to-end — the headline "endpoints must exist" feature is proven. `action` is total (`I => String`):
the verb is derived from the endpoint's method, falling back to `@get` for a methodless or non-action
endpoint exactly as Tapir's own client interpreter does; a typed `ActionOptions` (`contentType`,
`headers`) renders Datastar's action options object when supplied. Phase 4: the JVM-only `sse` module
(scalatags + zio-json) — `ServerSentEvents` (patch-elements / patch-signals / executeScript) renders
the two Datastar server events to their exact wire format, and `readSignals` decodes the round-tripped
store into the typed model (the symmetry payoff), validated against the official conformance suite (all
19 GET + 1 POST golden cases). Phase 5 is underway: the `scenarios` dogfood app (ZIO + http4s/Blaze +
tapir) now presents its examples as a sidebar **gallery** (each demo shown beside the classpath-read
source that produces it, highlighted client-side): a server-driven counter (POST body →
`patch-signals`) and a live search (debounced `@get`, signals in the `datastar` query param →
`patch-elements`, one fragment rendering both the initial list and every patch) — exercising the typed
bindings, the endpoint bridge and the SSE codec end to end, wiring the previously deferred SSE transport
(`streamTextBody` over `text/event-stream`) and proving it with unit, in-process integration and
real-socket end-to-end tests. That transport is now a library module of its own — the JVM-only
`tapirsse` bridge (`works.iterative.scalatags.datastar.tapir.sse`): `SignalsInput.body/.query` decode
the round-tripped store into a typed input in the codec layer (a misfit payload is a `400`, never a
match in the handler), `datastarEvents` is the `text/event-stream` output, and `datastarStream` turns
the codec's rendered event strings into the response byte stream. It is the server-side third import
star, re-exporting the SSE codec so a handler reaches the whole server side through one import; the
`scenarios` app consumes it instead of hand-plumbing tapir/`ZStream`.
Next within Phase 5: more canonical examples (todo, click-to-edit, polling, SSE feed) and the docs
(llms.txt, contributing).
