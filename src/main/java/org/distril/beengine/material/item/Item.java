package org.distril.beengine.material.item;

import com.nukkitx.nbt.NbtMap;
import com.nukkitx.nbt.NbtType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;
import org.distril.beengine.material.Material;
import org.distril.beengine.material.item.behavior.Behavior;

import java.util.List;
import java.util.Objects;

@Getter
@ToString
@AllArgsConstructor
public class Item {

	public static final Item AIR = ItemBuilder.builder().build();

	private final Material material;
	private final int meta;
	private final int count;
	private final NbtMap nbt;
	private final long blockingTicks;
	private final int blockRuntimeId;
	private final int networkId;

	private final Behavior behavior;

	public ItemBuilder toBuilder() {
		return ItemBuilder.builder(this);
	}

	public Item decrementCount(int count) {
		return this.toBuilder().count(this.count - count).build();
	}

	public Item incrementCount(int count) {
		return this.toBuilder().count(this.count + count).build();
	}

	public String getCustomName() {
		return this.nbt.getCompound("display").getString("Name");
	}

	public Item setCustomName(String customName) {
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

		return this.toBuilder().nbt(this.nbt.toBuilder().putCompound("display", displayNbt).build()).build();
	}

	public List<String> getLores() {
		return this.nbt.getCompound("display").getList("Lore", NbtType.STRING);
	}

	public Item setLores(String... lores) {
		if (lores == null || lores.length == 0) {
			return this;
		}

		var displayNbt = this.nbt.getCompound("display").toBuilder()
				.putList("Lore", NbtType.STRING, lores)
				.build();

		return this.toBuilder().nbt(this.nbt.toBuilder().putCompound("display", displayNbt).build()).build();
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

		return this.equals((Item) obj, true, true);
	}

	public boolean equals(Item that, boolean checkMeta, boolean checkData) {
		checkMeta = !checkMeta || (this.meta == that.getMeta() && this.blockRuntimeId == that.getBlockRuntimeId());
		checkData = !checkData || Objects.equals(this.nbt, that.getNbt());
		return this.material == that.getMaterial() && checkMeta && checkData;
	}

	@Override
	public int hashCode() {
		return Objects.hash(this.material, this.meta, this.count, this.nbt, this.blockingTicks, this.blockRuntimeId);
	}
}
