package org.distril.beengine.entity.data

import com.nukkitx.math.vector.Vector3f
import com.nukkitx.math.vector.Vector3i
import com.nukkitx.nbt.NbtMap
import com.nukkitx.protocol.bedrock.data.entity.EntityData
import com.nukkitx.protocol.bedrock.data.entity.EntityDataMap
import com.nukkitx.protocol.bedrock.data.entity.EntityFlag
import com.nukkitx.protocol.bedrock.data.entity.EntityFlags

class EntityMetadata(private val listener: Listener) {

    val data = EntityDataMap()
    val flags = EntityFlags()

    private val dataChangeSet = EntityDataMap()

    fun update() {
        if (!this.dataChangeSet.isEmpty()) {
            this.listener.onDataChange(this.dataChangeSet)
            this.dataChangeSet.clear()
        }
    }

    operator fun contains(data: EntityData) = this.data.containsKey(data)

    fun getType(data: EntityData) = this.data.getType(data)

    fun getInt(data: EntityData) = this.data.getInt(data)

    fun setInt(data: EntityData, value: Int) {
        val oldValue = this.getInt(data)
        if (oldValue != value) {
            this.data.putInt(data, value)
            this.dataChangeSet.putInt(data, value)
        }
    }

    fun getShort(data: EntityData) = this.data.getShort(data)

    fun setShort(data: EntityData, value: Int) {
        var value = value
        value = value.toShort().toInt()
        val oldValue = this.getShort(data)
        if (oldValue.toInt() != value) {
            this.data.putShort(data, value)
            this.dataChangeSet.putShort(data, value)
        }
    }

    fun getByte(data: EntityData) = this.data.getByte(data)

    fun setByte(data: EntityData, value: Int) {
        var value = value
        value = value.toByte().toInt()
        val oldValue = this.getByte(data)
        if (oldValue.toInt() != value) {
            this.data.putByte(data, value)
            dataChangeSet.putByte(data, value)
        }
    }

    fun getBoolean(data: EntityData) = this.getByte(data).toInt() != 0

    fun setBoolean(data: EntityData, value: Boolean) {
        val oldValue = this.getBoolean(data)
        if (oldValue != value) {
            this.data.putByte(data, if (value) 1 else 0)
            this.dataChangeSet.putByte(data, if (value) 1 else 0)
        }
    }

    fun getLong(data: EntityData) = this.data.getLong(data)

    fun setLong(data: EntityData, value: Long) {
        val oldValue = this.getLong(data)
        if (oldValue != value) {
            this.data.putLong(data, value)
            this.dataChangeSet.putLong(data, value)
        }
    }

    fun getString(data: EntityData) = this.data.getString(data)!!

    fun setString(data: EntityData, value: String) {
        val oldValue = this.getString(data)
        if (value != oldValue) {
            this.data.putString(data, value)
            this.dataChangeSet.putString(data, value)
        }
    }

    fun getFloat(data: EntityData) = this.data.getFloat(data)

    fun setFloat(data: EntityData, value: Float) {
        val oldValue = this.getFloat(data)
        if (oldValue != value) {
            this.data.putFloat(data, value)
            this.dataChangeSet.putFloat(data, value)
        }
    }

    fun getTag(data: EntityData) = this.data.getTag(data)!!

    fun setTag(data: EntityData, value: NbtMap) {
        val oldValue = this.getTag(data)
        if (value != oldValue) {
            this.data.putTag(data, value)
            this.dataChangeSet.putTag(data, value)
        }
    }

    fun getVector3i(data: EntityData) = this.data.getPos(data)!!

    fun setVector3i(data: EntityData, value: Vector3i) {
        val oldValue = this.getVector3i(data)
        if (value != oldValue) {
            this.data.putPos(data, value)
            this.dataChangeSet.putPos(data, value)
        }
    }

    fun getVector3f(data: EntityData) = this.data.getVector3f(data)!!

    fun setVector3f(data: EntityData, value: Vector3f) {
        val oldValue = this.getVector3f(data)
        if (value != oldValue) {
            this.data.putVector3f(data, value)
            this.dataChangeSet.putVector3f(data, value)
        }
    }

    operator fun get(data: EntityData): Any? = this.data.ensureAndGet(data)

    fun getFlag(flag: EntityFlag) = this.flags.getFlag(flag)

    fun setFlag(flag: EntityFlag, value: Boolean) {
        val oldValue = this.getFlag(flag)
        if (value != oldValue) {
            this.flags.setFlag(flag, value)
            this.dataChangeSet.putFlags(this.flags)
        }
    }

    interface Listener {

        fun onDataChange(dataMap: EntityDataMap)
    }
}
