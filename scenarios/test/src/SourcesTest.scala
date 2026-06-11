// PURPOSE: Unit tests for snippet extraction — region boundaries, dedent, and classpath loading.
// PURPOSE: Proves a displayed snippet is the real source between its markers, indentation normalised.
package works.iterative.scalatags.datastar.scenarios

import utest.*
import zio.*

object SourcesTest extends TestSuite:

    private val runtime = Runtime.default

    private def run[A](z: Task[A]): A =
        Unsafe.unsafe(implicit u => runtime.unsafe.run(z).getOrThrowFiberFailure())

    val tests = Tests:

        test("extract returns the lines between the markers, the markers dropped"):
            val src =
                """object X:
                  |    // snippet: a
                  |    val x = 1
                  |    // snippet-end
                  |end X""".stripMargin
            assert(Sources.extract(src, "a") == Right("val x = 1"))

        test("extract strips the region's common leading indentation"):
            val src =
                """// snippet: b
                  |        val a = 1
                  |          val b = 2
                  |// snippet-end""".stripMargin
            assert(Sources.extract(src, "b") == Right("val a = 1\n  val b = 2"))

        test("extract distinguishes multiple regions in one source"):
            val src =
                """// snippet: first
                  |one
                  |// snippet-end
                  |// snippet: second
                  |two
                  |// snippet-end""".stripMargin
            assert(Sources.extract(src, "first") == Right("one"))
            assert(Sources.extract(src, "second") == Right("two"))

        test("extract reports a missing region"):
            assert(Sources.extract("nothing here", "x").isLeft)

        test("extract reports an unterminated region"):
            assert(Sources.extract("// snippet: x\ncode", "x").isLeft)

        test("load reads a real source file from the classpath"):
            val code = run(Sources.load("Sources.scala"))
            assert(code.contains("object Sources"))

        test("load fails clearly when the resource is absent"):
            val failure = run(Sources.load("Nope.scala").either)
            assert(failure.isLeft)

    end tests

end SourcesTest
