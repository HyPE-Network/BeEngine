package org.distril.beengine.scheduler.task

interface Task {

	fun onRun()

	fun onCancel() {
		// functional method
	}
}
