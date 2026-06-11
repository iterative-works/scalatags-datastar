// PURPOSE: Reads source snippets from the classpath so the gallery shows the code that compiled.
// PURPOSE: Pure region extraction plus a thin classpath-loading edge — one snippet per named region.
package works.iterative.scalatags.datastar.scenarios

import zio.*
import scala.io.Source as ScalaSource
import java.nio.charset.StandardCharsets.UTF_8

/** Turns a marked region of a source file into a clean snippet for display.
  *
  * A snippet is delimited in the real source by `// snippet: <region>` and `// snippet-end` marker
  * lines. Because the gallery reads the very files that compiled (they ride on the classpath), a
  * shown snippet can never drift from the code that runs. [[extract]] is pure and carries the
  * region logic; [[load]] is the only effect — reading a classpath resource.
  */
object Sources:

    private val Open = "// snippet:"
    private val Close = "// snippet-end"

    /** Pulls the lines between `// snippet: <region>` and `// snippet-end` out of `source`, with
      * the region's common leading indentation removed and the marker lines dropped. A missing or
      * unterminated region is a `Left` — the caller decides whether that is fatal.
      */
    def extract(source: String, region: String): Either[String, String] =
        val open = s"$Open $region"
        val lines = source.linesIterator.toVector
        lines.indexWhere(_.trim == open) match
            case -1 => Left(s"no snippet region '$region'")
            case start =>
                lines.indexWhere(_.trim == Close, start + 1) match
                    case -1  => Left(s"snippet region '$region' is not closed")
                    case end => Right(dedent(lines.slice(start + 1, end)).mkString("\n"))
        end match
    end extract

    /** Drops the largest blank prefix shared by every non-blank line, so a snippet lifted from deep
      * inside a method reads flush-left.
      */
    private def dedent(lines: Vector[String]): Vector[String] =
        val indents = lines.filter(_.trim.nonEmpty).map(_.takeWhile(_ == ' ').length)
        val common = if indents.isEmpty then 0 else indents.min
        lines.map(line => if line.length >= common then line.drop(common) else line)
    end dedent

    /** Reads a classpath resource (a `.scala` file at the classpath root) as UTF-8 text. */
    def load(resource: String): Task[String] =
        ZIO.attemptBlocking(Option(getClass.getResourceAsStream("/" + resource))).flatMap:
            case None =>
                ZIO.fail(new NoSuchElementException(s"resource not found: $resource"))
            case Some(stream) =>
                ZIO.attemptBlocking:
                    try ScalaSource.fromInputStream(stream, UTF_8.name).mkString
                    finally stream.close()

    /** Loads `resource` from the classpath and extracts its `region` snippet, failing if either
      * step does.
      */
    def snippet(resource: String, region: String): Task[String] =
        load(resource).flatMap: source =>
            ZIO.fromEither(extract(source, region))
                .mapError(message => new NoSuchElementException(s"$resource: $message"))

end Sources
