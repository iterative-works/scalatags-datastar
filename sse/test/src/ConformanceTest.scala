// PURPOSE: Runs the official Datastar SDK golden conformance cases against our SSE codec.
// PURPOSE: Compares output the way the upstream Go runner does — by event fields and data subgroups.
package works.iterative.scalatags.datastar.sse

import scalatags.Text.all.raw
import utest.*
import zio.json.*
import zio.json.ast.Json

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import scala.concurrent.duration.DurationInt
import scala.jdk.CollectionConverters.*

/** Drives [[ServerSentEvents]] with every vendored golden case and checks the output against the
  * expected SSE, replicating the upstream runner's comparison: events are matched in order,
  * `event`/`id`/`retry` are compared by value, and `data:` lines are grouped by their leading key
  * before comparison so the order of unlike data lines within an event does not matter.
  */
object ConformanceTest extends TestSuite:

    val tests = Tests {
        test("golden GET cases match the codec output")(runAll("get"))
        test("golden POST cases match the codec output")(runAll("post"))
    }

    private val goldenDir: Path = Paths.get(getClass.getResource("/golden").toURI)

    private def runAll(kind: String): Unit =
        val failures = listCaseDirs(goldenDir.resolve(kind)).flatMap { dir =>
            val name = dir.getFileName.toString
            val input = Files.readString(dir.resolve("input.json"))
            val expected = Files.readString(dir.resolve("output.txt"))
            val actual = renderInput(name, input)
            compareEvents(name, parseSse(expected), parseSse(actual))
        }
        if failures.nonEmpty then sys.error(failures.mkString("\n"))
    end runAll

    // --- driving the codec from a golden input.json ---------------------------------------------

    private final case class Input(events: List[InputEvent]) derives JsonDecoder

    private final case class InputEvent(
        @jsonField("type") tpe: String,
        elements: Option[String] = None,
        selector: Option[String] = None,
        mode: Option[String] = None,
        useViewTransition: Option[Boolean] = None,
        namespace: Option[String] = None,
        signals: Option[Json] = None,
        @jsonField("signals-raw") signalsRaw: Option[String] = None,
        onlyIfMissing: Option[Boolean] = None,
        eventId: Option[String] = None,
        retryDuration: Option[Int] = None,
        script: Option[String] = None,
        attributes: Option[Json] = None,
        autoRemove: Option[Boolean] = None
    ) derives JsonDecoder

    private def renderInput(name: String, json: String): String =
        json.fromJson[Input] match
            case Left(err)     => sys.error(s"$name: could not parse input.json: $err")
            case Right(parsed) => parsed.events.map(renderEvent).mkString
    end renderInput

    private def renderEvent(ev: InputEvent): String =
        val retry =
            ev.retryDuration.map(_.millis).getOrElse(ServerSentEvents.DefaultSseRetryDuration)
        ev.tpe match
            case "patchElements" =>
                ServerSentEvents.patchElements(
                    raw(ev.elements.getOrElse("")),
                    selector = ev.selector,
                    mode = ev.mode.flatMap(ElementPatchMode.fromValue).getOrElse(
                        ElementPatchMode.Outer
                    ),
                    useViewTransition = ev.useViewTransition.getOrElse(false),
                    namespace = ev.namespace,
                    eventId = ev.eventId,
                    retryDuration = retry
                )
            case "patchSignals" =>
                val signalsJson = ev.signalsRaw.orElse(ev.signals.map(_.toJson)).getOrElse("")
                ServerSentEvents.patchSignalsRaw(
                    signalsJson,
                    onlyIfMissing = ev.onlyIfMissing.getOrElse(false),
                    eventId = ev.eventId,
                    retryDuration = retry
                )
            case "executeScript" =>
                ServerSentEvents.executeScript(
                    ev.script.getOrElse(""),
                    attributes = attributesOf(ev.attributes),
                    autoRemove = ev.autoRemove.getOrElse(true),
                    eventId = ev.eventId,
                    retryDuration = retry
                )
            case other => sys.error(s"unknown event type: $other")
        end match
    end renderEvent

    private def attributesOf(json: Option[Json]): Seq[(String, String)] = json match
        case Some(Json.Obj(fields)) =>
            fields.toList.map((key, value) =>
                key -> (value match
                    case Json.Str(s) => s
                    case other       => other.toJson
                )
            )
        case _ => Seq.empty

    // --- semantic SSE comparison (mirrors the upstream Go runner) -------------------------------

    private final case class SseEvent(fields: List[(String, String)]):
        def valuesOf(name: String): List[String] = fields.collect { case (n, v) if n == name => v }
        def names: Set[String] = fields.map(_._1).toSet
    end SseEvent

    private def parseSse(data: String): List[SseEvent] =
        val (events, current) =
            data.split("\n", -1).foldLeft((List.empty[SseEvent], List.empty[(String, String)])) {
                case ((evs, cur), line) =>
                    if line.isEmpty then
                        if cur.nonEmpty then (evs :+ SseEvent(cur), List.empty) else (evs, cur)
                    else
                        val idx = line.indexOf(':')
                        if idx == -1 then (evs, cur)
                        else (evs, cur :+ (line.substring(0, idx) -> line.substring(idx + 1).trim))
            }
        if current.nonEmpty then events :+ SseEvent(current) else events
    end parseSse

    private def compareEvents(
        name: String,
        expected: List[SseEvent],
        actual: List[SseEvent]
    ): List[String] =
        if expected.length != actual.length then
            List(s"$name: expected ${expected.length} event(s), got ${actual.length}")
        else
            expected.zip(actual).zipWithIndex.flatMap { case ((e, a), i) =>
                compareEvent(s"$name event ${i + 1}", e, a)
            }

    private def compareEvent(label: String, expected: SseEvent, actual: SseEvent): List[String] =
        val fieldErrs = ((expected.names ++ actual.names) - "data").toList.sorted.flatMap { field =>
            val e = expected.valuesOf(field)
            val a = actual.valuesOf(field)
            Option.when(e != a)(s"$label: field '$field' expected $e, got $a")
        }
        val expectedData = groupData(expected.valuesOf("data"))
        val actualData = groupData(actual.valuesOf("data"))
        val dataErrs = (expectedData.keySet ++ actualData.keySet).toList.sorted.flatMap { key =>
            val e = expectedData.getOrElse(key, Nil)
            val a = actualData.getOrElse(key, Nil)
            Option.when(e != a)(s"$label: data '$key' expected $e, got $a")
        }
        fieldErrs ++ dataErrs
    end compareEvent

    private def groupData(dataValues: List[String]): Map[String, List[String]] =
        dataValues.foldLeft(Map.empty[String, List[String]]) { (groups, field) =>
            val parts = field.split(" ", 2)
            val key = parts(0)
            val content = if parts.length > 1 then parts(1) else ""
            groups.updated(key, groups.getOrElse(key, Nil) :+ content)
        }

    private def listCaseDirs(dir: Path): List[Path] =
        val stream = Files.list(dir)
        try stream.iterator().asScala.filter(Files.isDirectory(_)).toList.sortBy(
                _.getFileName.toString
            )
        finally stream.close()
    end listCaseDirs

end ConformanceTest
