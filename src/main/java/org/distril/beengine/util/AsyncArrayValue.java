package org.distril.beengine.util;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class AsyncArrayValue<T> {

	private final List<CompletableFuture<T>> futures = new ArrayList<>();

	public void add(CompletableFuture<T> future) {
		this.futures.add(future);
	}

	@SuppressWarnings("unchecked")
	public CompletableFuture<Void> execute() {
		CompletableFuture<T>[] array = (CompletableFuture<T>[]) this.futures.toArray(new CompletableFuture[0]);
		return CompletableFuture.allOf(array);
	}
}
