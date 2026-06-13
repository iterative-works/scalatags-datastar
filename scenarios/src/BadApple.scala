// PURPOSE: The bad-apple example's domain — a tiny ASCII animation streamed frame by frame.
// PURPOSE: A self-contained loop of frames stands in for the video the original streams at 30fps.
package works.iterative.scalatags.datastar.scenarios

// snippet: bad-apple-frames
/** A small looping ASCII animation — a ball bouncing across a field — stood up so the example can
  * stream frames over SSE the way the original streams its video, without bundling a video.
  */
object BadAppleFrames:

    private def field(position: Int): String =
        val width = 12
        val row = (0 until width).map(column => if column == position then "●" else "·").mkString
        s"""+------------+
           ||$row|
           |+------------+""".stripMargin
    end field

    /** One full back-and-forth sweep of frames. */
    val all: Seq[String] =
        val forward = (0 until 12).map(field)
        forward ++ forward.reverse

end BadAppleFrames
// snippet-end
