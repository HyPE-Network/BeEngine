package org.distril.beengine.util

import kotlinx.coroutines.*
import org.apache.logging.log4j.Logger
import org.distril.beengine.util.Utils.getLogger
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

class ModuleScope(
	private val name: String,
	parentContext: CoroutineContext = EmptyCoroutineContext,
	dispatcher: CoroutineDispatcher = Dispatchers.Default,
	exceptionHandler: (CoroutineContext, Throwable, Logger, String) -> Unit = { _, throwable, log, _ ->
		log.error("Caught Exception on $name Scope", throwable)
	}
) : CoroutineScope {

	private val parentJob = SupervisorJob(parentContext[Job])

	override val coroutineContext: CoroutineContext =
		parentContext + parentJob + CoroutineName("$name Scope") + dispatcher +
				CoroutineExceptionHandler { context, throwable ->
					exceptionHandler(context, throwable, log, name)
				}

	fun dispose() = this.parentJob.cancel()

	companion object {

		private val log = ModuleScope.getLogger()
	}
}
