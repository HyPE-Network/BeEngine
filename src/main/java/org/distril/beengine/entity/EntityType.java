package org.distril.beengine.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.distril.beengine.entity.impl.EntityHuman;

@AllArgsConstructor
public enum EntityType {

	HUMAN(EntityHuman.class, "minecraft:player");

	private final Class<? extends Entity> entityClass;
	@Getter
	private final String identifier;

	@SuppressWarnings("unchecked")
	public <T extends Entity> T createEntity() {
		try {
			return (T) this.entityClass.getDeclaredConstructor().newInstance();
		} catch (Exception exception) {
			throw new RuntimeException(exception);
		}
	}
}
