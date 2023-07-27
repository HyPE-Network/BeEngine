package org.distril.beengine.scheduler

import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.distril.beengine.scheduler.task.RunnableTask
import org.distril.beengine.scheduler.task.Task
import org.distril.beengine.scheduler.task.TaskEntry
import org.distril.beengine.util.Utils.getLogger
import java.util.concurrent.Executors
import java.util.concurrent.PriorityBlockingQueue
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicLong

class Scheduler(private val taskTimeout: Long) {

	private val executor = Executors.newCachedThreadPool()

	private val queue = PriorityBlockingQueue<TaskEntry>()

	private val lastUpdateTick = AtomicLong(0)

	fun processTick(currentTick: Long) {
		this.lastUpdateTick.set(currentTick)

		synchronized(this.queue) {
			while (this.queue.isNotEmpty() && this.queue.peek().nextRunTick <= currentTick) {
				val taskEntry = this.queue.poll()
				if (taskEntry.isAsync) {
					this.executor.submit { this.executeTask(taskEntry) }
				} else {
					this.executeTask(taskEntry)
				}
			}
		}
	}

	private fun executeTask(entry: TaskEntry) {
		if (!entry.isCancelled) {
			runBlocking {
				try {
					withTimeout(taskTimeout) { entry.task.onRun() }
				} catch (exception: TimeoutCancellationException) {
					log.error("Task timed out:", exception)
				} catch (exception: Exception) {
					log.error("Task failed with exception:", exception)
				}

				if (entry.isRepeating && !entry.isCancelled) addInQueue(entry, entry.period)
			}
		}
	}

	fun scheduleTask(delay: Int = 0, period: Int = 0, async: Boolean = false, runnable: Runnable) =
		this.scheduleTask(delay, period, async, RunnableTask(runnable))

	fun scheduleTask(delay: Int = 0, period: Int = 0, async: Boolean = false, task: Task) =
		this.addInQueue(TaskEntry(task, delay, period, async), delay)


	private fun addInQueue(entry: TaskEntry, adds: Int): TaskEntry {
		synchronized(this.queue) {
			entry.nextRunTick = this.lastUpdateTick.get() + adds
			this.queue.offer(entry)
		}

		return entry
	}

	fun cancelAllTasks() {
		synchronized(this.queue) {
			this.queue.forEach { it.cancel() }
			this.queue.clear()
		}

		this.executor.shutdown()
		try {
			if (!this.executor.awaitTermination(this.taskTimeout, TimeUnit.MILLISECONDS)) {
				this.executor.shutdownNow()
			}
		} catch (_: InterruptedException) {
			this.executor.shutdownNow()
		}
	}

	companion object {

		private val log = Scheduler.getLogger()
	}
}
