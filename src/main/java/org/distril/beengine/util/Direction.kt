package org.distril.beengine.util;

/*
 * Copyright (C) 2023, Distril
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/lgpl-3.0.txt>.
 *
 * This program includes code from Cloudburst/API, which is licensed under the GNU Lesser General Public License v3.0.
 * A copy of the license can be found in the project or at <https://www.gnu.org/licenses/lgpl-3.0.txt>.
 */

import com.nukkitx.math.GenericMath;
import com.nukkitx.math.vector.Vector3i;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;
import java.util.Iterator;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Predicate;

@RequiredArgsConstructor
public enum Direction {

	DOWN(1, -1, "down", AxisDirection.NEGATIVE, Vector3i.from(0, -1, 0)),
	UP(0, -1, "up", AxisDirection.POSITIVE, Vector3i.from(0, 1, 0)),
	NORTH(3, 2, "north", AxisDirection.NEGATIVE, Vector3i.from(0, 0, -1)),
	SOUTH(2, 0, "south", AxisDirection.POSITIVE, Vector3i.from(0, 0, 1)),
	WEST(5, 1, "west", AxisDirection.NEGATIVE, Vector3i.from(-1, 0, 0)),
	EAST(4, 3, "east", AxisDirection.POSITIVE, Vector3i.from(1, 0, 0));

	/**
	 * All faces in D-U-N-S-W-E order
	 */
	private static final Direction[] VALUES = new Direction[6];

	/**
	 * All faces with horizontal axis in order S-W-N-E
	 */
	private static final Direction[] HORIZONTALS = new Direction[4];

	static {
		// Circular dependency
		DOWN.axis = Axis.Y;
		UP.axis = Axis.Y;
		NORTH.axis = Axis.Z;
		SOUTH.axis = Axis.Z;
		WEST.axis = Axis.X;
		EAST.axis = Axis.X;

		for (Direction face : Direction.values()) {
			VALUES[face.ordinal()] = face;

			if (face.getAxis().isHorizontal()) {
				HORIZONTALS[face.horizontalIndex] = face;
			}
		}
	}

	/**
	 * Index of the opposite BlockFace in the VALUES array
	 */
	private final int opposite;

	/**
	 * Ordering index for the HORIZONTALS field (S-W-N-E)
	 */
	private final int horizontalIndex;

	/**
	 * The name of this BlockFace (up, down, north, etc.)
	 */
	private final String name;
	private final AxisDirection axisDirection;

	/**
	 * Normalized vector that points in the direction of this BlockFace
	 */
	private final Vector3i unitVector;
	private Axis axis;

	/**
	 * Get a BlockFace by its index (0-5). The order is D-U-N-S-W-E
	 *
	 * @param index BlockFace index
	 *
	 * @return block face
	 */
	public static Direction fromIndex(int index) {
		return VALUES[Math.abs(index % VALUES.length)];
	}

	/**
	 * Get a BlockFace by its horizontal index (0-3). The order is S-W-N-E
	 *
	 * @param index BlockFace index
	 *
	 * @return block face
	 */
	public static Direction fromHorizontalIndex(int index) {
		return HORIZONTALS[Math.abs(index % HORIZONTALS.length)];
	}

	/**
	 * Get the BlockFace corresponding to the given angle (0-360). An angle of 0 is SOUTH, an angle of 90 would be WEST
	 *
	 * @param angle horizontal angle
	 *
	 * @return block face
	 */
	public static Direction fromHorizontalAngle(double angle) {
		return Direction.fromHorizontalIndex(GenericMath.floor(angle / 90.0D + 0.5D) & 3);
	}

	public static Direction fromAxis(AxisDirection axisDirection, Axis axis) {
		for (Direction face : VALUES) {
			if (face.getAxisDirection() == axisDirection && face.getAxis() == axis) {
				return face;
			}
		}

		throw new RuntimeException("Unable to get face from axis: " + axisDirection + " " + axis);
	}

	/**
	 * Choose a random BlockFace using the given Random
	 *
	 * @param random random number generator
	 *
	 * @return block face
	 */
	public static Direction random(Random random) {
		return VALUES[random.nextInt(VALUES.length)];
	}

	/**
	 * Get the index of this BlockFace (0-5). The order is D-U-N-S-W-E
	 *
	 * @return index
	 */
	public int getIndex() {
		return this.ordinal();
	}

	/**
	 * Get the horizontal index of this BlockFace (0-3). The order is S-W-N-E
	 *
	 * @return horizontal index
	 */
	public int getHorizontalIndex() {
		return this.horizontalIndex;
	}

	/**
	 * Get the angle of this BlockFace (0-360)
	 *
	 * @return horizontal angle
	 */
	public float getHorizontalAngle() {
		return (float) ((this.horizontalIndex & 3) * 90);
	}

