// PURPOSE: The dbmon example's domain — the rate-control signals and the database rows it mutates.
// PURPOSE: A Cell holds the table; the feed re-randomises rows each frame at the chosen rate.
package works.iterative.scalatags.datastar.scenarios

import works.iterative.scalatags.datastar.Signals
import zio.json.JsonDecoder
import zio.json.JsonEncoder

// snippet: dbmon-store
/** The dbmon controls: how often the table updates (frames per second) and what fraction of rows
  * change each frame. These ride the updates request as signals, so adjusting them re-opens the
  * feed at the new rate.
  */
final case class Dbmon(fps: Int = 4, mutationRate: Int = 40)
    derives Signals, JsonEncoder, JsonDecoder
object Dbmon extends Signals.Handles[Dbmon]:
    val fps = signal("fps")
    val mutationRate = signal("mutationRate")

/** One database cluster's live statistics. */
final case class Database(name: String, queries: Int, slowest: Double)

/** The dbmon example's store: the cluster rows the feed mutates. */
object Databases:
    val seed: Seq[Database] =
        (1 to 6).map(index => Database(s"Cluster $index", queries = 0, slowest = 0.0))
    val cell: Cell[Vector[Database]] = Cell(seed.toVector)
// snippet-end
