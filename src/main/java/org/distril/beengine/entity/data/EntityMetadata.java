package org.distril.beengine.entity.data;

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
 * This program includes code from Cloudburst/Server, which is licensed under the GNU Lesser General Public License v3.0.
 * A copy of the license can be found in the project or at <https://www.gnu.org/licenses/lgpl-3.0.txt>.
 */

import com.nukkitx.math.vector.Vector3f;
import com.nukkitx.math.vector.Vector3i;
import com.nukkitx.nbt.NbtMap;
import com.nukkitx.protocol.bedrock.data.entity.EntityData;
import com.nukkitx.protocol.bedrock.data.entity.EntityDataMap;
import com.nukkitx.protocol.bedrock.data.entity.EntityFlag;
import com.nukkitx.protocol.bedrock.data.entity.EntityFlags;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class EntityMetadata {

	@Getter
	private final EntityDataMap data = new EntityDataMap();
	@Getter
	private final EntityFlags flags = new EntityFlags();

	private final EntityDataMap dataChangeSet = new EntityDataMap();

	private final Listener listener;

	public void update() {
		if (!this.dataChangeSet.isEmpty()) {
			this.listener.onDataChange(this.dataChangeSet);
			this.dataChangeSet.clear();
		}
	}

	public void putAllIn(EntityDataMap map) {
		map.putAll(this.data);
	}

	public void putFlagsIn(EntityDataMap map) {
		map.putFlags(this.flags);
	}

	public boolean contains(EntityData data) {
		return this.data.containsKey(data);
	}

	public EntityData.Type getType(EntityData data) {
		return this.data.getType(data);
	}

	public int getInt(EntityData data) {
		return this.data.getInt(data);
	}

	public void setInt(EntityData data, int value) {
		var oldValue = this.getInt(data);
		if (oldValue != value) {
			this.data.putInt(data, value);
			this.dataChangeSet.putInt(data, value);
		}
	}

	public short getShort(EntityData data) {
		return this.data.getShort(data);
	}

	public void setShort(EntityData data, int value) {
		value = (short) value;
		var oldValue = this.getShort(data);
		if (oldValue != value) {
			this.data.putShort(data, value);
			this.dataChangeSet.putShort(data, value);
		}
	}

	public byte getByte(EntityData data) {
		return this.data.getByte(data);
	}

	public void setByte(EntityData data, int value) {
		value = (byte) value;
		var oldValue = this.getByte(data);
		if (oldValue != value) {
			this.data.putByte(data, value);
			this.dataChangeSet.putByte(data, value);
		}
	}

	public boolean getBoolean(EntityData data) {
		return this.getByte(data) != 0;
	}

	public void setBoolean(EntityData data, boolean value) {
		var oldValue = this.getBoolean(data);
		if (oldValue != value) {
			this.data.putByte(data, value ? 1 : 0);
			this.dataChangeSet.putByte(data, value ? 1 : 0);
		}
	}

	public long getLong(EntityData data) {
		return this.data.getLong(data);
	}

	public void setLong(EntityData data, long value) {
		var oldValue = this.getLong(data);
		if (oldValue != value) {
			this.data.putLong(data, value);
			this.dataChangeSet.putLong(data, value);
		}
	}

	public String getString(EntityData data) {
		return this.data.getString(data);
	}

	public void setString(EntityData data, String value) {
		if (value == null) {
			value = "";
		}

		var oldValue = this.getString(data);
		if (!value.equals(oldValue)) {
			this.data.putString(data, value);
			this.dataChangeSet.putString(data, value);
		}
	}

	public float getFloat(EntityData data) {
		return this.data.getFloat(data);
	}

	public void setFloat(EntityData data, float value) {
		var oldValue = this.getFloat(data);
		if (oldValue != value) {
			this.data.putFloat(data, value);
			this.dataChangeSet.putFloat(data, value);
		}
	}

	public NbtMap getTag(EntityData data) {
		return this.data.getTag(data);
	}

	public void setTag(EntityData data, NbtMap value) {
		if (value == null) {
			value = NbtMap.EMPTY;
		}

		var oldValue = this.getTag(data);
		if (!value.equals(oldValue)) {
			this.data.putTag(data, value);
			this.dataChangeSet.putTag(data, value);
		}
	}

	public Vector3i getVector3i(EntityData data) {
		return this.data.getPos(data);
	}

	public void setVector3i(EntityData data, Vector3i value) {
		if (value == null) {
			value = Vector3i.ZERO;
		}

		var oldValue = this.data.getPos(data);
		if (!value.equals(oldValue)) {
			this.data.putPos(data, value);
			this.dataChangeSet.putPos(data, value);
		}
	}

	public Vector3f getVector3f(EntityData data) {
		return this.data.getVector3f(data);
	}

	public void setVector3f(EntityData data, Vector3f value) {
		if (value == null) {
			value = Vector3f.ZERO;
		}

		var oldValue = this.data.getVector3f(data);
		if (!value.equals(oldValue)) {
			this.data.putVector3f(data, value);
			this.dataChangeSet.putVector3f(data, value);
		}
	}

	public Object get(EntityData data) {
		return this.data.ensureAndGet(data);
	}

	public boolean getFlag(EntityFlag flag) {
		return this.flags.getFlag(flag);
	}

	public void setFlag(EntityFlag flag, boolean value) {
		var oldValue = this.flags.getFlag(flag);
		if (value != oldValue) {
			this.flags.setFlag(flag, value);
			this.dataChangeSet.putFlags(this.flags);
		}
	}

	public interface Listener {

		void onDataChange(EntityDataMap dataMap);
	}
}
