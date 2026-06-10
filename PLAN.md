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
- **Baseline:** Mill 1.1.2, Scala 3.3.7, scalatags 0.13.1, tapir 1.13.15, ZIO, http4s
  — matching `scalatags-webawesome` and the iw-support libs.

## Module plan

| Module                       | Purpose                                                   | Targets   |
|------------------------------|-----------------------------------------------------------|-----------|
| `scalatags-datastar`         | core attribute + action bindings (scalatags only)         | JVM + JS  |
| `scalatags-datastar-tapir`   | endpoint reverse-routing bridge (typed backend actions)   | JVM + JS  |
| `scalatags-datastar-sse`     | server SSE codec: patch-elements / -signals / readSignals | JVM       |
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
- **Phase 2 — Typed signals + `Expr[A]` DSL.** `Signal[A]`, an expression algebra with Scala
  operator overloading rendering to Datastar expression strings; a `Signals` model deriving
  the initial `data-signals` JSON and typed handles; typed attribute variants.
- **Phase 3 — Tapir endpoint bridge.** ✅ Core DONE. `EndpointUrl.urlOf(endpoint): I => String`
  reverse-routes via the sttp client interpreter with no base URI, yielding a relative `/path?query`
  (leading slash guaranteed; query values form-encoded and round-tripping through Tapir's own server
  decoder). `EndpointAction.action(endpoint): I => String` is total — it derives the Datastar action
  from the endpoint: the verb from `endpoint.method` (the four mutating verbs map directly; a
  methodless endpoint or a non-action method such as HEAD falls back to `@get`, mirroring Tapir's own
  client interpreter which realizes a methodless endpoint as `method.getOrElse(Method.GET)`), the URL
  by reverse-routing. The embedded URL is escaped into the single-quoted literal so an apostrophe in a
  value cannot break out or inject expression syntax (every other literal-breaking char is
  percent-encoded by the router — pinned by test). Wires into `dataOn` end-to-end:
  `dataOn("click") := action(ep)(input)` renders `data-on:click="@post('/todos/7/toggle')"`.
  Cross-compiles JVM+JS; the published bridge depends on tapir only (core dependency is test-scoped).
  *Headline differentiator.* The GET fallback is specific to this Tapir-endpoint binding (faithful to
  Tapir); a future non-Tapir action source could choose different verb-resolution logic.
  Remaining for Phase 3 polish: action `options` (`{contentType, headers}`) if needed.
- **Phase 4 — Native SSE SDK.** `patchElements(frag, mode, selector, …)`, `patchSignals(signals)`,
  `readSignals` decoding query/body into the typed model; tapir `streamBody` / http4s SSE.
  Validated against the official conformance test suite.
- **Phase 5 — Dogfood app + docs.** Canonical Datastar examples (live search, todo,
  click-to-edit, polling, SSE feed) on tapir+http4s+ZIO+Scalatags. README, llms.txt, contributing.
- **Phase 6 (optional).** Generate SSE constants/enums from `datastar-sdk-config.json`. Component
  codegen is *not* warranted — the attribute set is small and stable (YAGNI).

## Status

Phases 1 and 3-core complete, cross-compiled, all tests green. Phase 1: full standard attribute
surface + typed modifier builder. Phase 3 core: endpoint reverse-routing (`urlOf`) and typed Datastar
actions (`action`) derived from Tapir endpoints, composing with `dataOn` end-to-end — the headline
"endpoints must exist" feature is proven. `action` is total (`I => String`): the verb is derived from
the endpoint's method, falling back to `@get` for a methodless or non-action endpoint exactly as
Tapir's own client interpreter does. Next: Phase 2's typed signals + `Expr[A]` DSL, with a typed
`dataOn := action` binding that consumes the action over typed expressions.
