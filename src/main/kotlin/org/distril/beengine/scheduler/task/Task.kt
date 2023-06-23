package org.distril.beengine.scheduler.task

abstract class Task {

	var cancelled = false
		private set

	abstract fun onRun()

	fun cancel() {
		this.cancelled = true

		this.onCancel()
	}

	protected fun onCancel() {
		// functional method
	}
}
