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
- **Phase 1 — Attribute bindings.** Full `data-*` surface; ergonomic plain/keyed unification
  and a typed modifier builder (`__debounce`, `__once`, `__window`, …); exhaustive
  smoke tests on exact wire output. *Independently shippable — the webawesome-parity deliverable.*
- **Phase 2 — Typed signals + `Expr[A]` DSL.** `Signal[A]`, an expression algebra with Scala
  operator overloading rendering to Datastar expression strings; a `Signals` model deriving
  the initial `data-signals` JSON and typed handles; typed attribute variants.
- **Phase 3 — Tapir endpoint bridge.** `urlOf(endpoint): I => String` reverse-routing (spike:
  Tapir sttp client interpreter with no base URI; fold over `EndpointInput` as fallback);
  typed `@get/@post/…` actions wired into `dataOn`. *Headline differentiator.*
- **Phase 4 — Native SSE SDK.** `patchElements(frag, mode, selector, …)`, `patchSignals(signals)`,
  `readSignals` decoding query/body into the typed model; tapir `streamBody` / http4s SSE.
  Validated against the official conformance test suite.
- **Phase 5 — Dogfood app + docs.** Canonical Datastar examples (live search, todo,
  click-to-edit, polling, SSE feed) on tapir+http4s+ZIO+Scalatags. README, llms.txt, contributing.
- **Phase 6 (optional).** Generate SSE constants/enums from `datastar-sdk-config.json`. Component
  codegen is *not* warranted — the attribute set is small and stable (YAGNI).

## Status

Phase 0 complete. Next: Phase 1 (full attribute surface + typed modifiers).
