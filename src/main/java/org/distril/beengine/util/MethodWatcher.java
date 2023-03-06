package org.distril.beengine.util;

import lombok.extern.log4j.Log4j2;

import java.util.function.Supplier;

@Log4j2
public class MethodWatcher {

	public static <T> T watch(Supplier<T> supplier, String testName) {
		var startTime = System.currentTimeMillis();
		var result = supplier.get();
		var endTime = System.currentTimeMillis();

		log.info("{} took {} ms!", testName, endTime - startTime);

		return result;
	}
}
