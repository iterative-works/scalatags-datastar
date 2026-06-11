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

    private val headExtra: Frag = frag(
        link(rel := "stylesheet", href := hljsAsset("build/styles/github.min.css")),
        script(src := hljsAsset("build/highlight.min.js")),
        script(src := hljsAsset("build/languages/scala.min.js")),
        script(raw("document.addEventListener('DOMContentLoaded', () => hljs.highlightAll());")),
        tag("style")(raw(styles))
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