	/**
	 * Get the name of this BlockFace (up, down, north, etc.)
	 *
	 * @return name
	 */
	public String getName() {
		return this.name;
	}

	/**
	 * Get the Axis of this BlockFace
	 *
	 * @return axis
	 */
	public Axis getAxis() {
		return this.axis;
	}

	/**
	 * Get the AxisDirection of this BlockFace
	 *
	 * @return axis direction
	 */
	public AxisDirection getAxisDirection() {
		return this.axisDirection;
	}

	/**
	 * Get the unit vector of this BlockFace
	 *
	 * @return vector
	 */
	public Vector3i getUnitVector() {
		return this.unitVector;
	}

	public Vector3i getOffset(Vector3i position) {
		return position.add(this.unitVector);
	}

	public Vector3i getOffset(Vector3i position, int step) {
		return position.add(this.unitVector.getX() * step, this.unitVector.getY() * step, this.unitVector.getZ() * step);
	}

	/**
	 * Returns an offset that addresses the block in front of this BlockFace
	 *
	 * @return x offset
	 */
	public int getXOffset() {
		return this.axis == Axis.X ? this.axisDirection.getOffset() : 0;
	}

	/**
	 * Returns an offset that addresses the block in front of this BlockFace
	 *
	 * @return y offset
	 */
	public int getYOffset() {
		return this.axis == Axis.Y ? this.axisDirection.getOffset() : 0;
	}

	/**
	 * Returns an offset that addresses the block in front of this BlockFace
	 *
	 * @return x offset
	 */
	public int getZOffset() {
		return this.axis == Axis.Z ? this.axisDirection.getOffset() : 0;
	}

	/**
	 * Get the opposite BlockFace (e.g. DOWN ==&gt; UP)
	 *
	 * @return block face
	 */
	public Direction getOpposite() {
		return Direction.fromIndex(this.opposite);
	}

	/**
	 * Rotate this BlockFace around the Y axis clockwise (NORTH =&gt; EAST =&gt; SOUTH =&gt; WEST =&gt; BB_NORTH)
	 *
	 * @return block face
	 */
	public Direction rotateY() {
		return switch (this) {
			case NORTH -> EAST;
			case EAST -> SOUTH;
			case SOUTH -> WEST;
			case WEST -> NORTH;
			default -> throw new RuntimeException("Unable to get Y-rotated face of " + this);
		};
	}

	/**
	 * Rotate this BlockFace around the Y axis counter-clockwise (NORTH =&gt; WEST =&gt; SOUTH =&gt; EAST =&gt; BB_NORTH)
	 *
	 * @return block face
	 */
	public Direction rotateYCCW() {
		return switch (this) {
			case NORTH -> WEST;
			case EAST -> NORTH;
			case SOUTH -> EAST;
			case WEST -> SOUTH;
			default -> throw new RuntimeException("Unable to get counter-clockwise Y-rotated face of " + this);
		};
	}

	@Override
	public String toString() {
		return this.name;
	}

	@Getter
	@RequiredArgsConstructor
	public enum Axis implements Predicate<Direction> {

		X("x"),
		Y("y"),
		Z("z");

		static {
			// Circular dependency
			X.plane = Plane.HORIZONTAL;
			Y.plane = Plane.VERTICAL;
			Z.plane = Plane.HORIZONTAL;
		}

		private final String name;
		private Plane plane;

		public boolean isVertical() {
			return this.plane == Plane.VERTICAL;
		}

		public boolean isHorizontal() {
			return this.plane == Plane.HORIZONTAL;
		}

		public boolean test(Direction direction) {
			return direction != null && direction.getAxis() == this;
		}

		@Override
		public String toString() {
			return this.name;
		}
	}

	@Getter
	@RequiredArgsConstructor
	public enum AxisDirection {

		POSITIVE(1, "Towards positive"),
		NEGATIVE(-1, "Towards negative");

		private final int offset;
		private final String description;

		@Override
		public String toString() {
			return this.description;
		}
	}

	public enum Plane implements Predicate<Direction>, Iterable<Direction> {

		HORIZONTAL,
		VERTICAL;

		static {
			// Circular dependency
			HORIZONTAL.faces = new Direction[]{NORTH, EAST, SOUTH, WEST};
			VERTICAL.faces = new Direction[]{UP, DOWN};
		}

		private Direction[] faces;

		public Direction random() {
			return this.faces[ThreadLocalRandom.current().nextInt(this.faces.length)];
		}

		public Direction random(Random random) {
			return this.faces[random.nextInt(this.faces.length)];
		}

		public boolean test(Direction direction) {
			return direction != null && direction.getAxis().getPlane() == this;
		}

		@SuppressWarnings("NullableProblems")
		@Override
		public Iterator<Direction> iterator() {
			return Arrays.stream(this.faces).iterator();
		}
	}
}
