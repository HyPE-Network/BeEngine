package org.distril.beengine.material.item;

import com.nukkitx.nbt.NbtMap;
import com.nukkitx.nbt.NbtType;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.distril.beengine.material.Material;
import org.distril.beengine.material.block.Block;
import org.distril.beengine.material.block.BlockPalette;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

@Getter
@Setter
@ToString
public abstract class Item implements Cloneable, ItemBehaviors {

	private static final AtomicInteger NEXT_NETWORK_ID = new AtomicInteger(0);

	private final Material material;
	private final int networkId;
	private int meta;
	private int count = 1;
	private NbtMap nbt = NbtMap.EMPTY;

	public Item(Material material) {
		this.material = material;
		this.networkId = material == Material.AIR ? 0 : NEXT_NETWORK_ID.incrementAndGet();
	}

	public void setCount(int count) {
		this.count = Math.max(0, Math.min(count, this.getMaxCount()));
	}

	public void setNbt(NbtMap nbt) {
		this.nbt = nbt == null ? NbtMap.EMPTY : nbt;
	}

	public String getCustomName() {
		return this.nbt.getCompound("display").getString("Name");
	}

	public void setCustomName(String customName) {
		if (customName != null) {
			this.nbt = this.nbt.getCompound("display").toBuilder()
					.putString("Name", customName)
					.build();
		} else {
			var displayBuilder = this.nbt.getCompound("display").toBuilder();
			displayBuilder.remove("Name");
			this.nbt = displayBuilder.build();
		}
	}

	public List<String> getLores() {
		return this.nbt.getCompound("display").getList("Lore", NbtType.STRING);
	}

	public void setLores(String... lores) {
		if (lores == null || lores.length == 0) {
			var displayBuilder = this.nbt.getCompound("display").toBuilder();
			displayBuilder.remove("Lore");
			this.nbt = displayBuilder.build();
		} else {
			this.nbt = this.nbt.getCompound("display").toBuilder()
					.putList("Lore", NbtType.STRING, lores)
					.build();
		}
	}

	@SuppressWarnings("unchecked")
	public <T extends Block> T toBlock() {
		return (T) BlockPalette.getBlock(this);
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
		checkMeta = !checkMeta || (this.meta == that.getMeta());
		checkData = !checkData || this.nbt.equals(that.getNbt());
		return this.material == that.getMaterial() && checkMeta && checkData;
	}

	@Override
	public int hashCode() {
		return Objects.hash(this.material, this.meta, this.count, this.nbt);
	}

	@Override
	public Item clone() {
		try {
			Item clone = (Item) super.clone();
			clone.meta = this.meta;
			clone.count = this.count;
			clone.nbt = this.nbt;
			return clone;
		} catch (CloneNotSupportedException exception) {
			throw new AssertionError();
		}
	}

	public int getBlockRuntimeId() {
		return this.material.isBlock() ? this.toBlock().getState().getRuntimeId() : 0;
	}
}
