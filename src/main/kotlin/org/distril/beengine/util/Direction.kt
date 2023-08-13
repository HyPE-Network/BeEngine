package org.distril.beengine.util

import com.nukkitx.math.vector.Vector3i
import kotlin.math.abs
import kotlin.math.floor

/**
 * Enum class representing different directions in a 3D space.
 * Each direction has an associated opposite direction, an axis, axis direction,
 * and a unit vector indicating the change in position along each axis.
 *
 * @property opposite The index of the opposite direction.
 * @property horizontalIndex Ordering index for the HORIZONTALS field (S-W-N-E).
 * @property axis The axis associated with the direction.
 * @property axisDirection The axis direction (POSITIVE or NEGATIVE) associated with the direction.
 * @property unitVector The unit vector indicating the change in position along each axis.
 */
enum class Direction(
	private val opposite: Int,
	val horizontalIndex: Int,
	val axis: Axis,
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

	/**
	 * Get the offset of the given position along this direction.
	 *
	 * @param position The initial position.
	 * @param step The step size for the offset (default is 1).
	 * @return The updated position after applying the offset.
	 */
	fun getOffset(position: Vector3i, step: Int = 1) =
		position.add(this.unitVector.x * step, this.unitVector.y * step, this.unitVector.z * step)

	/**
	 * Get the opposite Direction (e.g., DOWN => UP).
	 *
	 * @return The opposite Direction.
	 */
	fun getOpposite() = fromIndex(this.opposite)

	/**
	 * Rotate this Direction 90 degrees clockwise around the Y axis (NORTH => EAST => SOUTH => WEST => NORTH).
	 *
	 * @return The rotated Direction.
	 * @throws RuntimeException if the rotation is not defined for the current Direction.
	 */
	fun rotateY() = when (this) {
		NORTH -> EAST
		EAST -> SOUTH
		SOUTH -> WEST
		WEST -> NORTH
		else -> throw RuntimeException("Unable to get Y-rotated face of $this")
	}

	/**
	 * Rotate this Direction 90 degrees counter-clockwise around the Y axis (NORTH => WEST => SOUTH => EAST => NORTH).
	 *
	 * @return The counter-clockwise rotated Direction.
	 * @throws RuntimeException if the counter-clockwise rotation is not defined for the current Direction.
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
			entries.forEach {
				values[it.ordinal] = it
				if (it.axis.plane == Plane.HORIZONTAL) {
					horizontals[it.horizontalIndex] = it
				}
			}
		}

		/**
		 * Get the Direction enum value from the given index.
		 *
		 * @param index The index of the direction.
		 * @return The Direction enum value.
		 */
		fun fromIndex(index: Int) = this.values[abs(index % this.values.size)]

		/**
		 * Get the Direction enum value from the given horizontal index.
		 *
		 * @param index The horizontal index of the direction.
		 * @return The Direction enum value.
		 */
		fun fromHorizontalIndex(index: Int) = this.horizontals[abs(index % this.horizontals.size)]

		/**
		 * Get the Direction enum value from the given horizontal angle.
		 *
		 * @param angle The horizontal angle in degrees.
		 * @return The Direction enum value.
		 */
		fun fromHorizontalAngle(angle: Double) =
			this.fromHorizontalIndex(floor(angle / 90.0 + 0.5).toInt() and 3)

		/**
		 * Get the Direction enum value from the given axis direction and axis.
		 *
		 * @throws RuntimeException if the direction cannot be found.
		 */
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
