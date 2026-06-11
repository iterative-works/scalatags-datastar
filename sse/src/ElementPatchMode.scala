// PURPOSE: The strategy Datastar uses to patch elements into the DOM (a patch-elements `mode`).
// PURPOSE: Each case carries the literal `mode` value the SSE wire format expects.
package works.iterative.scalatags.datastar.sse

/** How a `datastar-patch-elements` event places its elements relative to the target.
  *
  * [[Outer]] is Datastar's default (morph the target's outer HTML) and is the only mode that
  * produces no `mode` data line on the wire. [[Remove]] deletes the target and carries no elements.
  */
enum ElementPatchMode(val value: String):
    /** Morph the target's outer HTML (the default). */
    case Outer extends ElementPatchMode("outer")

    /** Morph the target's inner HTML. */
    case Inner extends ElementPatchMode("inner")

    /** Replace the target's outer HTML without morphing. */
    case Replace extends ElementPatchMode("replace")

    /** Prepend the elements inside the target, before its children. */
    case Prepend extends ElementPatchMode("prepend")

    /** Append the elements inside the target, after its children. */
    case Append extends ElementPatchMode("append")

    /** Insert the elements before the target, as siblings. */
    case Before extends ElementPatchMode("before")

    /** Insert the elements after the target, as siblings. */
    case After extends ElementPatchMode("after")

    /** Remove the target from the DOM. */
    case Remove extends ElementPatchMode("remove")
end ElementPatchMode

object ElementPatchMode:
    /** The mode with this wire value, if any. */
    def fromValue(value: String): Option[ElementPatchMode] = values.find(_.value == value)
end ElementPatchMode
