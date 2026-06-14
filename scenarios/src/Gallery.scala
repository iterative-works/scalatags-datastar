// PURPOSE: The examples gallery chrome — a sidebar of demos and a pane showing one live, with source.
// PURPOSE: Pure rendering: it is handed already-loaded snippets, so the file reading stays at the edge.
package works.iterative.scalatags.datastar.scenarios

import scalatags.Text.all.*

/** Renders the gallery: a persistent sidebar that navigates between [[Demos]] and a content pane
  * that shows the selected demo running, with the source excerpts that build it beside it.
  *
  * The chrome stays a pure function of its inputs — [[demoPage]] receives the snippet text already
  * loaded, so the only effect (reading source from the classpath) lives in [[GalleryServer]]. The
  * source is syntax-highlighted client-side by a pinned highlight.js build, mirroring the Datastar
  * examples page; the Datastar client itself is still pinned once, in [[Layout]].
  */
object Gallery:

    /** The highlight.js release the gallery loads, pinned like the Datastar client to avoid drift.
      */
    val highlightVersion = "11.11.1"

    private def hljsAsset(path: String): String =
        s"https://cdn.jsdelivr.net/gh/highlightjs/cdn-release@$highlightVersion/$path"

    private val styles: String =
        """
          |*, *::before, *::after { box-sizing: border-box; }
          |body { margin: 0; color: #0f172a; line-height: 1.5;
          |  font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, Helvetica, Arial, sans-serif; }
          |.app { display: flex; min-height: 100vh; }
          |.sidebar { flex: 0 0 240px; background: #0f172a; color: #e2e8f0; padding: 1.5rem 1rem; }
          |.sidebar .brand { display: block; font-weight: 700; color: #fff; text-decoration: none; margin-bottom: 1.5rem; }
          |.sidebar ul { list-style: none; margin: 0; padding: 0; }
          |.sidebar li { margin: 0.25rem 0; }
          |.sidebar a { display: block; padding: 0.5rem 0.75rem; border-radius: 0.375rem; color: #cbd5e1; text-decoration: none; }
          |.sidebar a:hover { background: #1e293b; color: #fff; }
          |.sidebar a.active { background: #2563eb; color: #fff; }
          |.content { flex: 1; padding: 2rem 2.5rem; max-width: 56rem; }
          |.content h1 { margin-top: 0; }
          |.content header p { color: #475569; font-size: 1.05rem; }
          |.content ul.index { list-style: none; padding: 0; }
          |.content ul.index li { margin: 0.5rem 0; }
          |.demo { border: 1px solid #e2e8f0; border-radius: 0.5rem; padding: 1.5rem; margin: 1.5rem 0 2rem; background: #f8fafc; }
          |.source figure { margin: 0 0 1.5rem; }
          |.source figcaption { font-weight: 600; font-size: 0.85rem; color: #475569; margin-bottom: 0.4rem; }
          |.source pre { margin: 0; border: 1px solid #e2e8f0; border-radius: 0.5rem; overflow: auto; }
          |.source pre code { font-size: 0.85rem;
          |  font-family: "JetBrains Mono", "SF Mono", Menlo, Consolas, monospace; }
          |""".stripMargin

    /** Presentation for the demo widgets themselves. The data-star.dev examples lean on the site's
      * stylesheet for their component look; these rules give the embedded widgets the equivalent —
      * progress bars, bar charts, scroll panes, cards, tables and form controls — so each demo
      * reads the way it does on the official site. Everything is scoped under `.demo`, so it never
      * reaches the sidebar chrome or the highlighted source panels.
      */
    private val demoStyles: String =
        """
          |.demo h3 { margin: 0 0 0.75rem; font-size: 1rem; }
          |.demo > div > p, .demo p { margin: 0 0 0.75rem; }
          |.demo ul { list-style: none; padding: 0; margin: 0; }
          |.demo .muted, .demo .empty { color: #64748b; }
          |.demo table { border-collapse: collapse; width: 100%; background: #fff; }
          |.demo th, .demo td { padding: 0.4rem 0.6rem; border-bottom: 1px solid #e2e8f0; text-align: left; }
          |.demo thead th { font-size: 0.8rem; color: #475569; font-weight: 600; }
          |.demo td.active { color: #16a34a; font-weight: 600; }
          |.demo td.inactive { color: #94a3b8; }
          |.demo input:not([type=checkbox]):not([type=radio]), .demo select, .demo textarea {
          |  padding: 0.4rem 0.5rem; border: 1px solid #cbd5e1; border-radius: 0.375rem; font: inherit; }
          |.demo button { padding: 0.4rem 0.75rem; border: 1px solid #cbd5e1; border-radius: 0.375rem;
          |  background: #fff; cursor: pointer; font: inherit; }
          |.demo button:hover { background: #f1f5f9; }
          |.demo button.success { border-color: #16a34a; color: #166534; }
          |.demo button.danger { border-color: #dc2626; color: #b91c1c; }
          |.demo button.destroy { border: none; background: none; color: #dc2626; font-size: 1.1rem;
          |  padding: 0 0.4rem; line-height: 1; }
          |.demo .toolbar, .demo .actions, .demo .controls, .demo .filters {
          |  display: flex; gap: 0.5rem; flex-wrap: wrap; align-items: center; margin-bottom: 0.75rem; }
          |.demo .field { display: flex; flex-direction: column; gap: 0.25rem; margin-bottom: 0.75rem; max-width: 22rem; }
          |.demo .error { color: #dc2626; font-size: 0.85rem; }
          |.demo .progress { display: flex; align-items: center; gap: 0.75rem; }
          |.demo .track { flex: 1; height: 1rem; background: #e2e8f0; border-radius: 0.5rem; overflow: hidden; }
          |.demo .fill { height: 100%; background: #2563eb; transition: width 0.2s ease; }
          |.demo .label { min-width: 3rem; font-variant-numeric: tabular-nums; color: #475569; }
          |.demo .bars { display: flex; align-items: flex-end; gap: 0.5rem; height: 110px; padding-top: 0.5rem; }
          |.demo .bar { width: 2.5rem; background: #2563eb; border-radius: 0.25rem 0.25rem 0 0; }
          |.demo .scroller { max-height: 280px; overflow-y: auto; border: 1px solid #e2e8f0; border-radius: 0.5rem; }
          |.demo .sentinel { padding: 0.75rem; text-align: center; color: #64748b; }
          |.demo .contact { display: flex; flex-direction: column; padding: 0.5rem 0.75rem; border-bottom: 1px solid #e2e8f0; }
          |.demo .contact .email { color: #64748b; font-size: 0.85rem; }
          |.demo .result { margin-top: 0.75rem; padding: 0.5rem 0.75rem; background: #eef2ff; border-radius: 0.375rem; color: #3730a3; }
          |.demo .section { padding: 0.75rem; border: 1px solid #e2e8f0; border-radius: 0.375rem; margin-bottom: 0.5rem; background: #fff; }
          |.demo .ascii { font-family: "JetBrains Mono", Menlo, Consolas, monospace; white-space: pre;
          |  line-height: 1.05; font-size: 0.7rem; background: #0f172a; color: #e2e8f0; padding: 0.75rem; border-radius: 0.375rem; }
          |.demo .flashable { font-weight: 600; }
          |.demo .technique { margin-bottom: 1.5rem; }
          |.demo .technique:last-child { margin-bottom: 0; }
          |.demo .technique h4 { margin: 0 0 0.25rem; font-size: 0.95rem; }
          |.demo .technique > p { color: #475569; font-size: 0.9rem; margin: 0 0 0.5rem; }
          |.demo .throb { display: inline-block; padding: 0.75rem 1.25rem; border-radius: 0.375rem;
          |  color: #fff; font-weight: 600; transition: background-color 0.7s ease; }
          |.demo .vt-panel { max-width: 16rem; }
          |.demo .vt-panel li { padding: 0.4rem 0.6rem; background: #fff; border: 1px solid #e2e8f0;
          |  border-radius: 0.375rem; margin-bottom: 0.4rem; }
          |.demo .fade-card { transition: opacity 0.6s ease; padding: 0.75rem 1rem; background: #fff;
          |  border: 1px solid #e2e8f0; border-radius: 0.375rem; max-width: 18rem; }
          |.demo .fade-card.fading { opacity: 0; }
          |.demo .fade-in-list { margin-top: 0.5rem; display: flex; flex-direction: column; gap: 0.4rem; }
          |.demo .fade-in-item { padding: 0.4rem 0.6rem; background: #eef2ff; color: #3730a3;
          |  border-radius: 0.375rem; animation: demo-fade-in 1s ease; }
          |@keyframes demo-fade-in { from { opacity: 0; } to { opacity: 1; } }
          |""".stripMargin

    private val headExtra: Frag = frag(
        link(rel := "stylesheet", href := hljsAsset("build/styles/github.min.css")),
        script(src := hljsAsset("build/highlight.min.js")),
        script(src := hljsAsset("build/languages/scala.min.js")),
        script(raw("document.addEventListener('DOMContentLoaded', () => hljs.highlightAll());")),
        tag("style")(raw(styles + demoStyles))
    )

    private def navLink(demo: Demo, active: Boolean): Frag =
        val target = href := s"/examples/${demo.id}"
        if active then a(target, cls := "active")(demo.title)
        else a(target)(demo.title)
    end navLink

    private def sidebar(activeId: Option[String]): Frag =
        tag("nav")(cls := "sidebar")(
            a(cls := "brand", href := "/")("scalatags-datastar"),
            ul(Demos.all.map(demo => li(navLink(demo, activeId.contains(demo.id)))))
        )

    private def chrome(pageTitle: String, activeId: Option[String])(content: Frag*): String =
        Layout.page(pageTitle, headExtra)(
            div(cls := "app")(
                sidebar(activeId),
                tag("main")(cls := "content")(content*)
            )
        )

    /** The landing page: the sidebar plus an index inviting the reader into an example. */
    def home: String =
        chrome("scalatags-datastar examples", None)(
            tag("header")(
                h1("Datastar examples"),
                p(
                    "Live, typed Datastar demos on the house stack — each shown beside the Scala " +
                        "that produces it."
                )
            ),
            ul(cls := "index")(
                Demos.all.map(demo =>
                    li(a(href := s"/examples/${demo.id}")(strong(demo.title)), " — ", demo.blurb)
                )
            )
        )

    /** A single demo: its title and blurb, the live widget in a demo box, then a source panel per
      * snippet (already loaded into `(ref, text)` pairs), highlighted as Scala.
      */
    def demoPage(active: Demo, snippets: Seq[(SnippetRef, String)]): String =
        chrome(s"${active.title} — scalatags-datastar", Some(active.id))(
            tag("header")(h1(active.title), p(active.blurb)),
            tag("section")(cls := "demo")(active.widget),
            tag("section")(cls := "source")(
                snippets.map((ref, text) =>
                    tag("figure")(
                        tag("figcaption")(ref.caption),
                        pre(code(cls := "language-scala")(text))
                    )
                )
            )
        )

end Gallery
