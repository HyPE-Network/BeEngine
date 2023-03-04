package org.distril.beengine.util;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class CompletableFutureArray {

	private final List<CompletableFuture<Void>> futures = new ArrayList<>();

	public void add(Runnable runnable) {
		this.add(CompletableFuture.runAsync(runnable));
	}

	public void add(CompletableFuture<Void> future) {
		this.futures.add(future);
	}

	@SuppressWarnings("unchecked")
	public CompletableFuture<Void> execute() {
		CompletableFuture<Void>[] array = (CompletableFuture<Void>[]) this.futures.toArray(new CompletableFuture[0]);
		return CompletableFuture.allOf(array);
	}
}
