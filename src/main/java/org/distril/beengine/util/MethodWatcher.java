package org.distril.beengine.util;

import lombok.extern.log4j.Log4j2;

@Log4j2
public class MethodWatcher {

	public static void watch(Runnable runnable, String testName) {
		long startTime = System.currentTimeMillis();
		runnable.run();
		long endTime = System.currentTimeMillis();

		log.info(testName + " took " + (endTime - startTime) + " ms!");
	}
}
