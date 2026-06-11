# Vendored Datastar SDK conformance cases

These `get/` and `post/` cases are copied verbatim from the official Datastar SDK test suite
(`sdk/tests/golden` in <https://github.com/starfederation/datastar>, MIT-licensed). Each case is a
folder with an `input.json` request payload and an `output.txt` golden SSE response.

`ConformanceTest` drives our codec with each `input.json` and compares its output against
`output.txt` using the same semantic comparison the upstream Go runner applies: events are matched,
`event`/`id`/`retry` fields compared by value, and `data:` lines grouped by their leading key
(`selector`, `mode`, `elements`, `signals`, …) before comparison — so the order of unlike `data:`
lines within an event is not significant, exactly as upstream.
