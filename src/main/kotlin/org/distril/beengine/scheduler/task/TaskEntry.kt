package org.distril.beengine.scheduler.task

class TaskEntry(
	val task: Task,
	val delay: Int,
	val period: Int,
	val async: Boolean,
	var nextRunTick: Long = 0
) {

	val isCancelled get() = this.task.cancelled

	val isRepeating = this.period > 0
}
