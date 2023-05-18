package org.distril.beengine.scheduler

import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.distril.beengine.scheduler.task.RunnableTask
import org.distril.beengine.scheduler.task.Task
import org.distril.beengine.scheduler.task.TaskEntry
import org.distril.beengine.util.Utils.getLogger
import java.util.*
import java.util.concurrent.ForkJoinPool
import java.util.concurrent.TimeUnit

class Scheduler(private val taskTimeout: Long) {

	private val queue: Queue<TaskEntry> = ArrayDeque()

	private var lastUpdateTick: Long = 0

	fun scheduleTask(delay: Int = 0, period: Int = 0, async: Boolean = false, runnable: Runnable) =
		this.scheduleTask(delay, period, async, RunnableTask(runnable))

	fun scheduleTask(delay: Int = 0, period: Int = 0, async: Boolean = false, task: Task) =
		this.addInQueue(TaskEntry(task, delay, period, async))

	fun processTick(currentTick: Long) {
		this.lastUpdateTick = currentTick
		synchronized(this.queue) {
			while (!this.queue.isEmpty() && this.queue.peek().run { this != null && this.nextRunTick <= currentTick }) {
				val taskEntry = this.queue.poll()
				val task = taskEntry.task
				if (taskEntry.async) {
					POOL.submit {
						Thread.currentThread().apply { name = "BeEngine Scheduler Task #" + this.id }

						runBlocking {
							try {
								withTimeout(taskTimeout) { task.onRun() }
							} catch (exception: TimeoutCancellationException) {
								log.error("Scheduler Task timed out:", exception)
							} catch (exception: Exception) {
								log.error("Scheduler Task failed with exception:", exception)
							}
						}

						if (!task.cancelled && taskEntry.isRepeating) {
							this.addInQueue(taskEntry)
						}
					}
				} else {
					task.onRun()

					if (!task.cancelled && taskEntry.isRepeating) {
						this.addInQueue(taskEntry)
					}
				}
			}
		}
	}

	private fun addInQueue(entry: TaskEntry) {
		if (entry.isRepeating) {
			entry.nextRunTick = this.lastUpdateTick + entry.period
		} else {
			entry.nextRunTick = this.lastUpdateTick + entry.delay
		}

		synchronized(this.queue) { this.queue.offer(entry) }
	}

	fun cancelAllTasks() {
		synchronized(this.queue) {
			this.queue.forEach { it.task.cancel() }
			this.queue.clear()
		}

		POOL.shutdown()
		try {
			if (!POOL.awaitTermination(this.taskTimeout, TimeUnit.MILLISECONDS)) POOL.shutdownNow()
		} catch (_: InterruptedException) {
			POOL.shutdownNow()
		}
	}

	companion object {

		private val log = Scheduler.getLogger()

		private val POOL = ForkJoinPool.commonPool()
	}
}
