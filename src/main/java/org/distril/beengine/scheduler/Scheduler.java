package org.distril.beengine.scheduler;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.distril.beengine.scheduler.task.RunnableTask;
import org.distril.beengine.scheduler.task.Task;
import org.distril.beengine.scheduler.task.TaskEntry;

import java.util.Comparator;
import java.util.PriorityQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Scheduler {

	private static final Comparator<TaskEntry> COMPARATOR = Comparator.comparing(TaskEntry::getNextRunTick).reversed();
	private static final ExecutorService POOL = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

	private final PriorityQueue<TaskEntry> queue = new PriorityQueue<>(COMPARATOR);

	private long lastUpdateTick;

	public TaskBuilder prepareTask(Runnable runnable) {
		return new TaskBuilder(this, new RunnableTask(runnable));
	}


	public TaskBuilder prepareTask(Task task) {
		return new TaskBuilder(this, task);
	}

	public void processTick(long currentTick) {
		this.lastUpdateTick = currentTick;
		while (!this.queue.isEmpty() && this.queue.peek().getNextRunTick() <= lastUpdateTick) {
			var taskEntry = this.queue.poll();

			if (taskEntry.isCancelled()) {
				continue;
			}

			Task task = taskEntry.getTask();
			if (taskEntry.isAsync()) {
				POOL.submit(task::onRun);
			} else {
				task.onRun();
			}

			if (!taskEntry.isCancelled() && taskEntry.isRepeating()) {
				this.addInQueue(taskEntry);
			}
		}
	}

	protected void addInQueue(TaskEntry entry) {
		if (entry.isRepeating()) {
			entry.setNextRunTick(this.lastUpdateTick + entry.getPeriod());
		} else {
			entry.setNextRunTick(this.lastUpdateTick + entry.getDelay());
		}

		this.queue.add(entry);
	}

	@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
	public static class TaskBuilder {

		private final Scheduler scheduler;
		private final Task task;

		private int delay, period;
		private boolean async;

		public TaskBuilder delay(int delay) {
			this.delay = delay;
			return this;
		}

		public TaskBuilder repeating(int period) {
			this.period = period;
			return this;
		}

		public TaskBuilder async() {
			this.async = true;
			return this;
		}

		public Task schedule() {
			this.scheduler.addInQueue(new TaskEntry(this.task, this.delay, this.period, this.async));

			return this.task;
		}
	}
}
