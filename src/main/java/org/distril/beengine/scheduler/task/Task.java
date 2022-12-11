package org.distril.beengine.scheduler.task;

import lombok.Getter;

public abstract class Task {

	@Getter
	private boolean cancelled;

	public abstract void onRun();

	public void onCancel() {/**/}

	public void cancel() {
		this.cancelled = true;
		this.onCancel();
	}
}
