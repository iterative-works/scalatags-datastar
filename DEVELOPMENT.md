# Development Guide

How to build, test, format, lint and measure coverage on scalatags-datastar.
For what the library *is*, see [README.md](./README.md); for design and roadmap,
[PLAN.md](./PLAN.md).

**Build system:** [Mill](https://mill-build.org) 1.1.2 (via the bundled `./mill`
launcher), Scala 3.3.8 (LTS), cross-compiled to the JVM and Scala.js 1.21.0.

## Modules

| Module | Platforms | What |
|---|---|---|
| `datastar` | JVM + JS | Core DSL: `data-*` attributes, the `Expr` DSL, the `Signals` model |
| `tapir` | JVM + JS | The `endpoint.action` reverse-routing bridge |
| `sse` | JVM | The native server-side SSE codec and `readSignals` |
| `tapirsse` | JVM | The Tapir + SSE server bridge (signal-store input codecs, event-stream output) |
| `scenarios` | JVM | The runnable examples-gallery dogfood app |

## Building and testing

```bash
./mill __.compile              # cross-compile every module (JVM + JS)

# Per-module tests (these are also the CI steps):
./mill datastar.jvm.test
./mill datastar.js.test        # links to JS and runs on Node
./mill tapir.jvm.test
./mill tapir.js.test           # runs on Node
./mill sse.test
./mill tapirsse.test
./mill scenarios.test          # boots Blaze; real end-to-end HTTP

./mill scenarios.run           # serve the gallery on http://localhost:8080 (PORT overrides)
```

Run the JVM and JS test modules as **separate** `./mill` invocations (as CI does).
Mixing them into one invocation can race in test discovery.

CI is configured at [`.github/workflows/ci.yml`](.github/workflows/ci.yml) and runs
on every push to `main`, every pull request, and on demand (`workflow_dispatch`).
It enforces exactly the gates below.

## Code formatting (scalafmt)

Sources are formatted with scalafmt (config in `.scalafmt.conf`). CI runs the check
ahead of the tests, so a violation fails the build in seconds.

```bash
./mill mill.scalalib.scalafmt.ScalafmtModule/reformatAll __.sources    # reformat
./mill mill.scalalib.scalafmt.ScalafmtModule/checkFormatAll __.sources # verify (CI uses this)
```

## Linting (scalafix)

Static checks and rewrites run via [scalafix](https://scalacenter.github.io/scalafix/)
(the `mill-scalafix` plugin). Rules live in `.scalafix.conf`. Every main module mixes
in the `IWScalacOptions` trait (which brings `ScalafixModule`), and every `test` object
mixes in `ScalafixModule` explicitly, so the lint gate covers test code too.

Active rules:

- **DisableSyntax** — bans `null`, `var`, `throw`, and `return`.
- **OrganizeImports** (`removeUnused = true`, `targetDialect = Scala3`) — sorts and
  explodes imports and drops unused ones via the compiler's `-Wunused:imports`
  diagnostics.
- **RemoveUnused** (`privates`/`locals`/`patternvars`/`params`) — drops unused private
  members, locals, pattern variables and explicit parameters. `RemoveUnused.imports`
  stays `false`, because combining it with `OrganizeImports` produces broken rewrites
  (both touch import statements).

```bash
./mill __.fix            # apply rewrites
./mill __.fix --check    # verify without modifying (CI uses this)
```

`--check` builds SemanticDB, so it requires a clean compile — see below.

## Compiler warnings

Every module mixes in the `IWScalacOptions` trait at the top of `build.mill`. It adds:

```
-deprecation -feature -unchecked
-language:higherKinds -language:implicitConversions
-Wvalue-discard -Wnonunit-statement
-Wunused:all
-Werror
```

`-Werror` makes every warning a compile error, so a clean, warning-free build is
**enforced**, not merely encouraged — `./mill __.compile` (and the scalafix gate that
builds on it) fail on any warning. When a warning is a deliberate, correct choice
(e.g. evidence carried only as a type constraint), mark the specific binding
`@scala.annotation.unused` rather than relaxing the flags.

## Coverage (scoverage)

The five JVM modules are instrumented with [scoverage](https://github.com/scoverage)
(the Scala.js modules are not — Scala 3 coverage is the compiler's native
`-coverage-out`, which the JS backend does not emit). Generate reports locally after
running the tests:

```bash
./mill __.scoverage.xmlReport     # per-module XML
./mill scoverage.htmlReportAll    # aggregated HTML (out/scoverage/htmlReportAll.dest/)
```

CI runs these on a green build, writes a per-module coverage table to the job summary,
and uploads the XML + HTML as the `coverage-report` artifact.

## Publishing

The four library modules publish to [Sonatype Central](https://central.sonatype.com)
under the `works.iterative` namespace; the `scenarios` dogfood app is not published.
The cross-built modules publish both a JVM (`_3`) and a Scala.js (`_sjs1_3`) artifact.

| Module | Artifact |
|---|---|
| `datastar` | `works.iterative::scalatags-datastar` |
| `tapir` | `works.iterative::scalatags-datastar-tapir` |
| `sse` | `works.iterative::scalatags-datastar-sse` |
| `tapirsse` | `works.iterative::scalatags-datastar-tapirsse` |

All modules share one version — `publishVer` in `build.mill`.

### Release process

- **Snapshot:** every push to `main` publishes the current `publishVer` (kept at
  `X.Y.Z-SNAPSHOT` during development) via the
  [Publish workflow](.github/workflows/publish.yml).
- **Release:** bump `publishVer` to drop `-SNAPSHOT`, commit, then tag and push:

  ```bash
  git tag v0.1.0 && git push origin v0.1.0
  ```

  The tag push runs the same workflow and publishes the release. Afterwards bump
  `publishVer` to the next `-SNAPSHOT`.

### Credentials

The workflow needs three repo secrets (set with `gh secret set <name> -R <repo>`):

| Secret | Value |
|---|---|
| `MILL_PGP_SECRET_BASE64` | base64-encoded GPG private signing key |
| `MILL_SONATYPE_USERNAME` | user token from <https://central.sonatype.com/usertoken> |
| `MILL_SONATYPE_PASSWORD` | the token's password (same page) |

To publish from a workstation, export the same variables (plus `MILL_PGP_PASSPHRASE`
if the key has one) from a gitignored `.envrc.local` and run the workflow's command
directly. `./mill __.publishLocal` publishes to `~/.ivy2/local` for testing without
credentials.

## Git hooks

Tracked under `.githooks/`. Wire them once per clone:

```bash
git config core.hooksPath .githooks
```

| Hook | When | What |
|---|---|---|
| `pre-commit` | `git commit` with `.scala` files staged | scalafmt check (the gate CI runs) |
| `pre-push` | `git push` with new commits | scalafix `--check` across all modules (compile is implicit via SemanticDB, and under `-Werror` that means warning-free) |

Both short-circuit when there is nothing relevant to check (docs-only commit, branch
deletion). To bypass in a genuine emergency: `git commit --no-verify` /
`git push --no-verify` — CI will still reject the change, so this is rarely useful.

## git blame

`.git-blame-ignore-revs` lists mechanical-rewrite commits (mass reformat, import
sweeps) so `git blame` shows the real author of each line. Configure once per clone:

```bash
git config blame.ignoreRevsFile .git-blame-ignore-revs
```

GitHub's web blame respects the file automatically.
