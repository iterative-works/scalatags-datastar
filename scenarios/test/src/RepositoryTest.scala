// PURPOSE: Unit tests for the in-memory Repository — the Ref-backed store the row examples share.
// PURPOSE: Pins atomic read/add/delete/update/updateAll/reset over a seeded collection of rows.
package works.iterative.scalatags.datastar.scenarios

import utest.*
import zio.*

object RepositoryTest extends TestSuite:

    private final case class Box(id: Int, label: String)

    private val runtime = Runtime.default

    private def run[A](z: UIO[A]): A =
        Unsafe.unsafe(implicit u => runtime.unsafe.run(z).getOrThrowFiberFailure())

    private def fresh: Repository[Int, Box] =
        Repository(Seq(Box(1, "a"), Box(2, "b")), _.id)

    val tests = Tests:

        test("all returns the seed in order"):
            assert(run(fresh.all) == Vector(Box(1, "a"), Box(2, "b")))

        test("get finds a row by id, or None"):
            val repo = fresh
            assert(run(repo.get(2)).contains(Box(2, "b")))
            assert(run(repo.get(9)).isEmpty)

        test("add appends a row"):
            val repo = fresh
            run(repo.add(Box(3, "c")))
            assert(run(repo.all).map(_.id) == Vector(1, 2, 3))

        test("delete removes the row with that id"):
            val repo = fresh
            run(repo.delete(1))
            assert(run(repo.all).map(_.id) == Vector(2))

        test("update transforms only the matching row"):
            val repo = fresh
            run(repo.update(2)(_.copy(label = "x")))
            assert(run(repo.get(2)).contains(Box(2, "x")))
            assert(run(repo.get(1)).contains(Box(1, "a")))

        test("updateAll transforms every row"):
            val repo = fresh
            run(repo.updateAll(_.copy(label = "z")))
            assert(run(repo.all).forall(_.label == "z"))

        test("reset restores the seed"):
            val repo = fresh
            run(repo.delete(1))
            run(repo.add(Box(7, "g")))
            run(repo.reset())
            assert(run(repo.all) == Vector(Box(1, "a"), Box(2, "b")))

    end tests

end RepositoryTest
