package org.distril.beengine.util

import com.nukkitx.math.vector.Vector3i
import kotlin.math.abs
import kotlin.math.floor

enum class Direction(
    private val opposite: Int,
    /**
     * Ordering index for the HORIZONTALS field (S-W-N-E)
     */
    val horizontalIndex: Int,
    val axis: Axis,
    /**
     * Get the horizontal index of this BlockFace (0-3). The order is S-W-N-E
     *
     * @return horizontal index
     */
    val axisDirection: AxisDirection,
    val unitVector: Vector3i
) {

    DOWN(1, -1, Axis.Y, AxisDirection.NEGATIVE, Vector3i.from(0, -1, 0)),
    UP(0, -1, Axis.Y, AxisDirection.POSITIVE, Vector3i.from(0, 1, 0)),

    NORTH(3, 2, Axis.Z, AxisDirection.NEGATIVE, Vector3i.from(0, 0, -1)),
    SOUTH(2, 0, Axis.Z, AxisDirection.POSITIVE, Vector3i.from(0, 0, 1)),

    WEST(5, 1, Axis.X, AxisDirection.NEGATIVE, Vector3i.from(-1, 0, 0)),
    EAST(4, 3, Axis.X, AxisDirection.POSITIVE, Vector3i.from(1, 0, 0));

    /**
     * Get the angle of this BlockFace (0-360)
     *
     * @return horizontal angle
     */
    val horizontalAngle = ((this.horizontalIndex and 3) * 90).toFloat()

    fun getOffset(position: Vector3i, step: Int = 1) =
        position.add(this.unitVector.x * step, this.unitVector.y * step, this.unitVector.z * step)

    /**
     * Get the opposite BlockFace (e.g. DOWN ==&gt; UP)
     *
     * @return block face
     */
    fun getOpposite() = fromIndex(this.opposite)

    /**
     * Rotate this BlockFace around the Y axis clockwise (NORTH =&gt; EAST =&gt; SOUTH =&gt; WEST =&gt; BB_NORTH)
     *
     * @return block face
     */
    fun rotateY() = when (this) {
        NORTH -> EAST
        EAST -> SOUTH
        SOUTH -> WEST
        WEST -> NORTH
        else -> throw RuntimeException("Unable to get Y-rotated face of $this")
    }

    /**
     * Rotate this BlockFace around the Y axis counter-clockwise (NORTH =&gt; WEST =&gt; SOUTH =&gt; EAST =&gt; BB_NORTH)
     *
     * @return block face
     */
    fun rotateYCCW() = when (this) {
        NORTH -> WEST
        EAST -> NORTH
        SOUTH -> EAST
        WEST -> SOUTH
        else -> throw RuntimeException("Unable to get counter-clockwise Y-rotated face of $this")
    }

    enum class Axis(val plane: Plane) {

        X(Plane.HORIZONTAL),
        Y(Plane.VERTICAL),
        Z(Plane.HORIZONTAL);
    }

    enum class AxisDirection(val offset: Int) {

        POSITIVE(1),
        NEGATIVE(-1);
    }

    enum class Plane(vararg val faces: Direction) {

        HORIZONTAL(NORTH, EAST, SOUTH, WEST),
        VERTICAL(UP, DOWN);
    }

    companion object {

        private val values = arrayOfNulls<Direction>(6)

        private val horizontals = arrayOfNulls<Direction>(4)

        init {
            Direction.values().forEach {
                values[it.ordinal] = it
                if (it.axis.plane == Plane.HORIZONTAL) {
                    horizontals[it.horizontalIndex] = it
                }
            }
        }

        fun fromIndex(index: Int) = this.values[abs(index % this.values.size)]

        fun fromHorizontalIndex(index: Int) = this.horizontals[abs(index % this.horizontals.size)]

        fun fromHorizontalAngle(angle: Double) =
            this.fromHorizontalIndex(floor(angle / 90.0 + 0.5).toInt() and 3)

        fun fromAxis(axisDirection: AxisDirection, axis: Axis): Direction {
            this.values.forEach {
                if (it!!.axisDirection == axisDirection && it.axis == axis) {
                    return it
                }
            }

            throw RuntimeException("Unable to get face from axis: $axisDirection $axis")
        }
    }
}
