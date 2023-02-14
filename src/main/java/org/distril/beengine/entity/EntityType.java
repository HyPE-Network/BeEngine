package org.distril.beengine.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum EntityType {

	HUMAN("minecraft:player");

	private final String identifier;
}
