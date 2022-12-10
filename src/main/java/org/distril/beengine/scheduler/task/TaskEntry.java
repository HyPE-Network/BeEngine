package org.distril.beengine.scheduler.task;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Getter
@RequiredArgsConstructor
public class TaskEntry {

	private final Task task;
	private final int delay, period;
	private final boolean async;
	@Setter
	private long nextRunTick;

	public boolean isCancelled() {
		return this.task.isCancelled();
	}

	public boolean isRepeating() {
		return this.period > 0;
	}
}
