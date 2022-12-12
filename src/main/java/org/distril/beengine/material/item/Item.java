package org.distril.beengine.material.item;

import com.nukkitx.nbt.NbtMap;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;
import lombok.extern.log4j.Log4j2;
import org.distril.beengine.material.Material;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@ToString
@Getter
@Setter
@Log4j2
public abstract class Item implements Cloneable, Behavior {

	private final List<String> lores = new ArrayList<>();

	@Accessors(chain = true)
	private Material material;

	@Accessors(chain = true)
	private int count = 1, meta;

	@Setter
	@Getter
	private String customName;
	private NbtMap nbt = NbtMap.EMPTY;

	public Item(Material material) {
		this(material, 0);
	}

	public Item(Material material, int meta) {
		this.material = material;
		this.meta = meta;
	}

	public Item setCount(int count) {
		this.count = Math.max(count, 0);

		return this;
	}

	public int getMaxStackSize() {
		return 64;
	}

	public List<String> getLores() {
		return Collections.unmodifiableList(this.lores);
	}

	public void setLores(String... lores) {
		this.lores.clear();
		this.lores.addAll(List.of(lores));
	}

	public int getRuntimeId() {
		return this.material.getItemRuntimeId();
	}

	public String getName() {
		return this.getClass().getSimpleName();
	}
}
