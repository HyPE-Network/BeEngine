package org.distril.beengine.material.item;

import com.nukkitx.nbt.NbtMap;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.distril.beengine.material.Material;
import org.distril.beengine.material.item.behavior.Behavior;
import org.distril.beengine.material.item.behavior.NoopBehavior;

import java.util.concurrent.atomic.AtomicInteger;

@Setter
@Accessors(chain = true, fluent = true)
public class ItemBuilder {

	private static final NoopBehavior NOOP_BEHAVIOR = new NoopBehavior();

	private static final AtomicInteger NEXT_NETWORK_ID = new AtomicInteger(0);

	private final Material material;
	private int networkId;
	private int meta;
	private int count = 1;
	private NbtMap nbt = NbtMap.EMPTY;
	private long blockingTicks;
	private int blockRuntimeId;

	private Behavior behavior = NOOP_BEHAVIOR;

	private ItemBuilder(Material material) {
		this.material = material;
		this.networkId = material != Material.AIR ? NEXT_NETWORK_ID.incrementAndGet() : 0;
	}

	public static ItemBuilder builder() {
		return new ItemBuilder(Material.AIR);
	}

	public static ItemBuilder builder(Material material) {
		return new ItemBuilder(material);
	}

	public static ItemBuilder builder(Item item) {
		return new ItemBuilder(item.getMaterial())
				.meta(item.getMeta())
				.count(item.getCount())
				.nbt(item.getNbt())
				.blockingTicks(item.getBlockingTicks())
				.blockRuntimeId(item.getBlockRuntimeId())
				.behavior(item.getBehavior());
	}

	public ItemBuilder count(int count) {
		this.count = Math.max(0, Math.min(count, this.behavior.getMaxStackSize()));

		return this;
	}

	public ItemBuilder nbt(NbtMap nbt) {
		this.nbt = nbt == null ? NbtMap.EMPTY : nbt;

		return this;
	}

	public Item build() {
		return new Item(this.material, this.meta, this.count, this.nbt, this.blockingTicks, this.blockRuntimeId,
				this.networkId, this.behavior);
	}
}
