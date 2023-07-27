package org.distril.beengine.scheduler.task

class TaskEntry(
	val task: Task,
	val delay: Int,
	val period: Int,
	val isAsync: Boolean,
	var nextRunTick: Long = 0
) : Comparable<TaskEntry> {

	var isCancelled = false
		private set

	val isRepeating = this.period > 0

	fun cancel() {
		this.isCancelled = true

		this.task.onCancel()
	}

	override fun compareTo(other: TaskEntry) = this.nextRunTick.compareTo(other.nextRunTick)
}
