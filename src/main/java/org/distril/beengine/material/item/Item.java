package org.distril.beengine.material.item;

import com.nukkitx.nbt.NbtMap;
import com.nukkitx.nbt.NbtType;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.distril.beengine.material.Material;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

@Getter
@Setter
@ToString
public abstract class Item implements Cloneable, Behavior {

	public static final Item AIR = Material.AIR.getItem();

	private static final AtomicInteger NEXT_NETWORK_ID = new AtomicInteger(0);

	private final Material material;
	private int meta;
	private int count = 1;
	private NbtMap nbt = NbtMap.EMPTY;
	private long blockingTicks;
	private int blockRuntimeId;
	private int networkId;

	public Item(Material material) {
		this(material, 0);
	}

	public Item(Material material, int meta) {
		this.material = material;
		this.meta = meta;
		this.networkId = material != Material.AIR ? NEXT_NETWORK_ID.incrementAndGet() : 0;
	}

	public void setCount(int count) {
		this.count = Math.max(count, 0);
	}

	public void setNbt(NbtMap nbt) {
		this.nbt = nbt == null ? NbtMap.EMPTY : nbt;
	}

	public String getCustomName() {
		return this.nbt.getCompound("display").getString("Name");
	}

	public void setCustomName(String customName) {
		NbtMap displayNbt;
		if (customName != null) {
			displayNbt = this.nbt.getCompound("display").toBuilder()
					.putString("Name", customName)
					.build();
		} else {
			var displayBuilder = this.nbt.getCompound("display").toBuilder();
			displayBuilder.remove("Name");
			displayNbt = displayBuilder.build();
		}

		this.nbt = this.nbt.toBuilder().putCompound("display", displayNbt).build();
	}

	public List<String> getLores() {
		return this.nbt.getCompound("display").getList("Lore", NbtType.STRING);
	}

	public void setLores(String... lores) {
		if (lores == null || lores.length == 0) {
			return;
		}

		var displayNbt = this.nbt.getCompound("display").toBuilder()
				.putList("Lore", NbtType.STRING, lores)
				.build();

		this.nbt = this.nbt.toBuilder().putCompound("display", displayNbt).build();
	}

	public int getMaxStackSize() {
		return 64;
	}

	public String getIdentifier() {
		return this.material.getIdentifier();
	}

	public int getRuntimeId() {
		return this.material.getItemRuntimeId();
	}

	public String getName() {
		return this.getClass().getSimpleName();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}

		if (obj == null || this.getClass() != obj.getClass()) {
			return false;
		}

		return this.equals((Item) obj, true, true, true);
	}

	public boolean equals(Item that, boolean checkMeta, boolean checkCount, boolean checkData) {
		checkMeta = !checkMeta || (this.meta == that.getMeta() && this.blockRuntimeId == that.getBlockRuntimeId());
		checkCount = !checkCount || this.count == that.getCount();
		checkData = !checkData || Objects.equals(this.nbt, that.getNbt());
		return this.material == that.getMaterial() && checkMeta && checkCount && checkData;
	}

	@Override
	public int hashCode() {
		return Objects.hash(this.material, this.meta, this.count, this.nbt, this.blockingTicks, this.blockRuntimeId);
	}

	@SuppressWarnings("MethodDoesntCallSuperMethod")
	@Override
	public Item clone() {
		Item clone = this.material.getItem();
		clone.setMeta(this.meta);
		clone.setCount(this.count);
		clone.setNbt(this.nbt);
		clone.setBlockingTicks(this.blockingTicks);
		clone.setBlockRuntimeId(this.blockRuntimeId);
		return clone;
	}
}
