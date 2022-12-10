package org.distril.beengine.scheduler.task;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class RunnableTask extends Task {

	private final Runnable runnable;

	@Override
	public void onRun() {
		this.runnable.run();
	}
}
