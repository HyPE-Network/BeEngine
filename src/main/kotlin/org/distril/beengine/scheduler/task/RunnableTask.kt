package org.distril.beengine.scheduler.task

class RunnableTask(private val runnable: Runnable) : Task() {

    override fun onRun() = this.runnable.run()
}
